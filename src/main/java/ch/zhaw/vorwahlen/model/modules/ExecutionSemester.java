package ch.zhaw.vorwahlen.model.modules;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum for execution semester.
 */
@RequiredArgsConstructor
@Getter
public enum ExecutionSemester {
    AUTUMN(5, "(HS)"),
    SPRING(6, "(FS)"),
    AUTUMN_AND_SPRING(7, "(HS)/(FS)");

    public static final int BOTH_SEMESTERS_INT = 7;
    public static final int SPRING_SEMESTER_INT = 6;
    public static final String BOTH_SEMESTERS_STRING = "5;6";
    public static final String AUTUMN_SEMESTER_STRING = "5.0";
    private final int semester;
    private final String description;

    /**
     * Parses the string to {@link ExecutionSemester}.
     * @param semesters string to be parsed.
     * @return ExecutionSemester
     */
    public static ExecutionSemester parseFromString(String semesters) {
        ExecutionSemester executionSemester;
        if (semesters.contains(BOTH_SEMESTERS_STRING)) {
            executionSemester = AUTUMN_AND_SPRING;
        } else if (semesters.contains(AUTUMN_SEMESTER_STRING)) {
            executionSemester = AUTUMN;
        } else {
            executionSemester = SPRING;
        }
        return executionSemester;
    }

    /**
     * Parses the semester to {@link ExecutionSemester}.
     * @param semesters to be parsed integer.
     * @return ExecutionSemester
     */
    public static ExecutionSemester parseFromInt(int semesters) {
        ExecutionSemester executionSemester;
        if (semesters == BOTH_SEMESTERS_INT) {
            executionSemester = AUTUMN_AND_SPRING;
        } else if (semesters == SPRING_SEMESTER_INT) {
            executionSemester = SPRING;
        } else {
            executionSemester = AUTUMN;
        }
        return executionSemester;
    }
}
