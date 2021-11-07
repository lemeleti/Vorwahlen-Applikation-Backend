package ch.zhaw.vorwahlen.model.modules;

import ch.zhaw.vorwahlen.parser.ModuleParser;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ModuleCategory {
    SUBJECT_MODULE,
    INTERDISCIPLINARY_MODULE,
    CONTEXT_MODULE;

    public static ModuleCategory parse(String moduleNo, String moduleGroup) {
        ModuleCategory moduleCategory = null;
        if(moduleNo.startsWith("WV.")) {
            moduleCategory = ModuleCategory.SUBJECT_MODULE;
        } else if(moduleNo.startsWith("WVK.") || moduleNo.startsWith("XXK.")) {
            moduleCategory = ModuleCategory.CONTEXT_MODULE;
        } else if(moduleNo.startsWith("WM.")) {
            moduleCategory = ModuleCategory.INTERDISCIPLINARY_MODULE;
        } else if(moduleNo.startsWith("XX.")) {
            if(moduleGroup.contains(ModuleParser.MODULE_GROUP_IT_6)) {
                moduleCategory = ModuleCategory.SUBJECT_MODULE;
            } else if(moduleGroup.contains(ModuleParser.MODULE_GROUP_IT_5)) {
                moduleCategory = ModuleCategory.CONTEXT_MODULE;
            }
        }
        return moduleCategory;
    }
}
