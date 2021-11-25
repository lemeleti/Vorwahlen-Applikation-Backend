package ch.zhaw.vorwahlen.model.modulestructure;

import java.util.Map;

public interface ModuleDefinition {
    Map<Integer, Integer> contextModules();
    Map<Integer, Integer> projectModule();
    Map<Integer, Integer> interdisciplinaryModules();
    Map<Integer, Integer> subjectModules();
    Map<Integer, Integer> bachelorModule();
}
