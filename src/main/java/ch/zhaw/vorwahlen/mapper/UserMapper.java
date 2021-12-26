package ch.zhaw.vorwahlen.mapper;

import ch.zhaw.vorwahlen.model.UserDTO;
import ch.zhaw.vorwahlen.security.model.User;
import org.springframework.stereotype.Component;

/**
 * Mapping class for {@link User}.
 */
@Component
public class UserMapper implements Mapper<UserDTO, User> {
    @Override
    public UserDTO toDto(User user) {
        return new UserDTO(
                user.getName(),
                user.getLastName(),
                user.getAffiliation(),
                user.getHomeOrg(),
                user.getMail(),
                user.getRole(),
                user.isExistent()
        );
    }
}
