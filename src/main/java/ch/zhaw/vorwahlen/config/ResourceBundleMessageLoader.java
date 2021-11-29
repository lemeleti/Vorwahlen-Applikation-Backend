package ch.zhaw.vorwahlen.config;

import lombok.experimental.UtilityClass;

import java.util.Locale;
import java.util.ResourceBundle;

@UtilityClass
public class ResourceBundleMessageLoader {

    private final ResourceBundle resourceBundle = ResourceBundle.getBundle("resource",  Locale.GERMAN);

    public String getMessage(String code) {
        return resourceBundle.getString(code);
    }

}
