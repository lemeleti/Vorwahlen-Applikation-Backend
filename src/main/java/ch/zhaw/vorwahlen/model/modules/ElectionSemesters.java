package ch.zhaw.vorwahlen.model.modules;

import lombok.Data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class ElectionSemesters {
    List<Integer> fullTimeSemesters;
    List<Integer> partTimeSemestersFirstElection;
    List<Integer> partTimeSemestersSecondElection;

    public Set<Integer> getSemestersForStudent(Student student) {
        var semesterList = fullTimeSemesters;
        if (student.isTZ() && student.isSecondElection()) {
            semesterList = partTimeSemestersSecondElection;
        } else if (student.isTZ()) {
            semesterList = partTimeSemestersFirstElection;
        }
        return new HashSet<>(semesterList);
    }
}
