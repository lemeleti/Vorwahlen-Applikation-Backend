package ch.zhaw.vorwahlen.model.mailtemplate;

import org.springframework.boot.context.properties.ConstructorBinding;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Dto for the {@link MailTemplate}.
 */
@ConstructorBinding
public record MailTemplateDTO(Long id,
                              @Size(max = 255, message = "{validation.mail.template.description.size}")
                              @NotNull(message = "{validation.mail.template.description.null}")
                              String description,
                              @Size(max = 255, message = "{validation.mail.template.subject.size}")
                              @NotNull(message = "{validation.mail.template.subject.null}")
                              String subject,
                              @NotNull(message = "{validation.mail.template.message.null}")
                              String message) {
}
