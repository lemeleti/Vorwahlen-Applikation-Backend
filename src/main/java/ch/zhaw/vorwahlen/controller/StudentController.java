package ch.zhaw.vorwahlen.controller;

import ch.zhaw.vorwahlen.model.mailtemplate.NotificationDTO;
import ch.zhaw.vorwahlen.model.core.student.StudentDTO;
import ch.zhaw.vorwahlen.model.core.validationsetting.ValidationSettingDTO;
import ch.zhaw.vorwahlen.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * Controller for the Student entity.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("students")
public class StudentController {
    private final StudentService studentService;

    /**
     * Return all students.
     * @return {@link ResponseEntity} containing list of {@link StudentDTO}.
     */
    @GetMapping(path = "")
    public ResponseEntity<List<StudentDTO>> getAllStudents() {

        return ResponseEntity.ok().body(studentService.getAllStudents());
    }

    /**
     * Add a student.
     * @param studentDTO to be added student.
     * @return {@link ResponseEntity} containing {@link StudentDTO}.
     */
    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StudentDTO> addStudent(@Valid @RequestBody StudentDTO studentDTO) {
        return ResponseEntity.ok(studentService.addStudent(studentDTO));
    }

    /**
     * Returns the student by his id.
     * @param id of student.
     * @return {@link ResponseEntity} containing the {@link StudentDTO}.
     */
    @GetMapping(path = "/{id}")
    public ResponseEntity<StudentDTO> getStudentById(@PathVariable String id) {
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    /**
     * Deletes a student by his id.
     * @param id of student.
     * @return {@link ResponseEntity} containing {@link Void}.
     */
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteStudentById(@PathVariable String id) {
        studentService.deleteStudentById(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Replace a student by his id.
     * @param id of student.
     * @param studentDTO the new student.
     * @return {@link ResponseEntity} containing the {@link Void}.
     */
    @PutMapping(path = "/{id}")
    public ResponseEntity<Void> replaceStudentById(@PathVariable String id,
                                                         @Valid @RequestBody StudentDTO studentDTO) {
        studentService.replaceStudent(id, studentDTO);
        return ResponseEntity.ok().build();
    }

    /**
     * Patch student information.
     * @param id the student id.
     * @param patchedFields fields to be patched.
     * @return {@link ResponseEntity} containing {@link Void}.
     */
    @PatchMapping(path = "/{id}")
    public ResponseEntity<Void> patchFields(@PathVariable String id,
                                            @RequestBody Map<String, Boolean> patchedFields) {
        studentService.updateStudentEditableFields(id, patchedFields);
        return ResponseEntity.ok().build();
    }

    /**
     * Replace the settings for a student
     * @param id of student.
     * @param validationSettingDTO the new settings.
     * @return {@link ResponseEntity} containing the {@link Void}.
     */
    @PutMapping(path = "/{id}/settings")
    public ResponseEntity<Void> replaceValidationSettings(@PathVariable String id,
                                                          @RequestBody ValidationSettingDTO validationSettingDTO) {
        studentService.replaceValidationSettings(id, validationSettingDTO);
        return ResponseEntity.ok().build();
    }

    /**
     * Get validation settings by student mail
     * @param id mail of the student
     * @return {@link ValidationSettingDTO}
     */
    @GetMapping(path ="/{id}/settings")
    public ResponseEntity<ValidationSettingDTO> getValidationSettings(@PathVariable String id) {
        return ResponseEntity.ok(studentService.getValidationSettingForStudent(id));
    }

    /**
     * Import class list from Excel.
     * @param file the Excel file.
     * @return {@link ResponseEntity} containing {@link Void}.
     */
    @PostMapping(path = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> saveClassListsFromExcel(@RequestParam("file") MultipartFile file,
                                                        @RequestParam("worksheet") String worksheet) {
        if (file.isEmpty()) return ResponseEntity.badRequest().build();
        studentService.importClassListExcel(file, worksheet);
        return ResponseEntity.ok().build();
    }

    /**
     * Import dispensation list from Excel.
     * @param file the Excel file.
     * @return {@link ResponseEntity} containing {@link Void}.
     */
    @PostMapping(path = "/dispensations", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> saveDispensationListFromExcel(@RequestParam("file") MultipartFile file,
                                                              @RequestParam("worksheet") String worksheet) {
        if (file.isEmpty()) return ResponseEntity.badRequest().build();
        studentService.importDispensationExcel(file, worksheet);
        return ResponseEntity.ok().build();
    }

    /**
     * Send notification to the students.
     * @param notificationDTO the notification to be send.
     * @return {@link ResponseEntity} containing {@link Void}.
     */
    @PostMapping(path = "/notify")
    public ResponseEntity<Void> notifyStudents(@Valid @RequestBody NotificationDTO notificationDTO) {
        studentService.notifyStudents(notificationDTO);
        return ResponseEntity.ok().build();
    }
}
