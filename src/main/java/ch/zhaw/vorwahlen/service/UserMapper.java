package ch.zhaw.vorwahlen.service;

import ch.zhaw.vorwahlen.model.dto.UserDTO;
import ch.zhaw.vorwahlen.model.user.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserDTO toDto(User user) {
        return new UserDTO(
                user.getName(),
                user.getLastName(),
                user.getAffiliation(),
                user.getHomeOrg(),
                user.getMail(),
                user.getRole()
        );
    }
}
