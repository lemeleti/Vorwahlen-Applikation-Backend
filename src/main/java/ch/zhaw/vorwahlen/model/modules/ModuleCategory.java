package ch.zhaw.vorwahlen.model.modules;

import ch.zhaw.vorwahlen.parser.ModuleParser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Categorize modules by module id.
 */
@RequiredArgsConstructor
@Getter
public enum ModuleCategory {
    SUBJECT_MODULE(4, "Fachliches Wahlpflichmodul"),
    INTERDISCIPLINARY_MODULE(4, "Überfachliches Wahlpflichmodul"),
    CONTEXT_MODULE(2, "Kontext Wahlpflichmodul"),
    PROJECT_MODULE(6, "Projektarbeit in der Informatik"),
    BACHELOR_MODULE(12, "Bachelorarbeit in der Informatik"),
    DISPENSED_PA_MODULE(6, "Projektarbeit in der Informatik - Dispensiert"),
    DISPENSED_WPM_MODULE(4, "Fachliches Wahlpflichmodul - Dispensiert");

    private final int credits;
    private final String description;

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
