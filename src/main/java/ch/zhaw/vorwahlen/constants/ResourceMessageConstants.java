package ch.zhaw.vorwahlen.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * This class contains codes for the localized messages as constans.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResourceMessageConstants {

    public static final String ERROR_INTERNAL_SERVER_ERROR = "error.internal_server_error";
    public static final String ERROR_METHOD_ARGUMENT_NOT_VALID = "error.method_argument_not_valid";
    public static final String ERROR_SESSION_NOT_FOUND = "error.session_not_found";
    public static final String ERROR_USER_NOT_FOUND = "error.user_not_found";
    public static final String ERROR_STUDENT_NOT_FOUND = "error.student_not_found";
    public static final String ERROR_MODULE_NOT_FOUND = "error.module_not_found";
    public static final String ERROR_EXPORT_EXCEPTION = "error.export_exception";
    public static final String ERROR_IMPORT_EXCEPTION = "error.import_exception";
    public static final String ERROR_MAIL_TEMPLATE_NOT_FOUND = "error.mail_template_not_found";
    public static final String ERROR_MODULE_ELECTION_NOT_FOUND = "error.module_election_not_found";
}
