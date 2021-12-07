package ch.zhaw.vorwahlen.model.modules;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
public enum ExecutionSemester {
    AUTUMN(5, "(HS)"),
    SPRING(6, "(FS)"),
    AUTUMN_AND_SPRING(7, "(HS)/(FS)");

    @Getter
    private final int semester;
    @Getter
    private final String description;

    public static ExecutionSemester parseFromString(String semesters) {
        ExecutionSemester executionSemester;
        if (semesters.contains("5;6")) {
            executionSemester = AUTUMN_AND_SPRING;
        } else if (semesters.contains("5.0")) {
            executionSemester = AUTUMN;
        } else {
            executionSemester = SPRING;
        }
        return executionSemester;
    }

    public static ExecutionSemester parseFromInt(int semesters) {
        ExecutionSemester executionSemester;
        if (semesters == 7) {
            executionSemester = AUTUMN_AND_SPRING;
        } else if (semesters == 6) {
            executionSemester = SPRING;
        } else {
            executionSemester = AUTUMN;
        }
        return executionSemester;
    }
}
