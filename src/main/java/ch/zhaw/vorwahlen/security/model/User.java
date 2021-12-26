package ch.zhaw.vorwahlen.security.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Model object of a user for the login. (Shibboleth).
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data @Builder
public class User implements Serializable {
    @Serial
    private static final long serialVersionUID = 4606459981752811017L;
    private String name;
    private String lastName;
    private String affiliation;
    private String homeOrg;
    private String mail;
    private String role;
    private String shibbolethSessionId;
    private boolean isExistent;
}
