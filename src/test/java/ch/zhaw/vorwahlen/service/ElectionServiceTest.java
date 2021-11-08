package ch.zhaw.vorwahlen.service;

import ch.zhaw.vorwahlen.model.dto.StudentDTO;
import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.model.modules.ModuleCategory;
import ch.zhaw.vorwahlen.model.modules.ModuleElection;
import ch.zhaw.vorwahlen.parser.ModuleParser;
import ch.zhaw.vorwahlen.repository.ElectionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DataJpaTest
class ElectionServiceTest {

    public static final int RANDOM_SEED = 32034;
    public static final int WPM_DISPENSATION = 8;
    public static final int CREDITS_PER_CONTEXT_MODULE = 2;
    public static final int CREDITS_PER_SUBJECT_MODULE = 4;
    public static final int CREDITS_PER_INTERDISCIPLINARY_MODULE = 4;

    public static final String LANGUAGE_DEUTSCH = "Deutsch";
    public static final String LANGUAGE_ENGLISCH = "Englisch";
    public static final String INTERDISCIPLINARY_PREFIX_WM = "WM.";

    public static final double CHANCE_TO_GET_GERMAN = 0.75;

    private final List<String> possibleContextPrefixes = List.of("WVK.", "WVK.SIC", "XXK.", "XX.");
    private final List<String> possibleSubjectPrefixes = List.of("WV.", "XX.");

    private final ElectionRepository electionRepository;

    private ElectionService electionService;

    private final StudentDTO studentDTOMock = mock(StudentDTO.class);
    private final ModuleElection moduleElectionMock = mock(ModuleElection.class);

    @Autowired
    public ElectionServiceTest(ElectionRepository electionRepository) {
        this.electionRepository = electionRepository;
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        electionService = new ElectionService(electionRepository);
    }

    @AfterEach
    void tearDown() {
    }

    // ================================================================================================================
    // Positive tests
    // ================================================================================================================

    @Test
    void testSaveElection() {
        when(studentDTOMock.getEmail()).thenReturn("test@mail.com");
        when(studentDTOMock.getWpmDispensation()).thenReturn(0);
        when(studentDTOMock.isTZ()).thenReturn(false);
        when(studentDTOMock.isIP()).thenReturn(false);

        var validElection = validElectionSet();
        var moduleElection = new ModuleElection();
        moduleElection.setElectedModules(validElection);
        moduleElection.setOverflowedElectedModules(new HashSet<>());

        assertTrue(electionService.saveElection(studentDTOMock, moduleElection));
        assertTrue(moduleElection.isElectionValid());

        var extraModuleMock = mock(Module.class);
        when(extraModuleMock.getCredits()).thenReturn((byte) CREDITS_PER_SUBJECT_MODULE);
        when(extraModuleMock.getShortModuleNo()).thenReturn(INTERDISCIPLINARY_PREFIX_WM);
        when(extraModuleMock.getModuleGroup()).thenReturn(ModuleParser.MODULE_GROUP_IT_6);
        when(extraModuleMock.getLanguage()).thenReturn(LANGUAGE_DEUTSCH);

        moduleElection.setOverflowedElectedModules(Set.of(extraModuleMock));

        assertTrue(electionService.saveElection(studentDTOMock, moduleElection));
        assertFalse(moduleElection.isElectionValid());
    }

    @Test
    @Sql("classpath:sql/modules_election.sql")
    void testValidateElectionFullTime() {
        var validElection = validElectionSet();

        //===== Returns valid
        when(moduleElectionMock.getOverflowedElectedModules()).thenReturn(new HashSet<>());
        when(moduleElectionMock.getElectedModules()).thenReturn(validElection);

        // Case VZ, Non-IP, No Dispensations
        when(studentDTOMock.isTZ()).thenReturn(false);
        when(studentDTOMock.isIP()).thenReturn(false);
        when(studentDTOMock.getWpmDispensation()).thenReturn(0);
        assertTrue(electionService.validateElection(studentDTOMock, moduleElectionMock));

        // Case VZ, IP, No Dispensations
        when(studentDTOMock.isIP()).thenReturn(true);
        assertTrue(electionService.validateElection(studentDTOMock, moduleElectionMock));

        // Case VZ, IP, Some Dispensations
        removeModulesFromSet(validElection);
        when(studentDTOMock.getWpmDispensation()).thenReturn(WPM_DISPENSATION);
        assertTrue(electionService.validateElection(studentDTOMock, moduleElectionMock));

        // Case VZ, Non-IP, Some Dispensations
        when(studentDTOMock.isIP()).thenReturn(false);
        assertTrue(electionService.validateElection(studentDTOMock, moduleElectionMock));

        //===== Returns invalid
        // Case VZ, Non-IP, No Dispensations (Not enough selected)
        when(studentDTOMock.getWpmDispensation()).thenReturn(0);
        for (var mode = 1; mode < 4; mode++) {
            assertInvalidElection(moduleElectionMock, studentDTOMock, mode);
        }

        // Case VZ, Non-IP, No Dispensations (To much selected)
        for (var mode = 4; mode < 7; mode++) {
            assertInvalidElection(moduleElectionMock, studentDTOMock, mode);
        }

        // Case VZ, IP, No Dispensations (Not enough english selected)
        assertInvalidElection(moduleElectionMock, studentDTOMock, 7);
    }

