package ch.zhaw.vorwahlen.controller;

import ch.zhaw.vorwahlen.model.dto.ModuleElectionDTO;
import ch.zhaw.vorwahlen.model.dto.StudentDTO;
import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.model.modules.ModuleElection;
import ch.zhaw.vorwahlen.model.user.User;
import ch.zhaw.vorwahlen.service.ClassListService;
import ch.zhaw.vorwahlen.service.DTOMapper;
import ch.zhaw.vorwahlen.service.ElectionService;
import ch.zhaw.vorwahlen.service.ModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Controller for a module.
 */
@RequiredArgsConstructor
@Controller
public class ModuleElectionController {
    private final ModuleService moduleService;
    private final ElectionService electionService;
    private final ClassListService classListService;

    /**
     *
     * @return
     */
    @MessageMapping("/validate")
    @SendTo("/module/electionStatus")
    public boolean isElectionValid() {
        var student = getStudent();
        return electionService.validateElection(student);
    }

    /**
     *
     * @param moduleElectionDTO
     * @return
     */
    @MessageMapping("/save")
    @SendTo("/module/electionSaveStatus")
    public boolean saveElection(ModuleElectionDTO moduleElectionDTO) {
        var student = getStudent();
        return electionService.saveElection(student, moduleElectionDTO);
    }


    public List<String> getElectedModules() {
        return List.of();
    }

    private StudentDTO getStudent() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var user = getUserFromAuth(auth);
        if (user == null) {
            // todo throw exception
        }
        var student = classListService.getStudentById(user.getMail()); // todo: replace with role
        if(student.isEmpty()) {
            // todo throw exception
        }
        return student.get();
    }

    private User getUserFromAuth(Authentication auth) {
        User user = null;
        try {
            if (auth != null && auth.getPrincipal() != null) {
                user = (User) auth.getPrincipal();
            }
        } catch (ClassCastException ignored) {
            // Has to be empty, do nothing
        }
        return user;
    }
}
