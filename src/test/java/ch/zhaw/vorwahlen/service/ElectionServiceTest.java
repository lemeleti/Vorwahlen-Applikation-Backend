package ch.zhaw.vorwahlen.service;

import ch.zhaw.vorwahlen.model.dto.ModuleElectionDTO;
import ch.zhaw.vorwahlen.model.dto.StudentDTO;
import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.model.modules.ModuleCategory;
import ch.zhaw.vorwahlen.model.modules.ModuleElection;
import ch.zhaw.vorwahlen.parser.ModuleParser;
import ch.zhaw.vorwahlen.repository.ElectionRepository;
import ch.zhaw.vorwahlen.repository.ModuleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

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
    public static final String INTERDISCIPLINARY_PREFIX_WM = "t.BA.WM.";

    public static final double CHANCE_TO_GET_GERMAN = 0.75;

    private final List<String> possibleContextPrefixes = List.of("t.BA.WVK.", "t.BA.WVK.SIC", "t.BA.XXK.", "t.BA.XX.");
    private final List<String> possibleSubjectPrefixes = List.of("t.BA.WV.", "t.BA.XX.");

    private final ElectionRepository electionRepository;
    private final ModuleRepository moduleRepository;

    private ElectionService electionService;

    private final StudentDTO studentDTOMock = mock(StudentDTO.class);
    private final ModuleElection moduleElectionMock = mock(ModuleElection.class);

    private final List<String> interdisciplinaryModules = List.of("t.BA.WM.PHMOD.19HS");
    private final List<String> contextModules = List.of("t.BA.XXK.FUPRE.19HS", "t.BA.WVK.ZURO.20HS", "t.BA.WVK.SIC-TAF.20HS");
    private final List<String> subjectModules = List.of("t.BA.WV.ESE.19HS", "t.BA.WV.FUP.19HS");
    private final List<String> consecutiveSubjectModules = List.of(
            "t.BA.WV.AI2-EN.19HS",
            "t.BA.WV.AI1-EN.19HS",
            "t.BA.WV.CCP2-EN.19HS",
            "t.BA.WV.CCP1-EN.19HS",
            "t.BA.WV.DIP2-EN.20HS",
            "t.BA.WV.DIP-EN.19HS"
    );
    @Autowired
    public ElectionServiceTest(ElectionRepository electionRepository, ModuleRepository moduleRepository) {
        this.electionRepository = electionRepository;
        this.moduleRepository = moduleRepository;
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        electionService = new ElectionService(electionRepository, moduleRepository);
    }

    @AfterEach
    void tearDown() {
    }

    // ================================================================================================================
    // Positive tests
    // ================================================================================================================

    @Test
    @Sql("classpath:sql/modules_test_election.sql")
    void testGetModuleElectionByStudent() {
        when(studentDTOMock.getEmail()).thenReturn("test@mail.com");
        when(studentDTOMock.getWpmDispensation()).thenReturn(0);
        when(studentDTOMock.isTZ()).thenReturn(false);
        when(studentDTOMock.isIP()).thenReturn(false);

        assertNull(electionService.getModuleElectionByStudent(studentDTOMock));

        var validElection = validElectionSetForElectionDTO();
        var moduleElection = new ModuleElectionDTO();
        moduleElection.setElectedModules(validElection);
        moduleElection.setOverflowedElectedModules(new HashSet<>());

        electionService.saveElection(studentDTOMock, moduleElection);
        assertNotNull(electionService.getModuleElectionByStudent(studentDTOMock));
    }

    @Test
    @Sql("classpath:sql/modules_test_election.sql")
    void testSaveElection() {
        when(studentDTOMock.getEmail()).thenReturn("test@mail.com");
        // case VZ, Non-IP, No dispensations
        when(studentDTOMock.getWpmDispensation()).thenReturn(0);
        when(studentDTOMock.isTZ()).thenReturn(false);
        when(studentDTOMock.isIP()).thenReturn(false);

        var validElection = validElectionSetForElectionDTO();
        var moduleElection = new ModuleElectionDTO();
        moduleElection.setElectedModules(validElection);
        moduleElection.setOverflowedElectedModules(new HashSet<>());

        var jsonNode = new ObjectMapper().createObjectNode();
        jsonNode.put("election_saved", true);
        jsonNode.put("election_valid", true);

        assertEquals(jsonNode, electionService.saveElection(studentDTOMock, moduleElection));
        assertTrue(moduleElection.isElectionValid());

        jsonNode.put("election_valid", false);
        moduleElection.setOverflowedElectedModules(
                moduleRepository.findAll().stream()
                        .filter(module -> ModuleCategory.parse(module.getModuleNo(), module.getModuleGroup()) == ModuleCategory.INTERDISCIPLINARY_MODULE)
                        .filter(module -> !validElection.contains(module.getModuleNo()))
                        .findAny()
                        .map(Module::getModuleNo)
                        .stream()
                        .collect(Collectors.toSet()));

        assertFalse(moduleElection.getOverflowedElectedModules().isEmpty());
        assertEquals(jsonNode, electionService.saveElection(studentDTOMock, moduleElection));
        assertFalse(moduleElection.isElectionValid());
    }

    @Test
    @Sql("classpath:sql/modules_test_election.sql")
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

        // Case VZ, Non-IP, No Dispensations (Too much selected)
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
            if(module.getCredits() == CREDITS_PER_SUBJECT_MODULE
                    && ModuleCategory.SUBJECT_MODULE == ModuleCategory.parse(module.getModuleNo(), module.getModuleGroup())
                    && (module.getConsecutiveModuleNo() == null || module.getConsecutiveModuleNo().isBlank())) {
                removedCredits += module.getCredits();
                iterator.remove();
            }
        }
    }

    private Set<String> validElectionSetForElectionDTO() {
        var set = new HashSet<String>();
        set.addAll(interdisciplinaryModules);
        set.addAll(contextModules);
        set.addAll(subjectModules);
        set.addAll(consecutiveSubjectModules);
        return set;
    }

    private Set<Module> validElectionSet() {
        var contextModuleMockList = generateModuleMockSet(ElectionService.NUM_CONTEXT_MODULES);
        var interdisciplinaryModuleMockList = generateModuleMockSet(ElectionService.NUM_INTERDISCIPLINARY_MODULES);
        var subjectModuleMockList = generateModuleMockSet(ElectionService.NUM_SUBJECT_MODULES);

        assertEquals(interdisciplinaryModules.size(), interdisciplinaryModuleMockList.size());
        assertEquals(contextModules.size(), contextModuleMockList.size());
        assertEquals(subjectModules.size() + consecutiveSubjectModules.size(), subjectModuleMockList.size());
        assertEquals(0, consecutiveSubjectModules.size() % 2);

        for (var i = 0; i < contextModuleMockList.size(); i++) {
            var mock = contextModuleMockList.get(i);
            when(mock.getCredits()).thenReturn((byte) CREDITS_PER_CONTEXT_MODULE);
            when(mock.getLanguage()).thenReturn(LANGUAGE_DEUTSCH);
            when(mock.getModuleGroup()).thenReturn(ModuleParser.MODULE_GROUP_IT_5);
            when(mock.getModuleNo()).thenReturn(contextModules.get(i));
        }

        for (var i = 0; i < interdisciplinaryModuleMockList.size(); i++) {
            var mock = interdisciplinaryModuleMockList.get(i);
            when(mock.getCredits()).thenReturn((byte) CREDITS_PER_INTERDISCIPLINARY_MODULE);
            when(mock.getLanguage()).thenReturn(LANGUAGE_DEUTSCH);
            when(mock.getModuleGroup()).thenReturn(ModuleParser.MODULE_GROUP_IT_6);
            when(mock.getModuleNo()).thenReturn(interdisciplinaryModules.get(i));
        }

        var random = new Random(RANDOM_SEED);
        var mockCounter = 0;
        for (String subjectModule : subjectModules) {
            var index = random.nextInt(Integer.MAX_VALUE);
            var language = index > CHANCE_TO_GET_GERMAN * Integer.MAX_VALUE
                    ? LANGUAGE_DEUTSCH
                    : LANGUAGE_ENGLISCH;

            var mock = subjectModuleMockList.get(mockCounter);
            mockCounter++;
            when(mock.getCredits()).thenReturn((byte) CREDITS_PER_SUBJECT_MODULE);
            when(mock.getLanguage()).thenReturn(language);
            when(mock.getModuleGroup()).thenReturn(ModuleParser.MODULE_GROUP_IT_6);
            when(mock.getModuleNo()).thenReturn(subjectModule);
        }

        for (var i = 1; i < consecutiveSubjectModules.size(); i += 2) {
            var index = random.nextInt(Integer.MAX_VALUE);
            var language = index > CHANCE_TO_GET_GERMAN * Integer.MAX_VALUE
                    ? LANGUAGE_DEUTSCH
                    : LANGUAGE_ENGLISCH;

            var mock1 = subjectModuleMockList.get(mockCounter);
            mockCounter++;
            when(mock1.getCredits()).thenReturn((byte) CREDITS_PER_SUBJECT_MODULE);
            when(mock1.getLanguage()).thenReturn(language);
            when(mock1.getModuleGroup()).thenReturn(ModuleParser.MODULE_GROUP_IT_6);
            when(mock1.getModuleNo()).thenReturn(consecutiveSubjectModules.get(i - 1));
            when(mock1.getConsecutiveModuleNo()).thenReturn(consecutiveSubjectModules.get(i));

            var mock2 = subjectModuleMockList.get(mockCounter);
            mockCounter++;
            when(mock2.getCredits()).thenReturn((byte) CREDITS_PER_SUBJECT_MODULE);
            when(mock2.getLanguage()).thenReturn(language);
            when(mock2.getModuleGroup()).thenReturn(ModuleParser.MODULE_GROUP_IT_6);
            when(mock2.getModuleNo()).thenReturn(consecutiveSubjectModules.get(i));
            when(mock2.getConsecutiveModuleNo()).thenReturn(consecutiveSubjectModules.get(i - 1));
        }


        var set = new HashSet<Module>();
        set.addAll(contextModuleMockList);
        set.addAll(interdisciplinaryModuleMockList);
        set.addAll(subjectModuleMockList);
        set.forEach(module -> System.out.println(module.getModuleNo() + " - " + module.getConsecutiveModuleNo()));
        System.out.println("-------------");
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
            if(ModuleCategory.SUBJECT_MODULE == ModuleCategory.parse(module.getModuleNo(), module.getModuleGroup())
                && LANGUAGE_ENGLISCH.equals(module.getLanguage())) {
                iter.remove();
                removeCounter++;
            }
        }
    }

    private void addModule(Set<Module> set, String moduleNo, Module module) {
        when(module.getModuleNo()).thenReturn(moduleNo);
        set.add(module);
    }

    private void removeOneModuleByCategory(Set<Module> set, ModuleCategory moduleCategory) {
        var removed = false;
        var iter = set.iterator();
        while(!removed && iter.hasNext()) {
            var module = iter.next();
            if(moduleCategory == ModuleCategory.parse(module.getModuleNo(), module.getModuleGroup())) {
                removed = true;
                iter.remove();
            }
        }
    }

    private List<Module> generateModuleMockSet(int numElements) {
        var list = new ArrayList<Module>();
        for (var i = 0; i < numElements; i++) {
            list.add(mock(Module.class));
        }
        return list;
    }

    @Test
    void testValidateElectionPartTime() {

    }

    // ================================================================================================================
    // Negative tests
    // ================================================================================================================

    @Test
    @Sql("classpath:sql/modules_test_election.sql")
    void testSaveElection_Null_Student() {
        var validElection = validElectionSetForElectionDTO();
        var moduleElectionDTO = new ModuleElectionDTO();
        moduleElectionDTO.setElectedModules(validElection);
        moduleElectionDTO.setOverflowedElectedModules(new HashSet<>());

        var jsonNode = new ObjectMapper().createObjectNode();
        jsonNode.put("election_saved", false);
        jsonNode.put("election_valid", false);

        assertEquals(jsonNode, electionService.saveElection(null, moduleElectionDTO));
    }

    @Test
    void testSaveElection_Null_Election() {
        when(studentDTOMock.getEmail()).thenReturn("test@mail.com");
        when(studentDTOMock.getWpmDispensation()).thenReturn(0);
        when(studentDTOMock.isTZ()).thenReturn(false);
        when(studentDTOMock.isIP()).thenReturn(false);

        var jsonNode = new ObjectMapper().createObjectNode();
        jsonNode.put("election_saved", false);
        jsonNode.put("election_valid", false);

        assertEquals(jsonNode, electionService.saveElection(studentDTOMock, null));
    }

    @Test
    @Sql("classpath:sql/modules_test_election.sql")
    void testSaveElection_Null_Email() {
        when(studentDTOMock.getEmail()).thenReturn(null);
        when(studentDTOMock.getWpmDispensation()).thenReturn(0);
        when(studentDTOMock.isTZ()).thenReturn(false);
        when(studentDTOMock.isIP()).thenReturn(false);

        var validElection = validElectionSetForElectionDTO();
        var moduleElectionDTO = new ModuleElectionDTO();
        moduleElectionDTO.setElectedModules(validElection);
        moduleElectionDTO.setOverflowedElectedModules(new HashSet<>());

        var jsonNode = new ObjectMapper().createObjectNode();
        jsonNode.put("election_saved", false);
        jsonNode.put("election_valid", false);

        assertEquals(jsonNode, electionService.saveElection(studentDTOMock, moduleElectionDTO));
    }

    @Test
    @Sql("classpath:sql/modules_test_election.sql")
    void testSaveElection_Blank_Email() {
        when(studentDTOMock.getEmail()).thenReturn("  ");
        when(studentDTOMock.getWpmDispensation()).thenReturn(0);
        when(studentDTOMock.isTZ()).thenReturn(false);
        when(studentDTOMock.isIP()).thenReturn(false);

        var validElection = validElectionSetForElectionDTO();
        var moduleElectionDTO = new ModuleElectionDTO();
        moduleElectionDTO.setElectedModules(validElection);
        moduleElectionDTO.setOverflowedElectedModules(new HashSet<>());

        var jsonNode = new ObjectMapper().createObjectNode();
        jsonNode.put("election_saved", false);
        jsonNode.put("election_valid", false);

        assertEquals(jsonNode, electionService.saveElection(studentDTOMock, moduleElectionDTO));
    }

    // rest is tested with validatieElection(.., ..)



}
