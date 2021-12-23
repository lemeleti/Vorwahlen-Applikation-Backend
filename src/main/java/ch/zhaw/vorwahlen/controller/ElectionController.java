package ch.zhaw.vorwahlen.controller;

import ch.zhaw.vorwahlen.config.ResourceBundleMessageLoader;
import ch.zhaw.vorwahlen.constants.ResourceMessageConstants;
import ch.zhaw.vorwahlen.exception.SessionNotFoundException;
import ch.zhaw.vorwahlen.exception.UserNotFoundException;
import ch.zhaw.vorwahlen.model.ElectionTransferDTO;
import ch.zhaw.vorwahlen.model.core.election.ElectionDTO;
import ch.zhaw.vorwahlen.security.model.User;
import ch.zhaw.vorwahlen.service.ElectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.List;

/**
 * Controller for an election.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("elections")
public class ElectionController {
    public static final String EXCEL_MIME = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private final ElectionService electionService;

    /**
     * Stores the selection from student in session.
     * @param headerAccessor header data which contains the user session.
     * @param moduleNo module number that the user wants to elect.
     * @return {@link ElectionTransferDTO}
     */
    @MessageMapping("/save")
    @SendToUser("/queue/electionSaveStatus")
    @Transactional
    public ElectionTransferDTO saveElection(SimpMessageHeaderAccessor headerAccessor,
                                            String moduleNo) {
        var sessionAttributes = headerAccessor.getSessionAttributes();
        if(sessionAttributes == null) {
            throw new SessionNotFoundException(ResourceBundleMessageLoader.getMessage(ResourceMessageConstants.ERROR_SESSION_NOT_FOUND));
        }

        if (headerAccessor.getUser() instanceof UsernamePasswordAuthenticationToken authToken &&
                authToken.getPrincipal() instanceof User user) {

            return electionService.saveElection(user.getMail(), moduleNo, headerAccessor);
        } else {
            throw new UserNotFoundException(ResourceBundleMessageLoader.getMessage(ResourceMessageConstants.ERROR_USER_NOT_FOUND));
        }
    }

    /**
     * Get all  elections.
     * @return {@link ResponseEntity} containing list of {@link ElectionDTO}
     */
    @GetMapping(path = "")
    public ResponseEntity<List<ElectionDTO>> getAllElections() {
        return ResponseEntity.ok(electionService.getAllElections());
    }

    /**
     * Get  election by id.
     * @param id of the  election.
     * @return {@link ResponseEntity} containing the {@link ElectionDTO}
     */
    @GetMapping(path = "/{id}")
    public ResponseEntity<ElectionDTO> getElectionById(@PathVariable Long id) {
        return ResponseEntity.ok(electionService.getElectionById(id));
    }

    /**
     * Add a new  election.
     * @param electionDTO to be created  election.
     * @return {@link ResponseEntity} containing {@link ElectionDTO}.
     */
    @PostMapping(path = "")
    public ResponseEntity<ElectionDTO> addElection(@RequestBody @Valid ElectionDTO electionDTO) {
        return ResponseEntity.ok(electionService.createElection(electionDTO));
    }

    /**
     * Delete an election by id.
     * @param id of the  election.
     * @return {@link ResponseEntity} containing {@link Void}.
     */
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteElectionById(@PathVariable Long id) {
        electionService.deleteElectionById(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Replaces an election by id.
     * @param id of the  election.
     * @param electionDTO new  election.
     * @return {@link ResponseEntity} containing {@link Void}.
     */
    @PutMapping(path = "/{id}")
    public ResponseEntity<Void> replaceElectionById(@PathVariable Long id,
                                                    @RequestBody @Valid ElectionDTO electionDTO) {
        electionService.updateElection(id, electionDTO);
        return ResponseEntity.ok().build();
    }

    /**
     * Returns the stored election from student in session.
     * @param studentId email of the student.
     * @return {@link ElectionTransferDTO}
     */
    @GetMapping(path = "/{studentId}/structure")
    public ElectionTransferDTO getElection(@PathVariable String studentId) {
        return electionService.getElection(studentId);
    }

    /**
     * Triggers to validate the election again.
     * @param studentId email of the student.
     * @return {@link ResponseEntity} containing {@link Void}.
     */
    @PostMapping(path = "/{studentId}/validate")
    public ResponseEntity<Void> triggerValidation(@PathVariable String studentId) {
        electionService.updateValidation(studentId);
        return ResponseEntity.ok().build();
    }

    /**
     * Returns all stored  elections as MS-Excel file.
     * @return {@link ResponseEntity} containing byte array with the file data.
     */
    @GetMapping(path = "/export")
    public ResponseEntity<byte[]> exportElection() {
        var fileName = "attachment; filename=module_election.xlsx";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(EXCEL_MIME))
                .header(HttpHeaders.CONTENT_DISPOSITION, fileName)
                .body(electionService.exportElection());
    }
}
