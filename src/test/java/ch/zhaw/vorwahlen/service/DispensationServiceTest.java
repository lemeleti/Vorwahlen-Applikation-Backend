package ch.zhaw.vorwahlen.service;

import ch.zhaw.vorwahlen.repository.ClassListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
class DispensationServiceTest {

    private final ClassListRepository classListRepository;
    private DispensationService dispensationService;

    @Autowired
    public DispensationServiceTest(ClassListRepository classListRepository) {
        this.classListRepository = classListRepository;
    }

    @BeforeEach
    void setUp() {
        dispensationService = new DispensationService(classListRepository);
    }

    /*@Test
    @Sql("classpath:sql/class_list.sql")
    void testGetAllClassLists() {
        assertFalse(dispensationService.getAllClassLists().isEmpty());
    }
*/
}
