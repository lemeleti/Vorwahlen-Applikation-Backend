package ch.zhaw.vorwahlen.model.dto;

import org.springframework.boot.context.properties.ConstructorBinding;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@ConstructorBinding
public record NotificationDTO(@Email String email,
                              @NotEmpty String password,
                              @NotEmpty String subject,
                              @NotEmpty String message,
                              @NotEmpty String[] studentMailAddresses) {
}
