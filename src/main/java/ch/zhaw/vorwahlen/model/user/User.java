package ch.zhaw.vorwahlen.model.user;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data @Builder
public class User implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String name;
    private String lastName;
    private String affiliation;
    private String homeOrg;
    private String mail;
}
