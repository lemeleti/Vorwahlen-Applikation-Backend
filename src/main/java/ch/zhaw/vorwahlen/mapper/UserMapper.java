package ch.zhaw.vorwahlen.mapper;

import ch.zhaw.vorwahlen.model.dto.UserDTO;
import ch.zhaw.vorwahlen.security.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper implements Mapper<UserDTO, User> {
    @Override
    public UserDTO toDto(User user) {
        var student = user.getStudent();
        return new UserDTO(
                user.getName(),
                user.getLastName(),
                user.getAffiliation(),
                user.getHomeOrg(),
                user.getMail(),
                user.getRole(),
                student.isIP(),
                student.isTZ(),
                student.isSecondElection()
        );
    }
}
