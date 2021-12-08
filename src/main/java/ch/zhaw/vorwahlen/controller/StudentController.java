package ch.zhaw.vorwahlen.controller;

import ch.zhaw.vorwahlen.model.dto.NotificationDTO;
import ch.zhaw.vorwahlen.model.dto.StudentDTO;
import ch.zhaw.vorwahlen.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for the Student entity.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("students")
public class StudentController {
    private final StudentService studentService;

    /**
     * Return all class lists.
     * @return {@link ResponseEntity<List<StudentDTO>>} with status code ok.
     */
    @GetMapping(path = {"/", ""})
    public ResponseEntity<List<StudentDTO>> getAllStudents(
            @RequestParam(name = "electionvalid", required = false) Optional<Boolean> electionStatus) {

        List<StudentDTO> students;
        if (electionStatus.isPresent()) {
            students = studentService.getAllStudentsByElection(electionStatus.get());
        } else {
            students = studentService.getAllStudents();
        }

        return ResponseEntity.ok().body(students);
    }

    /**
     * Add a student.
     * @param studentDTO to be added student.
     * @return {@link ResponseEntity<Void>} with status code ok.
     */
    @PostMapping(path = {"/", ""}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> addStudent(@Valid @RequestBody StudentDTO studentDTO) {
        return ResponseEntity.created(studentService.addAndReturnLocation(studentDTO)).build();
    }

    /**
     * Returns the student by his id.
     * @param id of student.
     * @return {@link ResponseEntity<StudentDTO>} with status code ok.
     */
    @GetMapping(path = {"/{id}", "/{id}/"})
    public ResponseEntity<StudentDTO> getStudentById(@PathVariable String id) {
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    /**
     * Deletes a student by his id.
     * @param id of student.
     * @return {@link ResponseEntity<Void>} with status code no content.
     */
    @DeleteMapping(path = {"/{id}", "/{id}/"})
    public ResponseEntity<Void> deleteStudentById(@PathVariable String id) {
        studentService.deleteStudentById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Replace a student by his id.
     * @param id of student.
     * @param studentDTO the new student.
     * @return {@link ResponseEntity<StudentDTO>} with status code no content.
     */
    @PutMapping(path = {"/{id}", "/{id}/"})
    public ResponseEntity<StudentDTO> replaceStudentById(@PathVariable String id,
                                                         @Valid @RequestBody StudentDTO studentDTO) {
        return ResponseEntity.ok(studentService.replaceStudent(id, studentDTO));
    }

    @PatchMapping(path = {"/{id}", "/{id}/"})
    public ResponseEntity<Void> patchFields(@PathVariable String id,
                                            @RequestBody Map<String, Boolean> patchedFields) {
        studentService.updateStudentEditableFields(id, patchedFields);
        return ResponseEntity.noContent().build();
    }

    /**
     * Import class list from Excel.
     * @param file the Excel file.
     * @return {@link ResponseEntity<String>} with status code ok or bad request if the provided file is not there.
     */
    @PostMapping(path = {"/", ""}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> saveClassListsFromExcel(@RequestParam("file") MultipartFile file,
                                                          @RequestParam("worksheet") String worksheet) {
        if (file.isEmpty()) return ResponseEntity.badRequest().build();
        studentService.importClassListExcel(file, worksheet);
        return ResponseEntity.ok().build();
    }

    /**
     * Import dispensation list from Excel.
     * @param file the Excel file.
     * @return {@link ResponseEntity<String>} with status code ok or bad request when the provided file is not there
     */
    @PostMapping(path = {"/dispensations", "/dispensations"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> saveDispensationListFromExcel(@RequestParam("file") MultipartFile file,
                                                                @RequestParam("worksheet") String worksheet) {
        if (file.isEmpty()) return ResponseEntity.badRequest().build();
        studentService.importDispensationExcel(file, worksheet);
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = {"/notify", "/notify/"})
    public ResponseEntity<Void> notifyStudents(@Valid @RequestBody NotificationDTO notificationDTO) {
        studentService.notifyStudents(notificationDTO);
        return ResponseEntity.ok().build();
    }
}