    private void assertInvalidElection(ModuleElection moduleElectionMock, StudentDTO studentDTOMock, int mode) {
        var invalidElection = invalidElectionSet(mode);
        when(moduleElectionMock.getElectedModules()).thenReturn(invalidElection);
        assertFalse(electionService.validateElection(studentDTOMock, moduleElectionMock));
    }

    private void removeModulesFromSet(Set<Module> set) {
        var removedCredits = 0;
        var iterator = set.iterator();
        while(iterator.hasNext() && removedCredits < ElectionServiceTest.WPM_DISPENSATION) {
            var module = iterator.next();
            if(module.getCredits() == CREDITS_PER_SUBJECT_MODULE && ModuleCategory.SUBJECT_MODULE == ModuleCategory.parse(module.getShortModuleNo(), module.getModuleGroup())) {
                removedCredits += module.getCredits();
                iterator.remove();
            }
        }
    }

    private Set<Module> validElectionSet() {
        var contextModuleSet = generateModuleMockSet(ElectionService.NUM_CONTEXT_MODULES);
        var interdisciplinaryModuleSet = generateModuleMockSet(ElectionService.NUM_INTERDISCIPLINARY_MODULES);
        var subjectModuleSet = generateModuleMockSet(ElectionService.NUM_SUBJECT_MODULES);

        var random = new Random(RANDOM_SEED);

        for(var module: contextModuleSet) {
            when(module.getCredits()).thenReturn((byte) CREDITS_PER_CONTEXT_MODULE);

            var index = random.nextInt(possibleContextPrefixes.size());
            var prefix = possibleContextPrefixes.get(index);
            when(module.getShortModuleNo()).thenReturn(prefix);

            when(module.getModuleGroup()).thenReturn(ModuleParser.MODULE_GROUP_IT_5);
            when(module.getLanguage()).thenReturn(LANGUAGE_DEUTSCH);
        }

        for(var module: interdisciplinaryModuleSet) {
            when(module.getCredits()).thenReturn((byte) CREDITS_PER_INTERDISCIPLINARY_MODULE);
            when(module.getShortModuleNo()).thenReturn(INTERDISCIPLINARY_PREFIX_WM);
            when(module.getModuleGroup()).thenReturn(ModuleParser.MODULE_GROUP_IT_6);
            when(module.getLanguage()).thenReturn(LANGUAGE_DEUTSCH);
        }

        for(var module: subjectModuleSet) {
            when(module.getCredits()).thenReturn((byte) CREDITS_PER_SUBJECT_MODULE);
            var index = random.nextInt(possibleSubjectPrefixes.size());
            when(module.getShortModuleNo()).thenReturn(possibleSubjectPrefixes.get(index));
            when(module.getModuleGroup()).thenReturn(ModuleParser.MODULE_GROUP_IT_6);

            index = random.nextInt(Integer.MAX_VALUE);
            var language = index > CHANCE_TO_GET_GERMAN * Integer.MAX_VALUE
                    ? LANGUAGE_DEUTSCH
                    : LANGUAGE_ENGLISCH;
            when(module.getLanguage()).thenReturn(language);
        }

        var set = new HashSet<Module>();
        set.addAll(contextModuleSet);
        set.addAll(interdisciplinaryModuleSet);
        set.addAll(subjectModuleSet);
        return set;
    }

