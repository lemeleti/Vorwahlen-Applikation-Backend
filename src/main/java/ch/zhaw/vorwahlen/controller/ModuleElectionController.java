package ch.zhaw.vorwahlen.controller;

import ch.zhaw.vorwahlen.config.ResourceBundleMessageLoader;
import ch.zhaw.vorwahlen.constants.ResourceMessageConstants;
import ch.zhaw.vorwahlen.exception.SessionNotFoundException;
import ch.zhaw.vorwahlen.exception.UserNotFoundException;
import ch.zhaw.vorwahlen.model.dto.ElectionTransferDTO;
import ch.zhaw.vorwahlen.model.dto.ModuleElectionDTO;
import ch.zhaw.vorwahlen.security.authentication.CustomAuthToken;
import ch.zhaw.vorwahlen.security.model.User;
import ch.zhaw.vorwahlen.service.ElectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
 * Controller for a module election.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("elections")
public class ModuleElectionController {
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

        var token = (CustomAuthToken) headerAccessor.getUser();
        var user = token != null ? token.getUser() : null;
        if(user == null) {
            throw new UserNotFoundException(ResourceBundleMessageLoader.getMessage(ResourceMessageConstants.ERROR_USER_NOT_FOUND));
        }

        var student = user.getStudent();
        return electionService.saveElection(student, moduleNo);
    }

    /**
     * Get all module elections.
     * @return {@link ResponseEntity} containing list of {@link ModuleElectionDTO}
     */
    @GetMapping(path = {"", "/"})
    public ResponseEntity<List<ModuleElectionDTO>> getAllModuleElections() {
        return ResponseEntity.ok(electionService.getAllModuleElections());
    }

    /**
     * Get module election by id.
     * @param id of the module election.
     * @return {@link ResponseEntity} containing the {@link ModuleElectionDTO}
     */
    @GetMapping(path = {"/{id}", "/{id}/"})
    public ResponseEntity<ModuleElectionDTO> getModuleElectionById(@PathVariable Long id) {
        return ResponseEntity.ok(electionService.getModuleElectionById(id));
    }

    /**
     * Add a new module election.
     * @param moduleElectionDTO to be created module election.
     * @return {@link ResponseEntity} containing {@link ModuleElectionDTO}.
     */
    @PostMapping(path = {"", "/"})
    public ResponseEntity<ModuleElectionDTO> addModuleElection(@RequestBody @Valid ModuleElectionDTO moduleElectionDTO) {
        return ResponseEntity.ok(electionService.createModuleElection(moduleElectionDTO));
    }

    /**
     * Delete a module election by id.
     * @param id of the module election.
     * @return {@link ResponseEntity} containing {@link Void}.
     */
    @DeleteMapping(path = {"/{id}", "/{id}/"})
    public ResponseEntity<Void> deleteModuleElectionById(@PathVariable Long id) {
        electionService.deleteModuleElectionById(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Replaces a module election by id.
     * @param id of the module election.
     * @param moduleElectionDTO new module election.
     * @return {@link ResponseEntity} containing {@link Void}.
     */
    @PutMapping(path = {"/{id}", "/{id}/"})
    public ResponseEntity<Void> replaceModuleElectionById(@PathVariable Long id,
                                                          @RequestBody @Valid ModuleElectionDTO moduleElectionDTO) {
        electionService.updateModuleElection(id, moduleElectionDTO);
        return ResponseEntity.ok().build();
    }

    /**
     * Returns the stored election from student in session.
     * @return {@link ElectionTransferDTO}
     */
    @GetMapping(path = {"/structure", "/structure/" })
    public ElectionTransferDTO getElection(@AuthenticationPrincipal User user) {
        return electionService.getElection(user.getStudent());
    }

    /**
     * Returns all stored module elections as MS-Excel file.
     * @return {@link ResponseEntity} containing byte array with the file data.
     */
    @GetMapping(path = {"/export", "/export/"})
    public ResponseEntity<byte[]> exportModuleElection() {
        var fileName = "attachment; filename=module_election.xlsx";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(EXCEL_MIME))
                .header(HttpHeaders.CONTENT_DISPOSITION, fileName)
                .body(electionService.exportModuleElection());
    }
}
