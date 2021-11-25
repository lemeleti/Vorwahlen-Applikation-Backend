package ch.zhaw.vorwahlen.modulevalidation;

import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.model.modules.ModuleCategory;
import ch.zhaw.vorwahlen.model.modules.ModuleElection;
import ch.zhaw.vorwahlen.modules.ModuleCategoryTest;
import ch.zhaw.vorwahlen.parser.ModuleParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PartTimeElectionValidatorTest extends AbstractElectionValidatorTest {

    public static final String SEMESTER_6_AND_8 = "6;8";
    public static final String SEMESTER_5_AND_6 = "5;6";
    public static final String SEMESTER_5 = "5.0";
    public static final String SEMESTER_7 = "7.0";
    public static final int NUM_NON_CONSECUTIVE_SUBJECT_MODULES = 2;

    @Override
    @BeforeEach
    void setUp() {
        super.setUp();
        validator = new PartTimeElectionValidator(studentMock);
    }

    /* **************************************************************************************************************
     * Positive tests
     * ************************************************************************************************************** */

    @Test
    void testValidateElectionPartTime() {
        var isFistElection = true;
        validElectionSet = generateValidPartTimeElectionSet(isFistElection);
        when(studentMock.isSecondElection()).thenReturn(!isFistElection);
        testValidateElectionPartTimeCheck(isFistElection);

        isFistElection = false;
        validElectionSet = generateValidPartTimeElectionSet(isFistElection);
        when(studentMock.isSecondElection()).thenReturn(!isFistElection);
        testValidateElectionPartTimeCheck(isFistElection);
    }

    void testValidateElectionPartTimeCheck(boolean isFistElection){
        //===== Returns valid
        when(moduleElectionMock.getOverflowedElectedModules()).thenReturn(new HashSet<>());
        when(moduleElectionMock.getElectedModules()).thenReturn(validElectionSet);

        // Case Non-IP, No Dispensations
        when(studentMock.isTZ()).thenReturn(true);
        when(studentMock.isIP()).thenReturn(false);
        when(studentMock.getWpmDispensation()).thenReturn(0);
        assertTrue(validator.validate(moduleElectionMock));

        // Case IP, No Dispensations
        when(studentMock.isIP()).thenReturn(true);
        assertTrue(validator.validate(moduleElectionMock));

        // Case IP, Some Dispensations
        if(!isFistElection) {
            removeNonConsecutiveSubjectModulesFromSet(validElectionSet);
        }
        when(studentMock.getWpmDispensation()).thenReturn(WPM_DISPENSATION);
        assertTrue(validator.validate(moduleElectionMock));

        // Case Non-IP, Some Dispensations
        when(studentMock.isIP()).thenReturn(false);
        assertTrue(validator.validate(moduleElectionMock));

        //===== Returns invalid
        // Case Non-IP, No Dispensations (Not enough selected)
        when(studentMock.getWpmDispensation()).thenReturn(0);
        for (var mode = 1; mode < 3; mode++) {
            if(isFistElection && mode == 2) continue; // interdisciplinary count in first election is zero and valid
            assertInvalidElection(moduleElectionMock, validator, mode, isFistElection);
        }

        // Case Non-IP, No Dispensations (Too much selected)
        for (var mode = 3; mode < 5; mode++) {
            assertInvalidElection(moduleElectionMock, validator, mode, isFistElection);
        }
    }

    @Test
    void testCanModuleBeSelectedInThisRun() {
        var semesterBothElectionModuleMock = mock(Module.class);
        var semesterFirstElectionModuleMock = mock(Module.class);
        var semester5ModuleMock = mock(Module.class);
        var semester7ModuleMock = mock(Module.class);

        when(semesterBothElectionModuleMock.getPartTimeSemester()).thenReturn(SEMESTER_6_AND_8);
        when(semesterFirstElectionModuleMock.getPartTimeSemester()).thenReturn(SEMESTER_5_AND_6);
        when(semester5ModuleMock.getPartTimeSemester()).thenReturn(SEMESTER_5);
        when(semester7ModuleMock.getPartTimeSemester()).thenReturn(SEMESTER_7);

        // first election
        when(studentMock.isSecondElection()).thenReturn(false);
        var set = Set.of(semesterBothElectionModuleMock, semesterFirstElectionModuleMock, semester5ModuleMock);
        when(moduleElectionMock.getElectedModules()).thenReturn(set);
        assertTrue(((PartTimeElectionValidator) validator).canModuleBeSelectedInThisRun(moduleElectionMock));

        set = Set.of(semester7ModuleMock);
        when(moduleElectionMock.getElectedModules()).thenReturn(set);
        assertFalse(((PartTimeElectionValidator) validator).canModuleBeSelectedInThisRun(moduleElectionMock));

        // second election
        when(studentMock.isSecondElection()).thenReturn(true);
        set = Set.of(semesterBothElectionModuleMock, semester7ModuleMock);
        when(moduleElectionMock.getElectedModules()).thenReturn(set);
        assertTrue(((PartTimeElectionValidator) validator).canModuleBeSelectedInThisRun(moduleElectionMock));

        set = Set.of(semesterFirstElectionModuleMock);
        when(moduleElectionMock.getElectedModules()).thenReturn(set);
        assertFalse(((PartTimeElectionValidator) validator).canModuleBeSelectedInThisRun(moduleElectionMock));

        set = Set.of(semester5ModuleMock);
        when(moduleElectionMock.getElectedModules()).thenReturn(set);
        assertFalse(((PartTimeElectionValidator) validator).canModuleBeSelectedInThisRun(moduleElectionMock));
    }

    @Override
    @Test
    void testValidConsecutiveModulePairsInElection() {
        // first election
        when(studentMock.isSecondElection()).thenReturn(false);
        when(moduleElectionMock.getElectedModules()).thenReturn(validElectionSet);
        assertTrue(validator.validConsecutiveModulePairsInElection(moduleElectionMock));

        // second election
        when(studentMock.isSecondElection()).thenReturn(true);
        // AI 1
        var m1 = mock(Module.class);
        when(m1.getConsecutiveModuleNo()).thenReturn(consecutiveSubjectModules.get(1));
        when(m1.getShortModuleNo()).thenReturn(consecutiveSubjectModulesShort.get(1));

        // AI 2
        var m2 = mock(Module.class);
        when(m2.getConsecutiveModuleNo()).thenReturn(consecutiveSubjectModules.get(0));
        when(m2.getShortModuleNo()).thenReturn(consecutiveSubjectModulesShort.get(0));

        // FUP
        var m3 = mock(Module.class);
        when(m3.getShortModuleNo()).thenReturn(subjectModulesShort.get(1));

        // PSPP
        var m4 = mock(Module.class);
        when(m4.getShortModuleNo()).thenReturn(MODULE_WV_PSPP);

        // AI1, AI2
        when(moduleElectionMock.getElectedModules()).thenReturn(Set.of(m1, m2));
        assertTrue(validator.validConsecutiveModulePairsInElection(moduleElectionMock));

        // FUP, PSPP
        when(moduleElectionMock.getElectedModules()).thenReturn(Set.of(m3, m4));
        assertTrue(validator.validConsecutiveModulePairsInElection(moduleElectionMock));

        // PSPP
        when(moduleElectionMock.getElectedModules()).thenReturn(Set.of(m4));
        assertFalse(validator.validConsecutiveModulePairsInElection(moduleElectionMock));
    }

    @Test
    void testValidInterdisciplinaryModuleElection() {
        // first election
        when(studentMock.isSecondElection()).thenReturn(false);
        var m1 = mock(Module.class);
        var m2 = mock(Module.class);

        var allMocksList = List.of(m1, m2);
        for (Module mock : allMocksList) {
            when(mock.getModuleGroup()).thenReturn(ModuleParser.MODULE_GROUP_IT_6);
            when(mock.getModuleNo()).thenReturn(interdisciplinaryModules.get(0));
            when(mock.getShortModuleNo()).thenReturn(interdisciplinaryModulesShort.get(0));
        }

        when(moduleElectionMock.getElectedModules()).thenReturn(new HashSet<>());
        assertTrue(validator.validInterdisciplinaryModuleElection(moduleElectionMock));

        when(moduleElectionMock.getElectedModules()).thenReturn(Set.of(m1));
        assertFalse(validator.validInterdisciplinaryModuleElection(moduleElectionMock));

        // second election
        when(studentMock.isSecondElection()).thenReturn(true);

        when(moduleElectionMock.getElectedModules()).thenReturn(new HashSet<>());
        assertFalse(validator.validInterdisciplinaryModuleElection(moduleElectionMock));

        when(moduleElectionMock.getElectedModules()).thenReturn(Set.of(m1));
        assertTrue(validator.validInterdisciplinaryModuleElection(moduleElectionMock));

        when(moduleElectionMock.getElectedModules()).thenReturn(new HashSet<>(allMocksList));
        assertFalse(validator.validInterdisciplinaryModuleElection(moduleElectionMock));
    }

    @Test
    void testValidSubjectModuleElection() {
        // first election
        when(studentMock.isSecondElection()).thenReturn(false);
        var m1 = mock(Module.class);
        var m2 = mock(Module.class);
        var m3 = mock(Module.class);
        var m4 = mock(Module.class);
        var m5 = mock(Module.class);
        var m6 = mock(Module.class);
        var m7 = mock(Module.class);

        var allMocksList = new ArrayList<Module>();
        allMocksList.add(m1);
        allMocksList.add(m2);
        allMocksList.add(m3);
        allMocksList.add(m4);
        allMocksList.add(m5);
        allMocksList.add(m6);
        allMocksList.add(m7);

        for (Module mock : allMocksList) {
            when(mock.getModuleGroup()).thenReturn(ModuleParser.MODULE_GROUP_IT_6);
            when(mock.getModuleNo()).thenReturn(subjectModules.get(0));
            when(mock.getShortModuleNo()).thenReturn(subjectModulesShort.get(0));
        }

        when(moduleElectionMock.getElectedModules()).thenReturn(Set.of(m1, m2));
        assertTrue(validator.validSubjectModuleElection(moduleElectionMock));

        when(moduleElectionMock.getElectedModules()).thenReturn(Set.of(m1));
        assertFalse(validator.validSubjectModuleElection(moduleElectionMock));

        when(moduleElectionMock.getElectedModules()).thenReturn(Set.of(m1, m2, m3));
        assertFalse(validator.validSubjectModuleElection(moduleElectionMock));

        // second election
        when(studentMock.isSecondElection()).thenReturn(true);

        // one too much
        when(moduleElectionMock.getElectedModules()).thenReturn(new HashSet<>(allMocksList));
        assertFalse(validator.validSubjectModuleElection(moduleElectionMock));

        // exact amount needed
        allMocksList.remove(0);
        when(moduleElectionMock.getElectedModules()).thenReturn(new HashSet<>(allMocksList));
        assertTrue(validator.validSubjectModuleElection(moduleElectionMock));

        // one missing
        allMocksList.remove(0);
        when(moduleElectionMock.getElectedModules()).thenReturn(new HashSet<>(allMocksList));
        assertFalse(validator.validSubjectModuleElection(moduleElectionMock));
    }

    @Test
    void testValidContextModuleElection() {
        // first election
        when(studentMock.isSecondElection()).thenReturn(false);
        var m1 = mock(Module.class);
        var m2 = mock(Module.class);
        var m3 = mock(Module.class);

        var allMocksList = List.of(m1, m2, m3);
        for (Module mock : allMocksList) {
            when(mock.getModuleGroup()).thenReturn(ModuleParser.MODULE_GROUP_IT_5);
            when(mock.getModuleNo()).thenReturn(contextModules.get(0));
            when(mock.getShortModuleNo()).thenReturn(contextModules.get(0));
        }

        when(moduleElectionMock.getElectedModules()).thenReturn(Set.of());
        assertTrue(validator.validContextModuleElection(moduleElectionMock));

        when(moduleElectionMock.getElectedModules()).thenReturn(Set.of(m1));
        assertTrue(validator.validContextModuleElection(moduleElectionMock));

        when(moduleElectionMock.getElectedModules()).thenReturn(Set.of(m1, m2));
        assertTrue(validator.validContextModuleElection(moduleElectionMock));

        when(moduleElectionMock.getElectedModules()).thenReturn(Set.of(m1, m2, m3));
        assertTrue(validator.validContextModuleElection(moduleElectionMock));

        // second election
        when(studentMock.isSecondElection()).thenReturn(true);
        when(moduleElectionMock.getElectedModules()).thenReturn(Set.of());
        assertTrue(validator.validContextModuleElection(moduleElectionMock));

        when(moduleElectionMock.getElectedModules()).thenReturn(Set.of(m1));
        assertTrue(validator.validContextModuleElection(moduleElectionMock));

        when(moduleElectionMock.getElectedModules()).thenReturn(Set.of(m1, m2));
        assertTrue(validator.validContextModuleElection(moduleElectionMock));

        when(moduleElectionMock.getElectedModules()).thenReturn(Set.of(m1, m2, m3));
        assertTrue(validator.validContextModuleElection(moduleElectionMock));
    }

    @Test
    void testIsCreditSumValid() {
        var contextMocks = generateModuleMockSet(PartTimeElectionValidator.NUM_CONTEXT_MODULES_FIRST_ELECTION
                                                                 + PartTimeElectionValidator.NUM_CONTEXT_MODULES_SECOND_ELECTION);

        var interdisciplinaryMocks = generateModuleMockSet(PartTimeElectionValidator.NUM_INTERDISCIPLINARY_MODULES_FIRST_ELECTION
                                                                   + PartTimeElectionValidator.NUM_INTERDISCIPLINARY_MODULES_SECOND_ELECTION);

        var subjectMocks = generateModuleMockSet(PartTimeElectionValidator.NUM_SUBJECT_MODULES_FIRST_ELECTION
                                                         + PartTimeElectionValidator.NUM_SUBJECT_MODULES_SECOND_ELECTION);

        for (var context: contextMocks) {
            when(context.getCredits()).thenReturn((byte) CREDITS_PER_CONTEXT_MODULE);
        }

        for (var interdisciplinary: interdisciplinaryMocks) {
            when(interdisciplinary.getCredits()).thenReturn((byte) CREDITS_PER_INTERDISCIPLINARY_MODULE);
        }

        for (var subject: subjectMocks) {
            when(subject.getCredits()).thenReturn((byte) CREDITS_PER_SUBJECT_MODULE);
        }

        // first election
        when(studentMock.isSecondElection()).thenReturn(false);

        // without dispensation
        var set = new HashSet<Module>();
        set.add(subjectMocks.get(0));
        when(moduleElectionMock.getElectedModules()).thenReturn(set);
        assertFalse(validator.isCreditSumValid(moduleElectionMock));

        set.add(subjectMocks.get(1));
        assertTrue(validator.isCreditSumValid(moduleElectionMock));

        set.add(contextMocks.get(0));
        assertTrue(validator.isCreditSumValid(moduleElectionMock));
        set.add(contextMocks.get(1));
        assertTrue(validator.isCreditSumValid(moduleElectionMock));
        set.add(contextMocks.get(2));
        assertTrue(validator.isCreditSumValid(moduleElectionMock));

        set.add(subjectMocks.get(2));
        assertFalse(validator.isCreditSumValid(moduleElectionMock));

        // with dispensation
        when(studentMock.getWpmDispensation()).thenReturn(WPM_DISPENSATION);
        set = new HashSet<>();
        set.add(contextMocks.get(0));
        when(moduleElectionMock.getElectedModules()).thenReturn(set);
        assertFalse(validator.isCreditSumValid(moduleElectionMock));

        set.add(subjectMocks.get(0));
        assertFalse(validator.isCreditSumValid(moduleElectionMock));

        set.add(subjectMocks.get(1));
        assertTrue(validator.isCreditSumValid(moduleElectionMock));

        set.add(contextMocks.get(1));
        assertTrue(validator.isCreditSumValid(moduleElectionMock));

        set.add(contextMocks.get(2));
        assertTrue(validator.isCreditSumValid(moduleElectionMock));

        // second election
        when(studentMock.isSecondElection()).thenReturn(true);

        // without dispensation
        when(studentMock.getWpmDispensation()).thenReturn(0);

        set = new HashSet<>();
        set.add(interdisciplinaryMocks.get(0));
        set.add(subjectMocks.get(2));
        set.add(subjectMocks.get(3));
        set.add(subjectMocks.get(4));
        set.add(subjectMocks.get(5));
        set.add(subjectMocks.get(6));
        when(moduleElectionMock.getElectedModules()).thenReturn(set);
        assertFalse(validator.isCreditSumValid(moduleElectionMock));

        set.add(subjectMocks.get(7));
        assertTrue(validator.isCreditSumValid(moduleElectionMock));

        set.add(contextMocks.get(0));
        assertTrue(validator.isCreditSumValid(moduleElectionMock));
        set.add(contextMocks.get(1));
        assertTrue(validator.isCreditSumValid(moduleElectionMock));
        set.add(contextMocks.get(2));
        assertTrue(validator.isCreditSumValid(moduleElectionMock));

        set.add(subjectMocks.get(0));
        assertFalse(validator.isCreditSumValid(moduleElectionMock));

        // with dispensation
        when(studentMock.getWpmDispensation()).thenReturn(WPM_DISPENSATION);

        set = new HashSet<>();
        set.add(interdisciplinaryMocks.get(0));
        set.add(subjectMocks.get(2));
        set.add(subjectMocks.get(3));
        set.add(subjectMocks.get(4));
        when(moduleElectionMock.getElectedModules()).thenReturn(set);
        assertFalse(validator.isCreditSumValid(moduleElectionMock));

        set.add(subjectMocks.get(5));
        assertTrue(validator.isCreditSumValid(moduleElectionMock));

        set.add(contextMocks.get(0));
        assertTrue(validator.isCreditSumValid(moduleElectionMock));
        set.add(contextMocks.get(1));
        assertTrue(validator.isCreditSumValid(moduleElectionMock));
        set.add(contextMocks.get(2));
        assertTrue(validator.isCreditSumValid(moduleElectionMock));

        set.add(subjectMocks.get(0));
        assertFalse(validator.isCreditSumValid(moduleElectionMock));
    }

    /* **************************************************************************************************************
     * Negative tests
     * ************************************************************************************************************** */

    @Test
    void testValidIpModuleElection_Null() {
        when(studentMock.isIP()).thenReturn(true);
        assertTrue(validator.validIpModuleElection(null));
    }

    @Test
    void testValidIpModuleElection_NullElection() {
        when(studentMock.isIP()).thenReturn(true);
        when(moduleElectionMock.getElectedModules()).thenReturn(null);
        assertTrue(validator.validIpModuleElection(null));
    }

    @Test
    void testValidIpModuleElection_NullStudent() {
        validator = new PartTimeElectionValidator(null);
        assertTrue(validator.validIpModuleElection(moduleElectionMock));
    }

    @Test
    void testValidInterdisciplinaryModuleElection_Null() {
        assertThrows(NullPointerException.class, () -> validator.validInterdisciplinaryModuleElection(null));
    }

    @Test
    void testValidSubjectModuleElection_Null() {
        when(studentMock.getWpmDispensation()).thenReturn(0);
        assertThrows(NullPointerException.class, () -> validator.validSubjectModuleElection(null));
    }

    @Test
    void testValidSubjectModuleElection_NullStudent() {
        validator = new PartTimeElectionValidator(null);
        assertThrows(NullPointerException.class, () -> validator.validSubjectModuleElection(moduleElectionMock));
    }

    @Test
    void testValidContextModuleElection_Null() {
        assertThrows(NullPointerException.class, () -> validator.validContextModuleElection(null));
    }

    @Override
    @Test
    void testIsCreditSumValid_NullArgument() {
        assertThrows(NullPointerException.class, () -> validator.isCreditSumValid(null));
    }

    @Override
    @Test
    void testIsCreditSumValid_NullElectionSet() {
        when(moduleElectionMock.getElectedModules()).thenReturn(null);
        assertThrows(NullPointerException.class, () -> validator.isCreditSumValid(moduleElectionMock));
    }

    @Test
    void testIsCreditSumValid_NullElection() {
        var set = new HashSet<Module>();
        set.add(null);
        when(moduleElectionMock.getElectedModules()).thenReturn(set);
        assertThrows(NullPointerException.class, () -> validator.isCreditSumValid(moduleElectionMock));
    }

    @Test
    void testIsCreditSumValid_NullStudent() {
        validator = new FullTimeElectionValidator(null);

        when(studentMock.getWpmDispensation()).thenReturn(WPM_DISPENSATION);
        when(moduleElectionMock.getElectedModules()).thenReturn(validElectionSet);

        assertThrows(NullPointerException.class, () -> validator.isCreditSumValid(moduleElectionMock));
    }

    /* **************************************************************************************************************
     * Helper methods
     * ************************************************************************************************************** */

    Set<Module> generateValidPartTimeElectionSet(boolean isFirstElection) {
        var contextMocks = generateModuleMockSet(isFirstElection
                                                         ? PartTimeElectionValidator.NUM_CONTEXT_MODULES_FIRST_ELECTION
                                                         : PartTimeElectionValidator.NUM_CONTEXT_MODULES_SECOND_ELECTION);

        var interdisciplinaryMocks = generateModuleMockSet(isFirstElection
                                                                   ? PartTimeElectionValidator.NUM_INTERDISCIPLINARY_MODULES_FIRST_ELECTION
                                                                   : PartTimeElectionValidator.NUM_INTERDISCIPLINARY_MODULES_SECOND_ELECTION);

        var subjectMocks = generateModuleMockSet(isFirstElection
                                                         ? PartTimeElectionValidator.NUM_SUBJECT_MODULES_FIRST_ELECTION
                                                         : PartTimeElectionValidator.NUM_SUBJECT_MODULES_SECOND_ELECTION);

        var i = 0;
        for (var context: contextMocks) {
            when(context.getPartTimeSemester()).thenReturn((i % 3 == 0) ? SEMESTER_6_AND_8 : SEMESTER_5);
            when(context.getCredits()).thenReturn((byte) CREDITS_PER_CONTEXT_MODULE);
            when(context.getLanguage()).thenReturn(LANGUAGE_ENGLISCH);
            when(context.getModuleGroup()).thenReturn(ModuleParser.MODULE_GROUP_IT_5);
            when(context.getModuleNo()).thenReturn(contextModules.get(i));
            when(context.getShortModuleNo()).thenReturn(contextModulesShort.get(i));
            i++;
        }

        for (var interdisciplinary: interdisciplinaryMocks) {
            when(interdisciplinary.getPartTimeSemester()).thenReturn(SEMESTER_7);
            when(interdisciplinary.getCredits()).thenReturn((byte) CREDITS_PER_INTERDISCIPLINARY_MODULE);
            when(interdisciplinary.getLanguage()).thenReturn(LANGUAGE_DEUTSCH);
            when(interdisciplinary.getModuleGroup()).thenReturn(ModuleParser.MODULE_GROUP_IT_6);
            when(interdisciplinary.getModuleNo()).thenReturn(interdisciplinaryModules.get(0));
            when(interdisciplinary.getShortModuleNo()).thenReturn(interdisciplinaryModulesShort.get(0));
        }

        i = 0;
        for (var subject: subjectMocks) {
            when(subject.getPartTimeSemester()).thenReturn(SEMESTER_6_AND_8);
            when(subject.getCredits()).thenReturn((byte) CREDITS_PER_SUBJECT_MODULE);
            when(subject.getLanguage()).thenReturn(LANGUAGE_ENGLISCH);
            when(subject.getModuleGroup()).thenReturn(ModuleParser.MODULE_GROUP_IT_6);
            when(subject.getModuleNo()).thenReturn(consecutiveSubjectModules.get(i));
            when(subject.getShortModuleNo()).thenReturn(consecutiveSubjectModulesShort.get(i));
            i++;
        }

        var mockCounter = 0;
        for (i = 0; i < NUM_NON_CONSECUTIVE_SUBJECT_MODULES; i++) {
            var mock = subjectMocks.get(mockCounter);
            mockCounter++;
            when(mock.getCredits()).thenReturn((byte) CREDITS_PER_SUBJECT_MODULE);
            when(mock.getLanguage()).thenReturn(LANGUAGE_ENGLISCH);
            when(mock.getModuleGroup()).thenReturn(ModuleParser.MODULE_GROUP_IT_6);
            when(mock.getModuleNo()).thenReturn(subjectModules.get(i));
            when(mock.getShortModuleNo()).thenReturn(subjectModulesShort.get(i));
        }


        mockCounter = 0;
        for (i = NUM_NON_CONSECUTIVE_SUBJECT_MODULES + 1; i < subjectMocks.size(); i += 2) {
            var mock1 = subjectMocks.get(mockCounter);
            mockCounter++;
            when(mock1.getPartTimeSemester()).thenReturn(SEMESTER_6_AND_8);
            when(mock1.getCredits()).thenReturn((byte) CREDITS_PER_SUBJECT_MODULE);
            when(mock1.getLanguage()).thenReturn(LANGUAGE_ENGLISCH);
            when(mock1.getModuleGroup()).thenReturn(ModuleParser.MODULE_GROUP_IT_6);
            when(mock1.getShortModuleNo()).thenReturn(consecutiveSubjectModulesShort.get(i - 1));
            when(mock1.getModuleNo()).thenReturn(consecutiveSubjectModules.get(i - 1));
            when(mock1.getConsecutiveModuleNo()).thenReturn(consecutiveSubjectModules.get(i));

            var mock2 = subjectMocks.get(mockCounter);
            mockCounter++;
            when(mock2.getPartTimeSemester()).thenReturn(SEMESTER_6_AND_8);
            when(mock2.getCredits()).thenReturn((byte) CREDITS_PER_SUBJECT_MODULE);
            when(mock2.getLanguage()).thenReturn(LANGUAGE_ENGLISCH);
            when(mock2.getModuleGroup()).thenReturn(ModuleParser.MODULE_GROUP_IT_6);
            when(mock2.getShortModuleNo()).thenReturn(consecutiveSubjectModulesShort.get(i));
            when(mock2.getModuleNo()).thenReturn(consecutiveSubjectModules.get(i));
            when(mock2.getConsecutiveModuleNo()).thenReturn(consecutiveSubjectModules.get(i - 1));
        }

        var set = new HashSet<Module>();
        set.addAll(contextMocks);
        set.addAll(interdisciplinaryMocks);
        set.addAll(subjectMocks);
        return set;
    }

    @Override
    void addModule(Set<Module> set, String moduleNo, Module module, int credits) {
        when(module.getPartTimeSemester()).thenReturn(SEMESTER_6_AND_8);
        super.addModule(set, moduleNo, module, credits);
    }

    Set<Module> invalidElectionSet(int mode, boolean isFistElection) {
        var set = generateValidPartTimeElectionSet(isFistElection);
        var module = mock(Module.class);
        switch (mode) {
            case 1 -> removeOneModuleByCategory(set, ModuleCategory.SUBJECT_MODULE);
            case 2 -> removeOneModuleByCategory(set, ModuleCategory.INTERDISCIPLINARY_MODULE);
            case 3 -> addModule(set, ModuleCategoryTest.possibleSubjectPrefixes.get(0), module, CREDITS_PER_SUBJECT_MODULE);
            case 4 -> addModule(set, ModuleCategoryTest.INTERDISCIPLINARY_PREFIX_WM, module, CREDITS_PER_INTERDISCIPLINARY_MODULE);
        }
        return set;
    }

    void assertInvalidElection(ModuleElection moduleElectionMock, ElectionValidator validator, int mode, boolean isFistElection) {
        var invalidElection = invalidElectionSet(mode, isFistElection);
        when(moduleElectionMock.getElectedModules()).thenReturn(invalidElection);
        assertFalse(validator.validate(moduleElectionMock));
    }
}
