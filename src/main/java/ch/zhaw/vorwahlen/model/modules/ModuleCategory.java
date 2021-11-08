package ch.zhaw.vorwahlen.model.modules;

import ch.zhaw.vorwahlen.parser.ModuleParser;
import lombok.RequiredArgsConstructor;

/**
 * Categorize modules by module id.
 */
@RequiredArgsConstructor
public enum ModuleCategory {
    SUBJECT_MODULE,
    INTERDISCIPLINARY_MODULE,
    CONTEXT_MODULE;

    /**
     * Categorize modules by module id.
     * @param moduleNo the module id (t.BA.WM.RASOP-EN.19HS)
     * @param moduleGroup the module group (example: IT5 or IT6)
     * @return
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
