package ch.zhaw.vorwahlen.model;


/**
 * Dto for the {@link ch.zhaw.vorwahlen.security.model.User}.
 */
public record UserDTO(String name,
                      String lastName,
                      String affiliation,
                      String homeOrg,
                      String mail,
                      String role,
                      boolean exists) {}
