package ch.zhaw.vorwahlen.model.dto;

import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
public record StudentFirstTimeSetupDTO(boolean isFirstTimeSetup) {}
