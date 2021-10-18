package ch.zhaw.vorwahlen.service;

import ch.zhaw.vorwahlen.repository.ClassListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ClassListServiceTest {

    private final ClassListRepository classListRepository;
    private ClassListService classListService;

    @Autowired
    public ClassListServiceTest(ClassListRepository classListRepository) {
        this.classListRepository = classListRepository;
    }

    @BeforeEach
    void setUp() {
        classListService = new ClassListService(classListRepository);
    }

    @Test
    @Sql("classpath:sql/class_list.sql")
    void testGetAllClassLists() {
        assertFalse(classListService.getAllClassLists().isEmpty());
    }

}
