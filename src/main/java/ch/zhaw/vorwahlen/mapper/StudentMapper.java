package ch.zhaw.vorwahlen.mapper;

import ch.zhaw.vorwahlen.model.dto.StudentDTO;
import ch.zhaw.vorwahlen.model.modules.Student;
import org.springframework.stereotype.Component;

@Component
public class StudentMapper implements Mapper<StudentDTO, Student> {
    @Override
    public StudentDTO toDto(Student student) {
        return StudentDTO.builder()
                .email(student.getEmail())
                .name(student.getName())
                .clazz(student.getStudentClass().getName())
                .paDispensation(student.getPaDispensation())
                .wpmDispensation(student.getWpmDispensation())
                .isIP(student.isIP())
                .isTZ(student.isTZ())
                .isSecondElection(student.isSecondElection())
                .moduleElectionId(student.getElection() != null ? student.getElection().getId() : 0)
                .firstTimeSetup(student.isFirstTimeSetup())
                .canElect(student.isCanElect())
                .build();
    }

    @Override
    public Student toInstance(StudentDTO studentDTO) {
        return Student.builder()
                .email(studentDTO.getEmail())
                .name(studentDTO.getName())
                .paDispensation(studentDTO.getPaDispensation())
                .wpmDispensation(studentDTO.getWpmDispensation())
                .isIP(studentDTO.isIP())
                .isTZ(studentDTO.isTZ())
                .isSecondElection(studentDTO.isSecondElection())
                .canElect(studentDTO.isCanElect())
                .firstTimeSetup(studentDTO.isFirstTimeSetup())
                .build();
    }
}
