package ch.zhaw.vorwahlen.controller;

import ch.zhaw.vorwahlen.authentication.CustomAuthToken;
import ch.zhaw.vorwahlen.model.dto.ModuleElectionDTO;
import ch.zhaw.vorwahlen.model.dto.ModuleStructureDTO;
import ch.zhaw.vorwahlen.model.user.User;
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

/**
 * Controller for a module.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("election")
public class ModuleElectionController {
    private final ElectionService electionService;

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
                                   ModuleElectionDTO moduleElectionDTO) {
        // todo improve
        var token = ((CustomAuthToken) headerAccessor.getUser());
        var user = token != null ? token.getUser() : null;
        var node = new ObjectMapper().createObjectNode();
        var sessionAttributes = headerAccessor.getSessionAttributes();
        if(sessionAttributes != null && user != null) {
            var student = user.getStudent();
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
        return electionService.getModuleElectionByStudent(user.getStudent());
    }

    @GetMapping(path = {"/structure", "/structure/"})
    public ModuleStructureDTO getFullTimeModuleStructure(@AuthenticationPrincipal User user) {
        return electionService.getModuleStructure(user.getStudent());
    }
}
