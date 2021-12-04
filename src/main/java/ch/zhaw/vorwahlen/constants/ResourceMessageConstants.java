package ch.zhaw.vorwahlen.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResourceMessageConstants {

    public static final String ERROR_SESSION_NOT_FOUND = "error.session_not_found";
    public static final String ERROR_USER_NOT_FOUND = "error.user_not_found";
    public static final String ERROR_STUDENT_NOT_FOUND = "error.student_not_found";
    public static final String ERROR_MODULE_NOT_FOUND = "error.module_not_found";
    public static final String ERROR_EXPORT_EXCEPTION = "error.export_exception";
    public static final String ERROR_IMPORT_EXCEPTION = "error.import_exception";

}
