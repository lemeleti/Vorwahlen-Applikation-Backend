package ch.zhaw.vorwahlen.model.dto;

import org.springframework.boot.context.properties.ConstructorBinding;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

/**
 * Dto for the notification.
 */
@ConstructorBinding
public record NotificationDTO(@Email(message = "{validation.email.invalid}") String email,
                              @NotEmpty(message = "{validation.password.empty}") String password,
                              @NotEmpty(message = "{validation.subject.empty}") String subject,
                              @NotEmpty(message = "{validation.message.empty}") String message,
                              @NotEmpty(message = "{validation.addresses.empty}") String[] studentMailAddresses) {
}
