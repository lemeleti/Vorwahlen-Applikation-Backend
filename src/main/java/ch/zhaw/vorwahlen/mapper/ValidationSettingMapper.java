package ch.zhaw.vorwahlen.mapper;

import ch.zhaw.vorwahlen.model.core.validationsetting.ValidationSettingDTO;
import ch.zhaw.vorwahlen.model.core.validationsetting.ValidationSetting;
import org.springframework.stereotype.Component;

/**
 * Mapping class for {@link ValidationSetting}.
 */
@Component
public class ValidationSettingMapper implements Mapper<ValidationSettingDTO, ValidationSetting> {
    @Override
    public ValidationSettingDTO toDto(ValidationSetting validationSetting) {
        return new ValidationSettingDTO(validationSetting.isRepetent(),
                validationSetting.hadAlreadyElectedTwoConsecutiveModules(),
                validationSetting.isSkipConsecutiveModuleCheck(),
                validationSetting.getElectedContextModulesInFirstElection());
    }

    @Override
    public ValidationSetting toInstance(ValidationSettingDTO param) {
        var settings = new ValidationSetting();
        settings.setRepetent(param.isRepetent());
        settings.setAlreadyElectedTwoConsecutiveModules(param.hadAlreadyElectedTwoConsecutiveModules());
        settings.setSkipConsecutiveModuleCheck(param.isSkipConsecutiveModuleCheck());
        settings.setElectedContextModulesInFirstElection(param.electedContextModulesInFirstElection());
        return settings;
    }

}
