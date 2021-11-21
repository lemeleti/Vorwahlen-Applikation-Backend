package ch.zhaw.vorwahlen.modulevalidation;

import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.model.modules.ModuleCategory;
import ch.zhaw.vorwahlen.model.modules.ModuleElection;
import ch.zhaw.vorwahlen.model.modules.Student;
import ch.zhaw.vorwahlen.parser.ModuleParser;
import ch.zhaw.vorwahlen.service.ElectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AbstractElectionValidatorTest {

    private static final int NUM_CONTEXT_MODULES = 3;
    private static final int NUM_SUBJECT_MODULES = 8;
    private static final int NUM_INTERDISCIPLINARY_MODULES = 1;
    private static final String NON_EXISTING_MODULE = "WHOAMI";

    static final int RANDOM_SEED = 32034;
    static final int WPM_DISPENSATION = 8;

    static final int CREDITS_PER_CONTEXT_MODULE = 2;
    static final int CREDITS_PER_SUBJECT_MODULE = 4;
    static final int CREDITS_PER_INTERDISCIPLINARY_MODULE = 4;

    static final String LANGUAGE_DEUTSCH = "Deutsch";
    static final String LANGUAGE_ENGLISCH = "Englisch";
    static final double CHANCE_TO_GET_GERMAN = 0.75;
    public static final String CONSECUTIVE_VALUE = "consecutive";
    public static final String MODULE_WV_PSPP = "WV.PSPP";

    final List<String> interdisciplinaryModulesShort = List.of("WM.PHMOD");
    final List<String> contextModulesShort = List.of("XXK.FUPRE", "WVK.ZURO", "WVK.SIC-TAF");
    final List<String> subjectModulesShort = List.of("WV.ESE", "WV.FUP");
    final List<String> consecutiveSubjectModulesShort = List.of(
            "WV.AI2-EN",
            "WV.AI1-EN",
            "WV.CCP2-EN",
            "WV.CCP1-EN",
            "WV.DIP2-EN",
            "WV.DIP-EN"
    );

    final List<String> interdisciplinaryModules = List.of("t.BA.WM.PHMOD.19HS");
    final List<String> contextModules = List.of("t.BA.XXK.FUPRE.19HS", "t.BA.WVK.ZURO.20HS", "t.BA.WVK.SIC-TAF.20HS");
    final List<String> subjectModules = List.of("t.BA.WV.ESE.19HS", "t.BA.WV.FUP.19HS");
    final List<String> consecutiveSubjectModules = List.of(
            "t.BA.WV.AI2-EN.19HS",
            "t.BA.WV.AI1-EN.19HS",
            "t.BA.WV.CCP2-EN.19HS",
            "t.BA.WV.CCP1-EN.19HS",
            "t.BA.WV.DIP2-EN.20HS",
            "t.BA.WV.DIP-EN.19HS"
    );

    @Mock
    Student studentMock;

    @Mock
    ModuleElection moduleElectionMock;

    AbstractElectionValidator validator;

    Set<Module> validElectionSet;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = mock(AbstractElectionValidator.class, Mockito.withSettings()
                .useConstructor(studentMock)
                .defaultAnswer(CALLS_REAL_METHODS));

        validElectionSet = generateValidElectionSet();
    }

    /* **************************************************************************************************************
     * Positive tests
     * ************************************************************************************************************** */

    @Test
    void testValidConsecutiveModulePairsInElection() {
        when(moduleElectionMock.getElectedModules()).thenReturn(validElectionSet);
        assertTrue(validator.validConsecutiveModulePairsInElection(moduleElectionMock));

        var m1 = mock(Module.class);
        var m2 = mock(Module.class);
        var m3 = mock(Module.class);
        var m4 = mock(Module.class);

        // AI 1
        when(m1.getConsecutiveModuleNo()).thenReturn(consecutiveSubjectModules.get(1));
        when(m1.getShortModuleNo()).thenReturn(consecutiveSubjectModulesShort.get(1));

        // AI 2
        when(m2.getConsecutiveModuleNo()).thenReturn(consecutiveSubjectModules.get(0));
        when(m2.getShortModuleNo()).thenReturn(consecutiveSubjectModulesShort.get(0));

        // FUP
        when(m3.getShortModuleNo()).thenReturn(subjectModulesShort.get(1));

        // PSPP
        when(m4.getShortModuleNo()).thenReturn(MODULE_WV_PSPP);

        // AI1, AI2, PSPP, FUP
        when(moduleElectionMock.getElectedModules()).thenReturn(Set.of(m1, m2, m3, m4));
        assertTrue(validator.validConsecutiveModulePairsInElection(moduleElectionMock));

        // AI1, AI2, PSPP
        when(moduleElectionMock.getElectedModules()).thenReturn(Set.of(m1, m2, m4));
        assertFalse(validator.validConsecutiveModulePairsInElection(moduleElectionMock));
    }

    @Test
    void testAreModulesConsecutive() {
        var moduleMock1 = mock(Module.class);
        var moduleMock2 = mock(Module.class);

        when(moduleMock1.getConsecutiveModuleNo()).thenReturn(null);
        assertFalse(validator.areModulesConsecutive(moduleMock1, moduleMock2));

        when(moduleMock1.getConsecutiveModuleNo()).thenReturn(" ");
        assertFalse(validator.areModulesConsecutive(moduleMock1, moduleMock2));

        when(moduleMock1.getConsecutiveModuleNo()).thenReturn(CONSECUTIVE_VALUE);
        when(moduleMock2.getConsecutiveModuleNo()).thenReturn(null);
        assertFalse(validator.areModulesConsecutive(moduleMock1, moduleMock2));

        when(moduleMock2.getConsecutiveModuleNo()).thenReturn(" ");
        assertFalse(validator.areModulesConsecutive(moduleMock1, moduleMock2));

        when(moduleMock2.getConsecutiveModuleNo()).thenReturn(CONSECUTIVE_VALUE);
        assertTrue(validator.areModulesConsecutive(moduleMock1, moduleMock2));

    }

    @Test
    void testContainsModule() {
        assertTrue(validator.containsModule(validElectionSet, consecutiveSubjectModulesShort.get(0)));
        assertFalse(validator.containsModule(validElectionSet, NON_EXISTING_MODULE));
    }

    @Test
    void testCountModuleCategory() {
        when(moduleElectionMock.getElectedModules()).thenReturn(validElectionSet);
        assertEquals(NUM_CONTEXT_MODULES, validator.countModuleCategory(moduleElectionMock, ModuleCategory.CONTEXT_MODULE));
        assertEquals(NUM_INTERDISCIPLINARY_MODULES, validator.countModuleCategory(moduleElectionMock, ModuleCategory.INTERDISCIPLINARY_MODULE));
        assertEquals(NUM_SUBJECT_MODULES, validator.countModuleCategory(moduleElectionMock, ModuleCategory.SUBJECT_MODULE));
    }

    @Test
    void testIsOverflownEmpty() {
        var overflownModules = Set.of(mock(Module.class));
        when(moduleElectionMock.getOverflowedElectedModules()).thenReturn(overflownModules);
        assertFalse(validator.isOverflownEmpty(moduleElectionMock));

        when(moduleElectionMock.getOverflowedElectedModules()).thenReturn(null);
        assertTrue(validator.isOverflownEmpty(moduleElectionMock));

        when(moduleElectionMock.getOverflowedElectedModules()).thenReturn(new HashSet<>());
        assertTrue(validator.isOverflownEmpty(moduleElectionMock));
    }

    /* **************************************************************************************************************
     * Negative tests
     * ************************************************************************************************************** */

    @Test
    void testValidConsecutiveModulePairsInElection_Null() {
        assertThrows(NullPointerException.class, () -> validator.validConsecutiveModulePairsInElection(null));
    }

    @Test
    void testValidConsecutiveModulePairsInElection_NullElectionSet() {
        when(moduleElectionMock.getElectedModules()).thenReturn(null);
        assertThrows(NullPointerException.class, () -> validator.validConsecutiveModulePairsInElection(moduleElectionMock));
    }

    @Test
    void testAreModulesConsecutive_NullModule1() {
        var moduleMock = mock(Module.class);
        when(moduleMock.getConsecutiveModuleNo()).thenReturn(CONSECUTIVE_VALUE);
        assertThrows(NullPointerException.class, () -> validator.areModulesConsecutive(null, moduleMock));
    }

    @Test
    void testAreModulesConsecutive_NullModule2() {
        var moduleMock = mock(Module.class);
        when(moduleMock.getConsecutiveModuleNo()).thenReturn(CONSECUTIVE_VALUE);
        assertThrows(NullPointerException.class, () -> validator.areModulesConsecutive(moduleMock, null));
    }

    @Test
    void testContainsModule_NullSet() {
        assertThrows(NullPointerException.class, () -> validator.containsModule(null, ""));
    }

    @Test
    void testContainsModule_NullModuleNo() {
        assertThrows(NullPointerException.class, () -> validator.containsModule(validElectionSet, null));
    }

    @Test
    void testContainsModule_SetModuleNoNull() {
        var moduleMock = mock(Module.class);
        assertDoesNotThrow(() -> validator.containsModule(Set.of(moduleMock), ""));
    }

    @Test
    void testContainsModule_BlankModuleNo() {
        assertDoesNotThrow(() -> validator.containsModule(validElectionSet, ""));
    }

    @Test
    void testCountModuleCategory_NullElection() {
        assertThrows(NullPointerException.class, () -> validator.countModuleCategory(null, ModuleCategory.CONTEXT_MODULE));
    }

    @Test
    void testCountModuleCategory_NullElectionSet() {
        var set = new HashSet<Module>();
        set.add(null);
        when(moduleElectionMock.getElectedModules()).thenReturn(set);
        assertThrows(NullPointerException.class, () -> validator.countModuleCategory(moduleElectionMock, ModuleCategory.CONTEXT_MODULE));
    }

    @Test
    void testCountModuleCategory_NullCategory() {
        when(moduleElectionMock.getElectedModules()).thenReturn(validElectionSet);
        assertEquals(0, validator.countModuleCategory(moduleElectionMock, null));
    }

    @Test
    void testIsOverflownEmpty_Null() {
        assertThrows(NullPointerException.class, () -> validator.isOverflownEmpty(null));
    }

    /* **************************************************************************************************************
     * Helper methods
     * ************************************************************************************************************** */

    List<Module> generateModuleMockSet(int numElements) {
        var list = new ArrayList<Module>();
        for (var i = 0; i < numElements; i++) {
            list.add(mock(Module.class));
        }
        return list;
    }

    Set<Module> generateValidElectionSet() {
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

    void removeNonConsecutiveSubjectModulesFromSet(Set<Module> set) {
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

    void removeEnglishModules(Set<Module> set) {
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

    void addModule(Set<Module> set, String moduleNo, Module module, int credits) {
        when(module.getModuleNo()).thenReturn(moduleNo);
        when(module.getCredits()).thenReturn((byte) credits);
        set.add(module);
    }

    void removeOneModuleByCategory(Set<Module> set, ModuleCategory moduleCategory) {
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
