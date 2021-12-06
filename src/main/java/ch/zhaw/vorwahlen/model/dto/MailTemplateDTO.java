package ch.zhaw.vorwahlen.model.dto;

import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
public record MailTemplateDTO(Long id,
                              String description,
                              String subject,
                              String message) {
}
