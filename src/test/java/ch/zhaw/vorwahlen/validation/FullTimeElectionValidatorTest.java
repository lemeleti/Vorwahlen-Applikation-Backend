package ch.zhaw.vorwahlen.validation;

import ch.zhaw.vorwahlen.model.core.module.Module;
import ch.zhaw.vorwahlen.model.core.module.ModuleCategory;
import ch.zhaw.vorwahlen.model.core.election.Election;
import ch.zhaw.vorwahlen.model.core.validationsetting.ValidationSetting;
import ch.zhaw.vorwahlen.modules.ModuleCategoryTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FullTimeElectionValidatorTest extends AbstractElectionValidatorTest {

    @Override
    @BeforeEach
    void setUp() {
        super.setUp();
        validator = new FullTimeElectionValidator(studentMock);
    }

    /* **************************************************************************************************************
     * Positive tests
     * ************************************************************************************************************** */

    @Test
    void testValidateElectionFullTime() {
        //===== Returns valid
        var validationSettingMock = mock(ValidationSetting.class);
        when(validationSettingMock.isRepetent()).thenReturn(true);
        when(electionMock.getValidationSetting()).thenReturn(validationSettingMock);
        assertTrue(validator.validate(electionMock).isValid());

        when(validationSettingMock.isRepetent()).thenReturn(false);
        when(electionMock.getElectedModules()).thenReturn(validElectionSet);

        // Case Non-IP, No Dispensations
        when(studentMock.isTZ()).thenReturn(false);
        when(studentMock.isIP()).thenReturn(false);
        when(studentMock.getWpmDispensation()).thenReturn(0);
        assertTrue(validator.validate(electionMock).isValid());

        // Case IP, No Dispensations
        when(studentMock.isIP()).thenReturn(true);
        assertTrue(validator.validate(electionMock).isValid());

        // Case IP, Some Dispensations
        removeNonConsecutiveSubjectModulesFromSet(validElectionSet);
        when(studentMock.getWpmDispensation()).thenReturn(WPM_DISPENSATION);
        assertTrue(validator.validate(electionMock).isValid());

        // Case Non-IP, Some Dispensations
        when(studentMock.isIP()).thenReturn(false);
        assertTrue(validator.validate(electionMock).isValid());

        //===== Returns invalid
        // Case Non-IP, No Dispensations (Not enough selected)
        when(studentMock.getWpmDispensation()).thenReturn(0);
        for (var mode = 1; mode < 4; mode++) {
            assertInvalidElection(electionMock, validator, mode);
        }

        // Case Non-IP, No Dispensations (Too much selected)
        for (var mode = 4; mode < 7; mode++) {
            assertInvalidElection(electionMock, validator, mode);
        }

        // Case IP, No Dispensations (Not enough english selected)
        assertInvalidElection(electionMock, validator, 7);
    }

    @Override
    @Test
    void testValidConsecutiveModulePairsInElection() {
        when(electionMock.getElectedModules()).thenReturn(validElectionSet);
        when(electionMock.getValidationSetting()).thenReturn(new ValidationSetting());
        assertTrue(validator.validConsecutiveModulePairsInElection(electionMock));

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
        when(electionMock.getElectedModules()).thenReturn(Set.of(m1, m2, m3, m4));
        assertTrue(validator.validConsecutiveModulePairsInElection(electionMock));

        // AI1, AI2, PSPP
        when(electionMock.getElectedModules()).thenReturn(Set.of(m1, m2, m4));
        assertFalse(validator.validConsecutiveModulePairsInElection(electionMock));
    }

    @Test
    void testValidIpElection() {
        // Case Non-IP
        when(studentMock.isIP()).thenReturn(false);
        assertTrue(validator.validIpElection(electionMock));

        //------------------------------------------------------------------
        // Case IP, ...
        when(electionMock.getElectedModules()).thenReturn(validElectionSet);
        when(studentMock.isIP()).thenReturn(true);

        // ... No Dispensations
        when(studentMock.getWpmDispensation()).thenReturn(0);
        assertTrue(validator.validIpElection(electionMock));

        removeEnglishModules(validElectionSet);
        assertFalse(validator.validIpElection(electionMock));

        // ... Some Dispensations
        when(studentMock.getWpmDispensation()).thenReturn(WPM_DISPENSATION);
        assertFalse(validator.validIpElection(electionMock));
    }

    @Test
    void testValidInterdisciplinaryElection() {
        // valid
        when(electionMock.getElectedModules()).thenReturn(validElectionSet);
        assertTrue(validator.validInterdisciplinaryElection(electionMock));

        // too less
        removeOneModuleByCategory(validElectionSet, ModuleCategory.INTERDISCIPLINARY_MODULE);
        assertFalse(validator.validInterdisciplinaryElection(electionMock));

        // too much
        validElectionSet = generateValidElectionSet();
        addModule(validElectionSet, ModuleCategoryTest.INTERDISCIPLINARY_PREFIX_WM, mock(Module.class), CREDITS_PER_INTERDISCIPLINARY_MODULE);
        assertFalse(validator.validInterdisciplinaryElection(electionMock));
    }

    @Test
    void testValidSubjectElection() {
        // valid
        when(electionMock.getElectedModules()).thenReturn(validElectionSet);
        assertTrue(validator.validSubjectElection(electionMock));

        // too less
        removeOneModuleByCategory(validElectionSet, ModuleCategory.SUBJECT_MODULE);
        assertFalse(validator.validSubjectElection(electionMock));

        // too much
        validElectionSet = generateValidElectionSet();
        addModule(validElectionSet, ModuleCategoryTest.possibleSubjectPrefixes.get(0), mock(Module.class), CREDITS_PER_SUBJECT_MODULE);
        assertFalse(validator.validSubjectElection(electionMock));
    }

    @Test
    void testValidContextElection() {
        // valid
        when(electionMock.getElectedModules()).thenReturn(validElectionSet);
        assertTrue(validator.validContextElection(electionMock));

        // too less
        removeOneModuleByCategory(validElectionSet, ModuleCategory.CONTEXT_MODULE);
        assertFalse(validator.validContextElection(electionMock));

        // too much
        validElectionSet = generateValidElectionSet();
        addModule(validElectionSet, ModuleCategoryTest.possibleContextPrefixes.get(0), mock(Module.class), CREDITS_PER_CONTEXT_MODULE);
        assertFalse(validator.validContextElection(electionMock));
    }

    @Test
    void testIsCreditSumValid() {
        //--- Case No Dispensations
        when(studentMock.getWpmDispensation()).thenReturn(0);
        when(electionMock.getElectedModules()).thenReturn(validElectionSet);
        assertTrue(validator.isCreditSumValid(electionMock));

        //--- Case Some Dispensations
        removeNonConsecutiveSubjectModulesFromSet(validElectionSet);
        when(studentMock.getWpmDispensation()).thenReturn(WPM_DISPENSATION);
        when(electionMock.getElectedModules()).thenReturn(validElectionSet);
        assertTrue(validator.isCreditSumValid(electionMock));

        // More modules selected considering the dispensations
        when(studentMock.getWpmDispensation()).thenReturn(WPM_DISPENSATION + 1);
        assertFalse(validator.isCreditSumValid(electionMock));

        // Not enough modules selected considering the dispensations
        when(studentMock.getWpmDispensation()).thenReturn(WPM_DISPENSATION - 1);
        assertFalse(validator.isCreditSumValid(electionMock));


        //--- Case Non-IP, No Dispensations (Not enough selected)
        when(studentMock.getWpmDispensation()).thenReturn(0);
        for (var mode = 1; mode < 4; mode++) {
            var invalidElection = invalidElectionSet(mode);
            when(electionMock.getElectedModules()).thenReturn(invalidElection);
            assertFalse(validator.isCreditSumValid(electionMock));
        }

        //--- Case Non-IP, No Dispensations (Too much selected)
        for (var mode = 4; mode < 7; mode++) {
            var invalidElection = invalidElectionSet(mode);
            when(electionMock.getElectedModules()).thenReturn(invalidElection);
            assertFalse(validator.isCreditSumValid(electionMock));
        }

        //--- Case IP, No Dispensations (Not enough english selected)
        var invalidElection = invalidElectionSet(7);
        when(electionMock.getElectedModules()).thenReturn(invalidElection);
        assertFalse(validator.isCreditSumValid(electionMock));
    }

    /* **************************************************************************************************************
     * Negative tests
     * ************************************************************************************************************** */

    @Test
    void testValidIpElection_Null() {
        when(studentMock.isIP()).thenReturn(true);
        assertThrows(NullPointerException.class, () -> validator.validIpElection(null));
    }

    @Test
    void testValidIpElection_NullElection() {
        when(studentMock.isIP()).thenReturn(true);
        when(electionMock.getElectedModules()).thenReturn(null);
        assertThrows(NullPointerException.class, () -> validator.validIpElection(electionMock));
    }

    @Test
    void testValidIpElection_NullStudent() {
        validator = new FullTimeElectionValidator(null);
        assertThrows(NullPointerException.class, () -> validator.validIpElection(electionMock));
    }

    @Test
    void testValidInterdisciplinaryElection_Null() {
        assertThrows(NullPointerException.class, () -> validator.validInterdisciplinaryElection(null));
    }

    @Test
    void testValidSubjectElection_Null() {
        when(studentMock.getWpmDispensation()).thenReturn(0);
        assertThrows(NullPointerException.class, () -> validator.validSubjectElection(null));
    }

    @Test
    void testValidSubjectElection_NullStudent() {
        validator = new FullTimeElectionValidator(null);
        assertThrows(NullPointerException.class, () -> validator.validSubjectElection(electionMock));
    }

    @Test
    void testValidContextElection_Null() {
        assertThrows(NullPointerException.class, () -> validator.validContextElection(null));
    }

    @Override
    @Test
    void testIsCreditSumValid_NullArgument() {
        assertThrows(NullPointerException.class, () -> validator.isCreditSumValid(null));
    }

    @Override
    @Test
    void testIsCreditSumValid_NullElectionSet() {
        when(electionMock.getElectedModules()).thenReturn(null);
        assertThrows(NullPointerException.class, () -> validator.isCreditSumValid(electionMock));
    }

    @Test
    void testIsCreditSumValid_NullElection() {
        var set = new HashSet<Module>();
        set.add(null);
        when(electionMock.getElectedModules()).thenReturn(set);
        assertThrows(NullPointerException.class, () -> validator.isCreditSumValid(electionMock));
    }

    @Test
    void testIsCreditSumValid_NullStudent() {
        validator = new FullTimeElectionValidator(null);

        when(studentMock.getWpmDispensation()).thenReturn(WPM_DISPENSATION);
        when(electionMock.getElectedModules()).thenReturn(validElectionSet);

        assertThrows(NullPointerException.class, () -> validator.isCreditSumValid(electionMock));
    }

    /* **************************************************************************************************************
     * Helper methods
     * ************************************************************************************************************** */

    void assertInvalidElection(Election electionMock, ElectionValidator validator, int mode) {
        var invalidElection = invalidElectionSet(mode);
        when(electionMock.getElectedModules()).thenReturn(invalidElection);
        assertFalse(validator.validate(electionMock).isValid());
    }
}
