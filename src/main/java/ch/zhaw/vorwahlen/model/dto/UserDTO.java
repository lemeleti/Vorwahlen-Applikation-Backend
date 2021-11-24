package ch.zhaw.vorwahlen.model.dto;

public record UserDTO(String name,
                      String lastName,
                      String affiliation,
                      String homeOrg,
                      String mail,
                      String role,
                      boolean isIP,
                      boolean isTZ,
                      boolean isSecondElection) {}