    private Set<Module> invalidElectionSet(int mode) {
        var set = validElectionSet();
        var module = mock(Module.class);
        switch (mode) {
            case 1 -> removeOneModuleByCategory(set, ModuleCategory.CONTEXT_MODULE);
            case 2 -> removeOneModuleByCategory(set, ModuleCategory.SUBJECT_MODULE);
            case 3 -> removeOneModuleByCategory(set, ModuleCategory.INTERDISCIPLINARY_MODULE);
            case 4 -> addModule(set, possibleContextPrefixes.get(0), module);
            case 5 -> addModule(set, possibleSubjectPrefixes.get(0), module);
            case 6 -> addModule(set, INTERDISCIPLINARY_PREFIX_WM, module);
            case 7 -> removeEnglishModules(set);
        }
        return set;
    }

    private void removeEnglishModules(Set<Module> set) {
        var removeCounter = 0;
        var iter = set.iterator();
        /*
         * With no dispensations there are 8 Subject modules.
         * At least 5 of them have to be english to get a total of 20 Credits.
         */
        while(removeCounter < CREDITS_PER_SUBJECT_MODULE && iter.hasNext()) {
            var module = iter.next();
            if(ModuleCategory.SUBJECT_MODULE == ModuleCategory.parse(module.getShortModuleNo(), module.getModuleGroup())
                && LANGUAGE_ENGLISCH.equals(module.getLanguage())) {
                iter.remove();
                removeCounter++;
            }
        }
    }

    private void addModule(Set<Module> set, String moduleNo, Module module) {
        when(module.getShortModuleNo()).thenReturn(moduleNo);
        set.add(module);
    }

    private void removeOneModuleByCategory(Set<Module> set, ModuleCategory moduleCategory) {
        var removed = false;
        var iter = set.iterator();
        while(!removed && iter.hasNext()) {
            var module = iter.next();
            if(moduleCategory == ModuleCategory.parse(module.getShortModuleNo(), module.getModuleGroup())) {
                removed = true;
                iter.remove();
            }
        }
    }

    private Set<Module> generateModuleMockSet(int numElements) {
        var set = new HashSet<Module>();
        for (var i = 0; i < numElements; i++) {
            set.add(mock(Module.class));
        }
        return set;
    }

    @Test
    void testValidateElectionPartTime() {

    }

    // ================================================================================================================
    // Negative tests
    // ================================================================================================================

    @Test
    void testSaveElection_Null_Student() {
        var validElection = validElectionSet();
        var moduleElection = new ModuleElection();
        moduleElection.setElectedModules(validElection);
        moduleElection.setOverflowedElectedModules(new HashSet<>());

        assertFalse(electionService.saveElection(null, moduleElection));
    }

    @Test
    void testSaveElection_Null_Election() {
        when(studentDTOMock.getEmail()).thenReturn("test@mail.com");
        when(studentDTOMock.getWpmDispensation()).thenReturn(0);
        when(studentDTOMock.isTZ()).thenReturn(false);
        when(studentDTOMock.isIP()).thenReturn(false);

        assertFalse(electionService.saveElection(studentDTOMock, null));
    }

    @Test
    void testSaveElection_Null_Email() {
        when(studentDTOMock.getEmail()).thenReturn(null);
        when(studentDTOMock.getWpmDispensation()).thenReturn(0);
        when(studentDTOMock.isTZ()).thenReturn(false);
        when(studentDTOMock.isIP()).thenReturn(false);

        var validElection = validElectionSet();
        var moduleElection = new ModuleElection();
        moduleElection.setElectedModules(validElection);
        moduleElection.setOverflowedElectedModules(new HashSet<>());

        assertFalse(electionService.saveElection(studentDTOMock, moduleElection));
    }

    @Test
    void testSaveElection_Blank_Email() {
        when(studentDTOMock.getEmail()).thenReturn("  ");
        when(studentDTOMock.getWpmDispensation()).thenReturn(0);
        when(studentDTOMock.isTZ()).thenReturn(false);
        when(studentDTOMock.isIP()).thenReturn(false);

        var validElection = validElectionSet();
        var moduleElection = new ModuleElection();
        moduleElection.setElectedModules(validElection);
        moduleElection.setOverflowedElectedModules(new HashSet<>());

        assertFalse(electionService.saveElection(studentDTOMock, moduleElection));
    }

    // rest is tested with validatieElection(.., ..)



}
