package ch.zhaw.vorwahlen.mapper;

import ch.zhaw.vorwahlen.model.dto.ValidationSettingDTO;
import ch.zhaw.vorwahlen.model.modules.ValidationSetting;
import org.springframework.stereotype.Component;

@Component
public class ValidationSettingMapper implements Mapper<ValidationSettingDTO, ValidationSetting> {
    @Override
    public ValidationSettingDTO toDto(ValidationSetting validationSetting) {
        return new ValidationSettingDTO(validationSetting.isRepetent(),
                validationSetting.hadAlreadyElectedTwoConsecutiveModules(),
                validationSetting.isSkipConsecutiveModuleCheck());
    }
}
