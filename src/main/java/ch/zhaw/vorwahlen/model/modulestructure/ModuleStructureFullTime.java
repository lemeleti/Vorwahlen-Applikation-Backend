package ch.zhaw.vorwahlen.model.modulestructure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.util.Map;

@ConstructorBinding
@ConfigurationProperties(prefix = "vz")
public record ModuleStructureFullTime(Map<Integer, Integer> contextModules,
                                      Map<Integer, Integer> projectModule,
                                      Map<Integer, Integer> interdisciplinaryModules,
                                      Map<Integer, Integer> subjectModules,
                                      Map<Integer, Integer> bachelorModule) implements ModuleStructure {}
