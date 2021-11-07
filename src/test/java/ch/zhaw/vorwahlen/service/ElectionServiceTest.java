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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DataJpaTest
class ElectionServiceTest {

    public static final int WPM_DISPENSATION = 8;
    private final ElectionRepository electionRepository;

    private ElectionService electionService;

    @Autowired
    public ElectionServiceTest(ElectionRepository electionRepository) {
        this.electionRepository = electionRepository;
    }

    @BeforeEach
    void setUp() {
        electionService = new ElectionService(electionRepository);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testValidateElectionFullTime() {
        var studentDTOMock = mock(StudentDTO.class);
        var moduleElectionMock = mock(ModuleElection.class);
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
        removeModulesFromSet(validElection, WPM_DISPENSATION);
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

    private void removeModulesFromSet(Set<Module> set, int dispensation) {
        var removedCredits = 0;
        var iterator = set.iterator();
        while(iterator.hasNext() && removedCredits < dispensation) {
            var module = iterator.next();
            if(module.getCredits() == 4 && ModuleCategory.SUBJECT_MODULE == ModuleCategory.parse(module.getModuleNo(), module.getModuleGroup())) {
                removedCredits += module.getCredits();
                iterator.remove();
            }
        }
    }

    private Set<Module> validElectionSet() {
        var contextModuleSet = generateModuleMockSet(ElectionService.NUM_CONTEXT_MODULES);
        var interdisciplinaryModuleSet = generateModuleMockSet(ElectionService.NUM_INTERDISCIPLINARY_MODULES);
        var subjectModuleSet = generateModuleMockSet(ElectionService.NUM_SUBJECT_MODULES);

        var random = new Random(32034);

        var possibleContextPrefixes = List.of("WVK.", "WVK.SIC", "XXK.", "XX.");
        for(var module: contextModuleSet) {
            when(module.getCredits()).thenReturn((byte) 2);

            var index = random.nextInt(possibleContextPrefixes.size());
            var prefix = possibleContextPrefixes.get(index);
            when(module.getModuleNo()).thenReturn(prefix);

            when(module.getModuleGroup()).thenReturn(ModuleParser.MODULE_GROUP_IT_5);
            when(module.getLanguage()).thenReturn("Deutsch");
        }

        for(var module: interdisciplinaryModuleSet) {
            when(module.getCredits()).thenReturn((byte) 4);
            when(module.getModuleNo()).thenReturn("WM.");
            when(module.getModuleGroup()).thenReturn(ModuleParser.MODULE_GROUP_IT_6);
            when(module.getLanguage()).thenReturn("Deutsch");
        }

        var possibleSubjectPrefixes = List.of("WV.", "XX.");
        for(var module: subjectModuleSet) {
            when(module.getCredits()).thenReturn((byte) 4);
            var index = random.nextInt(possibleSubjectPrefixes.size());
            when(module.getModuleNo()).thenReturn(possibleSubjectPrefixes.get(index));
            when(module.getModuleGroup()).thenReturn(ModuleParser.MODULE_GROUP_IT_6);

            index = random.nextInt(Integer.MAX_VALUE);
            var language = index > 0.75 * Integer.MAX_VALUE
                    ? "Deutsch"
                    : "Englisch";
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
            case 4 -> addModule(set, "WVK.", module);
            case 5 -> addModule(set, "WV.", module);
            case 6 -> addModule(set, "WM.", module);
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
        while(removeCounter < 4 && iter.hasNext()) {
            var module = iter.next();
            if(ModuleCategory.SUBJECT_MODULE == ModuleCategory.parse(module.getModuleNo(), module.getModuleGroup())
                && "Englisch".equals(module.getLanguage())) {
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

}
