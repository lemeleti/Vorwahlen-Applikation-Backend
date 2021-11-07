package ch.zhaw.vorwahlen.controller;

import ch.zhaw.vorwahlen.model.dto.EventoDataDTO;
import ch.zhaw.vorwahlen.model.dto.ModuleDTO;
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
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Controller for a module.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("module")
public class ModuleController {
    private final ModuleService moduleService;
    private final ElectionService electionService;
    private final ClassListService classListService;

    /**
     * Return all modules.
     * @return {@link ResponseEntity<List<ModuleDTO>>} with status code ok
     */
    @GetMapping(path = {"/", ""})
    public ResponseEntity<List<ModuleDTO>> getAllModules() {
        return ResponseEntity.ok().body(moduleService.getAllModules());
    }

    /**
     * Import module list from Excel.
     * @param file the Excel file.
     * @return {@link ResponseEntity<String>} with status code ok or bad request if the provided file is not there
     */
    @PostMapping(path = {"/", ""})
    public ResponseEntity<String> saveModulesFromExcel(@RequestParam("file") MultipartFile file,
                                                       @RequestParam("worksheet") String worksheet) {
        if (file.isEmpty()) return ResponseEntity.badRequest().build();
        moduleService.importModuleExcel(file, worksheet);
        return ResponseEntity.ok().build();
    }

    /**
     * Returns the additional data of a module.
     * @return {@link ResponseEntity<EventoDataDTO>} with status code ok
     */
    @GetMapping(path = {"/eventodata/{moduleKuerzel}"})
    public ResponseEntity<EventoDataDTO> getAdditionalModuleDataByKuerzel(@PathVariable String moduleKuerzel) {
        return ResponseEntity.ok().body(moduleService.getEventoDataById(moduleKuerzel));
    }

    /**
     * Scrape external website to retrieve additional data from the module list.
     * @return {@link ResponseEntity<String>} with status code ok
     */
    @PostMapping(path = "/scrape")
    public ResponseEntity<String> fetchAdditionalModuleData() {
        moduleService.fetchAdditionalModuleData();
        return ResponseEntity.ok().build();
    }

    /**
     *
     * @return
     */
    @MessageMapping("/validate")
    @SendTo("/module/electionStatus")
    public boolean isElectionValid() {
        var student = getStudent();
        var storedElection = electionService.getModuleElectionByStudent(student);
        if (storedElection.isEmpty()) {
            return false;
        }
        var moduleElection = mapDtoToModuleElection(storedElection.get(), student);
        return electionService.validateElection(student, moduleElection);
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
        var moduleElection = mapDtoToModuleElection(moduleElectionDTO, student);
        return electionService.saveElection(student, moduleElection);
    }

    private ModuleElection mapDtoToModuleElection(ModuleElectionDTO moduleElectionDTO, StudentDTO studentDTO) {
        Function<Set<String>, Set<Module>> mapModuleSet = list ->
                list.stream()
                        .map(moduleNo -> moduleService.getModuleById(moduleNo).get())
                        .map(DTOMapper.mapDtoToModule)
                        .collect(Collectors.toSet());

        var moduleElection = new ModuleElection();
        moduleElection.setStudentEmail(studentDTO.getEmail());
        moduleElection.setElectionValid(moduleElectionDTO.isElectionValid());
        moduleElection.setElectedModules(mapModuleSet.apply(moduleElectionDTO.getElectedModules()));
        moduleElection.setOverflowedElectedModules(mapModuleSet.apply(moduleElectionDTO.getOverflowedElectedModules()));
        return moduleElection;
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
