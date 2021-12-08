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
 * Controller for a module.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("election")
public class ModuleElectionController {
    public static final String EXCEL_MIME = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private final ElectionService electionService;

    /**
     * Stores the selection from student in session.
     * @param headerAccessor header data which contains the user session.
     * @param moduleNo module number that the user wants to elect.
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
        if(sessionAttributes == null) {
            throw new SessionNotFoundException(ResourceBundleMessageLoader.getMessage(ResourceMessageConstants.ERROR_SESSION_NOT_FOUND));
        }
        if(user == null) {
            throw new UserNotFoundException(ResourceBundleMessageLoader.getMessage(ResourceMessageConstants.ERROR_USER_NOT_FOUND));
        }
        var student = user.getStudent();
        return electionService.saveElection(student, moduleNo);
    }

    @GetMapping(path = {"", "/"})
    public ResponseEntity<List<ModuleElectionDTO>> getAllModuleElections() {
        return ResponseEntity.ok(electionService.getAllModuleElections());
    }

    @GetMapping(path = {"/{id}", "/{id}/"})
    public ResponseEntity<ModuleElectionDTO> getModuleElectionById(@PathVariable Long id) {
        return ResponseEntity.ok(electionService.getModuleElectionById(id));
    }

    @PostMapping(path = {"", "/"})
    public ResponseEntity<Void> createModuleElection(@RequestBody @Valid ModuleElectionDTO moduleElectionDTO) {
        electionService.createModuleElection(moduleElectionDTO);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(path = {"/{id}", "/{id}/"})
    public ResponseEntity<Void> deleteModuleElectionById(@PathVariable Long id) {
        electionService.deleteModuleElectionById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(path = {"/{id}", "/{id}/"})
    public ResponseEntity<Void> updateModuleElectionById(@PathVariable Long id,
                                                         @RequestBody @Valid ModuleElectionDTO moduleElectionDTO) {
        electionService.updateModuleElection(id, moduleElectionDTO);
        return ResponseEntity.noContent().build();
    }

    /**
     * Returns the stored selection from student in session.
     * @return List of module ids (example: "t.BA.WM.RASOP-EN.19HS")
     */
    @GetMapping(path = {"/structure", "/structure/" })
    public ElectionTransferDTO getElection(@AuthenticationPrincipal User user) {
        return electionService.getElection(user.getStudent());
    }

    /**
     * Returns all stored module elections as MS-Excel file.
     * @return byte array containing the file data.
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
