package ch.zhaw.vorwahlen.model.modulestructure;

import ch.zhaw.vorwahlen.model.core.student.Student;
import lombok.Data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Model class for which kind of student has which semesters for the election.
 */
@Data
public class ElectionSemesters {
    List<Integer> fullTimeSemesters;
    List<Integer> partTimeSemestersFirstElection;
    List<Integer> partTimeSemestersSecondElection;

    /**
     * Returns the list of semesters for the election depending on the student.
     * @param student the current student.
     * @return set of integers.
     */
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
