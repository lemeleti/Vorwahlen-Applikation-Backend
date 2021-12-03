package ch.zhaw.vorwahlen.mapper;

import ch.zhaw.vorwahlen.model.dto.StudentDTO;
import ch.zhaw.vorwahlen.model.modules.Student;
import org.springframework.stereotype.Component;

@Component
public class StudentMapper {
    public StudentDTO toDto(Student student) {
        return StudentDTO.builder()
                .email(student.getEmail())
                .name(student.getName())
                .clazz(student.getStudentClass().getName())
                .paDispensation(student.getPaDispensation())
                .wpmDispensation(student.getWpmDispensation())
                .isIP(student.isIP())
                .isTZ(student.isTZ())
                .build();
    }
}
