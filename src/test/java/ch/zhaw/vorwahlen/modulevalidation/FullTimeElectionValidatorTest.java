package ch.zhaw.vorwahlen.modulevalidation;

import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.model.modules.ModuleCategory;
import ch.zhaw.vorwahlen.model.modules.ModuleElection;
import ch.zhaw.vorwahlen.modules.ModuleCategoryTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FullTimeElectionValidatorTest extends AbstractElectionValidatorTest {

    @BeforeEach
    void setUp() {
        super.setUp();
        validator = new FullTimeElectionValidator(studentMock);
    }

    /* **************************************************************************************************************
     * Positive tests
     * ************************************************************************************************************** */

    @Test
    @Sql("classpath:sql/modules_test_election.sql")
    void testValidateElectionFullTime() {
        //===== Returns valid
        when(moduleElectionMock.getOverflowedElectedModules()).thenReturn(new HashSet<>());
        when(moduleElectionMock.getElectedModules()).thenReturn(validElectionSet);

        // Case Non-IP, No Dispensations
        when(studentMock.isTZ()).thenReturn(false);
        when(studentMock.isIP()).thenReturn(false);
        when(studentMock.getWpmDispensation()).thenReturn(0);
        assertTrue(validator.validate(moduleElectionMock));

        // Case IP, No Dispensations
        when(studentMock.isIP()).thenReturn(true);
        assertTrue(validator.validate(moduleElectionMock));

        // Case IP, Some Dispensations
        removeNonConsecutiveSubjectModulesFromSet(validElectionSet);
        when(studentMock.getWpmDispensation()).thenReturn(WPM_DISPENSATION);
        assertTrue(validator.validate(moduleElectionMock));

        // Case Non-IP, Some Dispensations
        when(studentMock.isIP()).thenReturn(false);
        assertTrue(validator.validate(moduleElectionMock));

        //===== Returns invalid
        // Case Non-IP, No Dispensations (Not enough selected)
        when(studentMock.getWpmDispensation()).thenReturn(0);
        for (var mode = 1; mode < 4; mode++) {
            assertInvalidElection(moduleElectionMock, validator, mode);
        }

        // Case Non-IP, No Dispensations (Too much selected)
        for (var mode = 4; mode < 7; mode++) {
            assertInvalidElection(moduleElectionMock, validator, mode);
        }

        // Case IP, No Dispensations (Not enough english selected)
        assertInvalidElection(moduleElectionMock, validator, 7);
    }

    @Test
    void testValidIpModuleElection() {
        // Case Non-IP
        when(studentMock.isIP()).thenReturn(false);
        assertTrue(validator.validIpModuleElection(moduleElectionMock));

        //------------------------------------------------------------------
        // Case IP, ...
        when(moduleElectionMock.getElectedModules()).thenReturn(validElectionSet);
        when(studentMock.isIP()).thenReturn(true);

        // ... No Dispensations
        when(studentMock.getWpmDispensation()).thenReturn(0);
        assertTrue(validator.validIpModuleElection(moduleElectionMock));

        removeEnglishModules(validElectionSet);
        assertFalse(validator.validIpModuleElection(moduleElectionMock));

        // ... Some Dispensations
        when(studentMock.getWpmDispensation()).thenReturn(WPM_DISPENSATION);
        assertTrue(validator.validIpModuleElection(moduleElectionMock));
    }

    @Test
    void testValidInterdisciplinaryModuleElection() {
        // valid
        when(moduleElectionMock.getElectedModules()).thenReturn(validElectionSet);
        assertTrue(validator.validInterdisciplinaryModuleElection(moduleElectionMock));

        // too less
        removeOneModuleByCategory(validElectionSet, ModuleCategory.INTERDISCIPLINARY_MODULE);
        assertFalse(validator.validInterdisciplinaryModuleElection(moduleElectionMock));

        // too much
        validElectionSet = generateValidElectionSet();
        addModule(validElectionSet, ModuleCategoryTest.INTERDISCIPLINARY_PREFIX_WM, mock(Module.class), CREDITS_PER_INTERDISCIPLINARY_MODULE);
        assertFalse(validator.validInterdisciplinaryModuleElection(moduleElectionMock));
    }

    @Test
    void testValidSubjectModuleElection() {
        // valid
        when(moduleElectionMock.getElectedModules()).thenReturn(validElectionSet);
        assertTrue(validator.validSubjectModuleElection(moduleElectionMock));

        // too less
        removeOneModuleByCategory(validElectionSet, ModuleCategory.SUBJECT_MODULE);
        assertFalse(validator.validSubjectModuleElection(moduleElectionMock));

        // too much
        validElectionSet = generateValidElectionSet();
        addModule(validElectionSet, ModuleCategoryTest.possibleSubjectPrefixes.get(0), mock(Module.class), CREDITS_PER_SUBJECT_MODULE);
        assertFalse(validator.validSubjectModuleElection(moduleElectionMock));
    }

    @Test
    void testValidContextModuleElection() {
        // valid
        when(moduleElectionMock.getElectedModules()).thenReturn(validElectionSet);
        assertTrue(validator.validContextModuleElection(moduleElectionMock));

        // too less
        removeOneModuleByCategory(validElectionSet, ModuleCategory.CONTEXT_MODULE);
        assertFalse(validator.validContextModuleElection(moduleElectionMock));

        // too much
        validElectionSet = generateValidElectionSet();
        addModule(validElectionSet, ModuleCategoryTest.possibleContextPrefixes.get(0), mock(Module.class), CREDITS_PER_CONTEXT_MODULE);
        assertFalse(validator.validContextModuleElection(moduleElectionMock));
    }

    @Test
    void testIsCreditSumValid() {
        //--- Case No Dispensations
        when(studentMock.getWpmDispensation()).thenReturn(0);
        when(moduleElectionMock.getElectedModules()).thenReturn(validElectionSet);
        assertTrue(validator.isCreditSumValid(moduleElectionMock));

        //--- Case Some Dispensations
        removeNonConsecutiveSubjectModulesFromSet(validElectionSet);
        when(studentMock.getWpmDispensation()).thenReturn(WPM_DISPENSATION);
        when(moduleElectionMock.getElectedModules()).thenReturn(validElectionSet);
        assertTrue(validator.isCreditSumValid(moduleElectionMock));

        // More modules selected considering the dispensations
        when(studentMock.getWpmDispensation()).thenReturn(WPM_DISPENSATION + 1);
        assertFalse(validator.isCreditSumValid(moduleElectionMock));

        // Not enough modules selected considering the dispensations
        when(studentMock.getWpmDispensation()).thenReturn(WPM_DISPENSATION - 1);
        assertFalse(validator.isCreditSumValid(moduleElectionMock));


        //--- Case Non-IP, No Dispensations (Not enough selected)
        when(studentMock.getWpmDispensation()).thenReturn(0);
        for (var mode = 1; mode < 4; mode++) {
            var invalidElection = invalidElectionSet(mode);
            when(moduleElectionMock.getElectedModules()).thenReturn(invalidElection);
            assertFalse(validator.isCreditSumValid(moduleElectionMock));
        }

        //--- Case Non-IP, No Dispensations (Too much selected)
        for (var mode = 4; mode < 7; mode++) {
            var invalidElection = invalidElectionSet(mode);
            when(moduleElectionMock.getElectedModules()).thenReturn(invalidElection);
            assertFalse(validator.isCreditSumValid(moduleElectionMock));
        }

        //--- Case IP, No Dispensations (Not enough english selected)
        var invalidElection = invalidElectionSet(7);
        when(moduleElectionMock.getElectedModules()).thenReturn(invalidElection);
        assertFalse(validator.isCreditSumValid(moduleElectionMock));
    }

    /* **************************************************************************************************************
     * Negative tests
     * ************************************************************************************************************** */

    @Test
    void testValidIpModuleElection_Null() {
        when(studentMock.isIP()).thenReturn(true);
        assertThrows(NullPointerException.class, () -> validator.validIpModuleElection(null));
    }

    @Test
    void testValidIpModuleElection_NullElection() {
        when(studentMock.isIP()).thenReturn(true);
        when(moduleElectionMock.getElectedModules()).thenReturn(null);
        assertThrows(NullPointerException.class, () -> validator.validIpModuleElection(moduleElectionMock));
    }

    @Test
    void testValidIpModuleElection_NullStudent() {
        validator = new FullTimeElectionValidator(null);
        assertThrows(NullPointerException.class, () -> validator.validIpModuleElection(moduleElectionMock));
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
        validator = new FullTimeElectionValidator(null);
        assertThrows(NullPointerException.class, () -> validator.validSubjectModuleElection(moduleElectionMock));
    }

    @Test
    void testValidContextModuleElection_Null() {
        assertThrows(NullPointerException.class, () -> validator.validContextModuleElection(null));
    }

    @Test
    void testIsCreditSumValid_NullArgument() {
        assertThrows(NullPointerException.class, () -> validator.isCreditSumValid(null));
    }

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

    Set<Module> invalidElectionSet(int mode) {
        var set = generateValidElectionSet();
        var module = mock(Module.class);
        switch (mode) {
            case 1 -> removeOneModuleByCategory(set, ModuleCategory.CONTEXT_MODULE);
            case 2 -> removeOneModuleByCategory(set, ModuleCategory.SUBJECT_MODULE);
            case 3 -> removeOneModuleByCategory(set, ModuleCategory.INTERDISCIPLINARY_MODULE);
            case 4 -> addModule(set, ModuleCategoryTest.possibleContextPrefixes.get(0), module, CREDITS_PER_CONTEXT_MODULE);
            case 5 -> addModule(set, ModuleCategoryTest.possibleSubjectPrefixes.get(0), module, CREDITS_PER_SUBJECT_MODULE);
            case 6 -> addModule(set, ModuleCategoryTest.INTERDISCIPLINARY_PREFIX_WM, module, CREDITS_PER_INTERDISCIPLINARY_MODULE);
            case 7 -> removeEnglishModules(set);
        }
        return set;
    }

    void assertInvalidElection(ModuleElection moduleElectionMock, ElectionValidator validator, int mode) {
        var invalidElection = invalidElectionSet(mode);
        when(moduleElectionMock.getElectedModules()).thenReturn(invalidElection);
        assertFalse(validator.validate(moduleElectionMock));
    }
}
