package ch.zhaw.vorwahlen.model.dto;

import org.springframework.boot.context.properties.ConstructorBinding;

/**
 * Dto for the {@link ch.zhaw.vorwahlen.model.modules.MailTemplate}.
 */
@ConstructorBinding
public record MailTemplateDTO(Long id,
                              String description,
                              String subject,
                              String message) {
}
