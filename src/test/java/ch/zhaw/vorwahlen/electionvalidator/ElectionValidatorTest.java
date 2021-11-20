package ch.zhaw.vorwahlen.electionvalidator;

import ch.zhaw.vorwahlen.model.modules.Student;
import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.model.modules.ModuleCategory;
import ch.zhaw.vorwahlen.model.modules.ModuleElection;
import ch.zhaw.vorwahlen.modulevalidation.ElectionValidator;
import ch.zhaw.vorwahlen.modulevalidation.FullTimeElectionValidator;
import ch.zhaw.vorwahlen.parser.ModuleParser;
import ch.zhaw.vorwahlen.service.ElectionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ElectionValidatorTest {

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

    private final ModuleElection moduleElectionMock = mock(ModuleElection.class);
    private final List<String> interdisciplinaryModulesShort = List.of("WM.PHMOD");
    private final List<String> contextModulesShort = List.of("XXK.FUPRE", "WVK.ZURO", "WVK.SIC-TAF");
    private final List<String> subjectModulesShort = List.of("WV.ESE", "WV.FUP");
    private final List<String> consecutiveSubjectModulesShort = List.of(
            "WV.AI2-EN",
            "WV.AI1-EN",
            "WV.CCP2-EN",
            "WV.CCP1-EN",
            "WV.DIP2-EN",
            "WV.DIP-EN"
    );

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


    private final Student studentMock = mock(Student.class);

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    @Sql("classpath:sql/modules_test_election.sql")
    void testValidateElectionFullTime() {
        var validator = new FullTimeElectionValidator(studentMock);

        var validElection = validElectionSet();

        //===== Returns valid
        when(moduleElectionMock.getOverflowedElectedModules()).thenReturn(new HashSet<>());
        when(moduleElectionMock.getElectedModules()).thenReturn(validElection);

        // Case VZ, Non-IP, No Dispensations
        when(studentMock.isTZ()).thenReturn(false);
        when(studentMock.isIP()).thenReturn(false);
        when(studentMock.getWpmDispensation()).thenReturn(0);
        assertTrue(validator.validate(moduleElectionMock));

        // Case VZ, IP, No Dispensations
        when(studentMock.isIP()).thenReturn(true);
        assertTrue(validator.validate(moduleElectionMock));

        // Case VZ, IP, Some Dispensations
        removeModulesFromSet(validElection);
        when(studentMock.getWpmDispensation()).thenReturn(WPM_DISPENSATION);
        assertTrue(validator.validate(moduleElectionMock));

        // Case VZ, Non-IP, Some Dispensations
        when(studentMock.isIP()).thenReturn(false);
        assertTrue(validator.validate(moduleElectionMock));

        //===== Returns invalid
        // Case VZ, Non-IP, No Dispensations (Not enough selected)
        when(studentMock.getWpmDispensation()).thenReturn(0);
        for (var mode = 1; mode < 4; mode++) {
            assertInvalidElection(moduleElectionMock, validator, mode);
        }

        // Case VZ, Non-IP, No Dispensations (Too much selected)
        for (var mode = 4; mode < 7; mode++) {
            assertInvalidElection(moduleElectionMock, validator, mode);
        }

        // Case VZ, IP, No Dispensations (Not enough english selected)
        assertInvalidElection(moduleElectionMock, validator, 7);
    }

    @Test
    void testValidateElectionPartTime() {

    }

    private List<Module> generateModuleMockSet(int numElements) {
        var list = new ArrayList<Module>();
        for (var i = 0; i < numElements; i++) {
            list.add(mock(Module.class));
        }
        return list;
    }

    private Set<Module> validElectionSet() {
        var contextModuleMockList = generateModuleMockSet(ElectionService.NUM_CONTEXT_MODULES);
        var interdisciplinaryModuleMockList = generateModuleMockSet(ElectionService.NUM_INTERDISCIPLINARY_MODULES);
        var subjectModuleMockList = generateModuleMockSet(ElectionService.NUM_SUBJECT_MODULES);

        assertEquals(interdisciplinaryModules.size(), interdisciplinaryModuleMockList.size());
        assertEquals(contextModules.size(), contextModuleMockList.size());
        assertEquals(subjectModules.size() + consecutiveSubjectModules.size(), subjectModuleMockList.size());
        assertEquals(0, consecutiveSubjectModules.size() % 2);
        assertEquals(contextModules.size(), contextModulesShort.size());
        assertEquals(interdisciplinaryModules.size(), interdisciplinaryModulesShort.size());
        assertEquals(subjectModules.size(), subjectModulesShort.size());

        for (var i = 0; i < contextModuleMockList.size(); i++) {
            var mock = contextModuleMockList.get(i);
            when(mock.getCredits()).thenReturn((byte) CREDITS_PER_CONTEXT_MODULE);
            when(mock.getLanguage()).thenReturn(LANGUAGE_DEUTSCH);
            when(mock.getModuleGroup()).thenReturn(ModuleParser.MODULE_GROUP_IT_5);
            when(mock.getModuleNo()).thenReturn(contextModules.get(i));
            when(mock.getShortModuleNo()).thenReturn(contextModulesShort.get(i));
        }

        for (var i = 0; i < interdisciplinaryModuleMockList.size(); i++) {
            var mock = interdisciplinaryModuleMockList.get(i);
            when(mock.getCredits()).thenReturn((byte) CREDITS_PER_INTERDISCIPLINARY_MODULE);
            when(mock.getLanguage()).thenReturn(LANGUAGE_DEUTSCH);
            when(mock.getModuleGroup()).thenReturn(ModuleParser.MODULE_GROUP_IT_6);
            when(mock.getModuleNo()).thenReturn(interdisciplinaryModules.get(i));
            when(mock.getShortModuleNo()).thenReturn(interdisciplinaryModulesShort.get(i));
        }

        var random = new Random(RANDOM_SEED);
        var mockCounter = 0;
        for (var i = 0; i < subjectModules.size(); i++) {
            var index = random.nextInt(Integer.MAX_VALUE);
            var language = index > CHANCE_TO_GET_GERMAN * Integer.MAX_VALUE
                    ? LANGUAGE_DEUTSCH
                    : LANGUAGE_ENGLISCH;

            var mock = subjectModuleMockList.get(mockCounter);
            mockCounter++;
            when(mock.getCredits()).thenReturn((byte) CREDITS_PER_SUBJECT_MODULE);
            when(mock.getLanguage()).thenReturn(language);
            when(mock.getModuleGroup()).thenReturn(ModuleParser.MODULE_GROUP_IT_6);
            when(mock.getModuleNo()).thenReturn(subjectModules.get(i));
            when(mock.getShortModuleNo()).thenReturn(subjectModulesShort.get(i));
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
            when(mock1.getShortModuleNo()).thenReturn(consecutiveSubjectModulesShort.get(i - 1));
            when(mock1.getModuleNo()).thenReturn(consecutiveSubjectModules.get(i - 1));
            when(mock1.getConsecutiveModuleNo()).thenReturn(consecutiveSubjectModules.get(i));

            var mock2 = subjectModuleMockList.get(mockCounter);
            mockCounter++;
            when(mock2.getCredits()).thenReturn((byte) CREDITS_PER_SUBJECT_MODULE);
            when(mock2.getLanguage()).thenReturn(language);
            when(mock2.getModuleGroup()).thenReturn(ModuleParser.MODULE_GROUP_IT_6);
            when(mock2.getShortModuleNo()).thenReturn(consecutiveSubjectModulesShort.get(i));
            when(mock2.getModuleNo()).thenReturn(consecutiveSubjectModules.get(i));
            when(mock2.getConsecutiveModuleNo()).thenReturn(consecutiveSubjectModules.get(i - 1));
        }

        var set = new HashSet<Module>();
        set.addAll(contextModuleMockList);
        set.addAll(interdisciplinaryModuleMockList);
        set.addAll(subjectModuleMockList);
        return set;
    }


    private void assertInvalidElection(ModuleElection moduleElectionMock, ElectionValidator validator, int mode) {
        var invalidElection = invalidElectionSet(mode);
        when(moduleElectionMock.getElectedModules()).thenReturn(invalidElection);
        assertFalse(validator.validate(moduleElectionMock));
    }

    private void removeModulesFromSet(Set<Module> set) {
        var removedCredits = 0;
        var iterator = set.iterator();
        while(iterator.hasNext() && removedCredits < WPM_DISPENSATION) {
            var module = iterator.next();
            if(module.getCredits() == CREDITS_PER_SUBJECT_MODULE
                    && ModuleCategory.SUBJECT_MODULE == ModuleCategory.parse(module.getModuleNo(), module.getModuleGroup())
                    && (module.getConsecutiveModuleNo() == null || module.getConsecutiveModuleNo().isBlank())) {
                removedCredits += module.getCredits();
                iterator.remove();
            }
        }
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

}
