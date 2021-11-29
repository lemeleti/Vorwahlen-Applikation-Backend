package ch.zhaw.vorwahlen.model.dto;

public record ValidationSettingDTO (boolean isRepetent,
                                    boolean hadAlreadyElectedTwoConsecutiveModules,
                                    boolean isSkipConsecutiveModuleCheck) {}
