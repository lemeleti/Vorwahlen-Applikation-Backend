package ch.zhaw.vorwahlen.modules;

import ch.zhaw.vorwahlen.model.modules.ModuleCategory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ModuleCategoryTest {

    public static final String VALID_MODULE_GROUP_IT5 = "IT5";
    public static final String VALID_MODULE_GROUP_IT6 = "IT6";

    public static final String SPECIAL_CASE_MODULE_T_BA_XX = "t.BA.XX.";

    public static final String INTERDISCIPLINARY_PREFIX_WM = "t.BA.WM.";
    public static final List<String> possibleSubjectPrefixes = List.of("t.BA.WV.", SPECIAL_CASE_MODULE_T_BA_XX);
    public static final List<String> possibleContextPrefixes = List.of("t.BA.WVK.", "t.BA.WVK.SIC", "t.BA.XXK.", SPECIAL_CASE_MODULE_T_BA_XX);

    /* **************************************************************************************************************
     * Positive tests
     * ************************************************************************************************************** */

    @Test
    void testParse() {
        assertEquals(ModuleCategory.INTERDISCIPLINARY_MODULE, ModuleCategory.parse(INTERDISCIPLINARY_PREFIX_WM, VALID_MODULE_GROUP_IT5));

        // special case "t.BA.XX." with group 5 is context
        for (var prefix: possibleContextPrefixes) {
            assertEquals(ModuleCategory.CONTEXT_MODULE, ModuleCategory.parse(prefix, VALID_MODULE_GROUP_IT5));
        }

        // special case "t.BA.XX." with group 6 is subject
        for (var prefix: possibleSubjectPrefixes) {
            assertEquals(ModuleCategory.SUBJECT_MODULE, ModuleCategory.parse(prefix, VALID_MODULE_GROUP_IT6));
        }
    }

    /* **************************************************************************************************************
     * Negative tests
     * ************************************************************************************************************** */

    @Test
    void testParse_NullModuleNo() {
        assertThrows(NullPointerException.class, () -> ModuleCategory.parse(null, VALID_MODULE_GROUP_IT6));
    }

    @Test
    void testParse_NullModuleGroup_SpecialCase_XX() {
        assertThrows(NullPointerException.class, () -> ModuleCategory.parse(SPECIAL_CASE_MODULE_T_BA_XX, null));
    }

    @Test
    void testParse_EmptyModuleNo() {
        assertNull(ModuleCategory.parse("", VALID_MODULE_GROUP_IT6));
    }

    @Test
    void testParse_EmptyModuleGroup_SpecialCase_XX() {
        assertNull(ModuleCategory.parse(SPECIAL_CASE_MODULE_T_BA_XX, ""));
    }

}
