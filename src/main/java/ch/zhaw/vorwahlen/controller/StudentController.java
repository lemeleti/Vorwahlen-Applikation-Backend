package ch.zhaw.vorwahlen.controller;

import ch.zhaw.vorwahlen.model.dto.StudentDTO;
import ch.zhaw.vorwahlen.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

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
    public ResponseEntity<List<StudentDTO>> getAllStudents() {
        return ResponseEntity.ok().body(studentService.getAllStudents());
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
    public ResponseEntity<StudentDTO> replaceStudent(@PathVariable String id,
                                                     @Valid @RequestBody StudentDTO studentDTO) {
        return ResponseEntity.ok(studentService.replaceStudent(id, studentDTO));
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

}
