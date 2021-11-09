package ch.zhaw.vorwahlen.controller;

import ch.zhaw.vorwahlen.model.dto.ModuleElectionDTO;
import ch.zhaw.vorwahlen.model.dto.StudentDTO;
import ch.zhaw.vorwahlen.model.user.User;
import ch.zhaw.vorwahlen.service.ClassListService;
import ch.zhaw.vorwahlen.service.ElectionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for a module.
 */
@RequiredArgsConstructor
@Controller
@RequestMapping("election")
public class ModuleElectionController {

    public static final String SPRING_SECURITY_CONTEXT = "SPRING_SECURITY_CONTEXT";
    private final ElectionService electionService;
    private final ClassListService classListService;

    /**
     * Validates current election from student in session.
     * @return true - if election is valid <br>
     *         false -  if election is invalid or session not found
     */
    @MessageMapping("/validate")
    @SendToUser("/queue/electionStatus")
    public boolean isElectionValid(SimpMessageHeaderAccessor headerAccessor) {
        var sessionAttributes = headerAccessor.getSessionAttributes();
        if(sessionAttributes != null) {
            var context = (SecurityContextImpl) sessionAttributes.get(SPRING_SECURITY_CONTEXT);
            var student = getStudent(context);
            return electionService.validateElection(student);
        }
        return false;
    }

    /**
     * Stores the selection from student in session.
     * @param moduleElectionDTO the election from user in session.
     * @return true - if election could be saved <br>
     *         false - if election could not be saved or session not found
     */
    @MessageMapping("/save")
    @SendToUser("/queue/electionSaveStatus")
    public ObjectNode saveElection(SimpMessageHeaderAccessor headerAccessor, ModuleElectionDTO moduleElectionDTO) {
        var node = new ObjectMapper().createObjectNode();
        var sessionAttributes = headerAccessor.getSessionAttributes();
        if(sessionAttributes != null) {
            var context = (SecurityContextImpl) sessionAttributes.get(SPRING_SECURITY_CONTEXT);
            var student = getStudent(context);
            node = electionService.saveElection(student, moduleElectionDTO);
        }
        return node;
    }

    /**
     * Returns the stored selection from student in session.
     * @return List of module ids (example: "t.BA.WM.RASOP-EN.19HS")
     */
    @GetMapping(path = {"", "/" })
    public ModuleElectionDTO getElectedModules() {
        var context = SecurityContextHolder.getContext();
        var student = getStudent(context);
        return electionService.getModuleElectionByStudent(student);
    }

    private StudentDTO getStudent(SecurityContext context) {
        var auth = context.getAuthentication();
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
