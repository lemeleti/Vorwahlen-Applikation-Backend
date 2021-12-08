package ch.zhaw.vorwahlen.config;

import lombok.experimental.UtilityClass;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Load resource file with localized messages.
 */
@UtilityClass
public class ResourceBundleMessageLoader {

    private final ResourceBundle resourceBundle = ResourceBundle.getBundle("resource",  Locale.GERMAN);

    /**
     * Returns localized message
     * @param code the identifier to the message
     * @return String
     */
    public String getMessage(String code) {
        return resourceBundle.getString(code);
    }

}
