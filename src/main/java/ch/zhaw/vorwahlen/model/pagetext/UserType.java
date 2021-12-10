package ch.zhaw.vorwahlen.model.pagetext;

import ch.zhaw.vorwahlen.config.ResourceBundleMessageLoader;
import ch.zhaw.vorwahlen.constants.ResourceMessageConstants;
import ch.zhaw.vorwahlen.exception.UserTypeInvalidException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enumerate types of user.
 */
@RequiredArgsConstructor
@Getter
public enum UserType {
    ANONYMOUS("anonym"),
    FULL_TIME("vz"),
    PART_TIME_FIRST_ELECTION("tz1"),
    PART_TIME_SECOND_ELECTION("tz2");

    private final String pathString;

    /**
     * Parse a string to {@link UserType}.
     * @param value the string to be parsed
     * @return {@link UserType}
     * @throws UserTypeInvalidException There is {@link UserType} of this value.
     */
    public static UserType parseString(String value) {
        for (var type: UserType.values()) {
            if(type.pathString.equals(value)) {
                return type;
            }
        }
        var message = ResourceBundleMessageLoader.getMessage(ResourceMessageConstants.ERROR_USER_TYPE_INVALID);
        throw new UserTypeInvalidException(message);
    }
}
