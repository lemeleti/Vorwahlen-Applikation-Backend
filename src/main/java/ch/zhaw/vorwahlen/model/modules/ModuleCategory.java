package ch.zhaw.vorwahlen.model.modules;

import ch.zhaw.vorwahlen.config.ResourceBundleMessageLoader;
import ch.zhaw.vorwahlen.parser.ModuleParser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Categorize modules by module id.
 */
@RequiredArgsConstructor
@Getter
public enum ModuleCategory {
    CONTEXT_MODULE(2),
    PROJECT_MODULE(6),
    BACHELOR_MODULE(12),
    SUBJECT_MODULE(4),
    INTERDISCIPLINARY_MODULE(4),
    DISPENSED_PA_MODULE(6),
    DISPENSED_WPM_MODULE(4);

    private final int credits;
    private String description;

    static {
        SUBJECT_MODULE.description = ResourceBundleMessageLoader.getMessage("module_category.subject_module");
        INTERDISCIPLINARY_MODULE.description = ResourceBundleMessageLoader.getMessage("module_category.interdisciplinary_module");
        CONTEXT_MODULE.description = ResourceBundleMessageLoader.getMessage("module_category.context_module");
        PROJECT_MODULE.description = ResourceBundleMessageLoader.getMessage("module_category.project_module");
        BACHELOR_MODULE.description = ResourceBundleMessageLoader.getMessage("module_category.bachelor_module");
        DISPENSED_PA_MODULE.description = ResourceBundleMessageLoader.getMessage("module_category.dispensed_pa_module");
        DISPENSED_WPM_MODULE.description = ResourceBundleMessageLoader.getMessage("module_category.dispensed_wpm_module");
    }

    /**
     * Categorize modules by module id.
     * @param moduleNo the module id (t.BA.WM.RASOP-EN.19HS)
     * @param moduleGroup the module group (example: IT5 or IT6)
     * @return ModuleCategory or null
     */
    public static ModuleCategory parse(String moduleNo, String moduleGroup) {
        ModuleCategory moduleCategory = null;
        if(moduleNo.startsWith("t.BA.WV.")) {
            moduleCategory = ModuleCategory.SUBJECT_MODULE;
        } else if(moduleNo.startsWith("t.BA.WVK.") || moduleNo.startsWith("t.BA.XXK.")) {
            moduleCategory = ModuleCategory.CONTEXT_MODULE;
        } else if(moduleNo.startsWith("t.BA.WM.")) {
            moduleCategory = ModuleCategory.INTERDISCIPLINARY_MODULE;
        } else if(moduleNo.startsWith("t.BA.XX.")) {
            if(moduleGroup.contains(ModuleParser.MODULE_GROUP_IT_6)) {
                moduleCategory = ModuleCategory.SUBJECT_MODULE;
            } else if(moduleGroup.contains(ModuleParser.MODULE_GROUP_IT_5)) {
                moduleCategory = ModuleCategory.CONTEXT_MODULE;
            }
        }
        return moduleCategory;
    }
}
