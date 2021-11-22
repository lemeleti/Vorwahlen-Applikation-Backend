package ch.zhaw.vorwahlen.controller;

import ch.zhaw.vorwahlen.authentication.CustomAuthToken;
import ch.zhaw.vorwahlen.model.dto.ElectionTransferDTO;
import ch.zhaw.vorwahlen.model.dto.ModuleElectionDTO;
import ch.zhaw.vorwahlen.model.dto.ElectionStructureDTO;
import ch.zhaw.vorwahlen.model.user.User;
import ch.zhaw.vorwahlen.service.ElectionService;
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
    public ElectionTransferDTO saveElection(SimpMessageHeaderAccessor headerAccessor,
                                            String moduleNo) {
        // todo improve
        var token = ((CustomAuthToken) headerAccessor.getUser());
        var user = token != null ? token.getUser() : null;
        var sessionAttributes = headerAccessor.getSessionAttributes();
        if(sessionAttributes == null || user == null) {
            // todo replace with custom exception
            throw new RuntimeException("No valid user or session was found");
        }
        var student = user.getStudent();
        return electionService.saveElection(student, moduleNo);
    }

    /**
     * Returns the stored selection from student in session.
     * @return List of module ids (example: "t.BA.WM.RASOP-EN.19HS")
     */
    @GetMapping(path = {"", "/" })
    public ElectionTransferDTO getElection(@AuthenticationPrincipal User user) {
        return electionService.getElection(user.getStudent());
    }
}
