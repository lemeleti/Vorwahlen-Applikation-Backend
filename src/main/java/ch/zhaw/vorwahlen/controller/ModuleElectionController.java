package ch.zhaw.vorwahlen.controller;

import ch.zhaw.vorwahlen.model.dto.ModuleElectionDTO;
import ch.zhaw.vorwahlen.model.dto.ModuleStructureDTO;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;

/**
 * Controller for a module.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("election")
public class ModuleElectionController {
    private final ElectionService electionService;
    private final ClassListService classListService;

    /**
     * Stores the selection from student in session.
     * @param moduleElectionDTO the election from user in session.
     * @return true - if election could be saved <br>
     *         false - if election could not be saved or session not found
     */
    @MessageMapping("/save")
    @SendToUser("/queue/electionSaveStatus")
    @Transactional
    public ObjectNode saveElection(SimpMessageHeaderAccessor headerAccessor,
                                   ModuleElectionDTO moduleElectionDTO, @AuthenticationPrincipal User user) {
        var node = new ObjectMapper().createObjectNode();
        var sessionAttributes = headerAccessor.getSessionAttributes();
        if(sessionAttributes != null) {
            var student = getStudent(user);
            node = electionService.saveElection(student, moduleElectionDTO);
        }
        return node;
    }

    /**
     * Returns the stored selection from student in session.
     * @return List of module ids (example: "t.BA.WM.RASOP-EN.19HS")
     */
    @GetMapping(path = {"", "/" })
    public ModuleElectionDTO getElectedModules(@AuthenticationPrincipal User user) {
        var student = getStudent(user);
        return electionService.getModuleElectionByStudent(student);
    }

    @GetMapping(path = {"/structure", "/structure/"})
    public ModuleStructureDTO getFullTimeModuleStructure(@AuthenticationPrincipal User user) {
        return electionService.getModuleStructure(getStudent(user));
    }

    private StudentDTO getStudent(User user) {
        if (user == null) {
            // todo throw exception
        }
        var student = classListService.getStudentById(user.getMail()); // todo: replace with role
        if(student.isEmpty()) {
            // todo throw exception
        }
        return student.get();
    }
}
