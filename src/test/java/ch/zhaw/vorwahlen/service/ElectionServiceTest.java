package ch.zhaw.vorwahlen.service;

import ch.zhaw.vorwahlen.model.dto.ModuleElectionDTO;
import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.model.modules.ModuleCategory;
import ch.zhaw.vorwahlen.model.modules.Student;
import ch.zhaw.vorwahlen.model.modulestructure.ModuleStructureFullTime;
import ch.zhaw.vorwahlen.model.modulestructure.ModuleStructurePartTime;
import ch.zhaw.vorwahlen.repository.ElectionRepository;
import ch.zhaw.vorwahlen.repository.ModuleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class ElectionServiceTest {


    private final ElectionRepository electionRepository;
    private final ModuleRepository moduleRepository;

    @Autowired
    private ModuleStructureFullTime structureFullTime;
    @Autowired
    private ModuleStructurePartTime structurePartTime;

    private ElectionService electionService;

    private final Student student = Student.builder().email("test@mail.com").build();

    private final List<String> interdisciplinaryModules = List.of("t.BA.WM.PHMOD.19HS");
    private final List<String> contextModules = List.of("t.BA.XXK.FUPRE.19HS", "t.BA.WVK.ZURO.20HS", "t.BA.WVK.SIC-TAF.20HS");
    private final List<String> subjectModules = List.of("t.BA.WV.ESE.19HS", "t.BA.WV.FUP.19HS");
    private final List<String> consecutiveSubjectModules = List.of(
            "t.BA.WV.AI2-EN.19HS",
            "t.BA.WV.AI1-EN.19HS",
            "t.BA.WV.CCP2-EN.19HS",
            "t.BA.WV.CCP1-EN.19HS",
            "t.BA.WV.DIP2-EN.20HS",
            "t.BA.WV.DIP-EN.19HS"
    );

    @Autowired
    public ElectionServiceTest(ElectionRepository electionRepository, ModuleRepository moduleRepository) {
        this.electionRepository = electionRepository;
        this.moduleRepository = moduleRepository;
    }

    @BeforeEach
    void setUp() {
        electionService = new ElectionService(electionRepository, moduleRepository, structureFullTime, structurePartTime);
    }

    @AfterEach
    void tearDown() {
    }

    // ================================================================================================================
    // Positive tests
    // ================================================================================================================

    @Test
    @Sql("classpath:sql/election_service_test_user.sql")
    @Sql("classpath:sql/modules_test_election.sql")
    void testGetModuleElectionByStudent() {
        assertNull(electionService.getModuleElectionByStudent(student));

        var validElection = validElectionSetForElectionDTO();
        var moduleElection = new ModuleElectionDTO();
        moduleElection.setElectedModules(validElection);
        moduleElection.setOverflowedElectedModules(new HashSet<>());

        electionService.saveElection(student, moduleElection);
        assertNotNull(electionService.getModuleElectionByStudent(student));
    }

    @Test
    @Sql("classpath:sql/election_service_test_user.sql")
    @Sql("classpath:sql/modules_test_election.sql")
    void testSaveElection() {
        var validElection = validElectionSetForElectionDTO();
        var moduleElection = new ModuleElectionDTO();
        moduleElection.setElectedModules(validElection);
        moduleElection.setOverflowedElectedModules(new HashSet<>());

        var jsonNode = new ObjectMapper().createObjectNode();
        jsonNode.put("electionSaved", true);
        jsonNode.put("electionValid", true);

        assertEquals(jsonNode, electionService.saveElection(student, moduleElection));
        assertTrue(moduleElection.isElectionValid());

        jsonNode.put("electionValid", false);
        moduleElection.setOverflowedElectedModules(
                moduleRepository.findAll().stream()
                        .filter(module -> ModuleCategory.parse(module.getModuleNo(), module.getModuleGroup()) == ModuleCategory.INTERDISCIPLINARY_MODULE)
                        .filter(module -> !validElection.contains(module.getModuleNo()))
                        .findAny()
                        .map(Module::getModuleNo)
                        .stream()
                        .collect(Collectors.toSet()));

        assertFalse(moduleElection.getOverflowedElectedModules().isEmpty());
        assertEquals(jsonNode, electionService.saveElection(student, moduleElection));
        assertFalse(moduleElection.isElectionValid());
    }

    private Set<String> validElectionSetForElectionDTO() {
        var set = new HashSet<String>();
        set.addAll(interdisciplinaryModules);
        set.addAll(contextModules);
        set.addAll(subjectModules);
        set.addAll(consecutiveSubjectModules);
        return set;
    }

    // ================================================================================================================
    // Negative tests
    // ================================================================================================================

    @Test
    @Sql("classpath:sql/modules_test_election.sql")
    void testSaveElection_Null_Student() {
        var validElection = validElectionSetForElectionDTO();
        var moduleElectionDTO = new ModuleElectionDTO();
        moduleElectionDTO.setElectedModules(validElection);
        moduleElectionDTO.setOverflowedElectedModules(new HashSet<>());

        var jsonNode = new ObjectMapper().createObjectNode();
        jsonNode.put("electionSaved", false);
        jsonNode.put("electionValid", false);

        assertEquals(jsonNode, electionService.saveElection(null, moduleElectionDTO));
    }

    @Test
    void testSaveElection_Null_Election() {
        var jsonNode = new ObjectMapper().createObjectNode();
        jsonNode.put("electionSaved", false);
        jsonNode.put("electionValid", false);

        assertEquals(jsonNode, electionService.saveElection(student, null));
    }

    @Test
    @Sql("classpath:sql/modules_test_election.sql")
    void testSaveElection_Null_Email() {
        var student = Student.builder().email(null).build();
        var validElection = validElectionSetForElectionDTO();
        var moduleElectionDTO = new ModuleElectionDTO();
        moduleElectionDTO.setElectedModules(validElection);
        moduleElectionDTO.setOverflowedElectedModules(new HashSet<>());

        var jsonNode = new ObjectMapper().createObjectNode();
        jsonNode.put("electionSaved", false);
        jsonNode.put("electionValid", false);

        assertEquals(jsonNode, electionService.saveElection(student, moduleElectionDTO));
    }

    @Test
    @Sql("classpath:sql/modules_test_election.sql")
    void testSaveElection_Blank_Email() {
        var student = Student.builder().email("").build();
        var validElection = validElectionSetForElectionDTO();
        var moduleElectionDTO = new ModuleElectionDTO();
        moduleElectionDTO.setElectedModules(validElection);
        moduleElectionDTO.setOverflowedElectedModules(new HashSet<>());

        var jsonNode = new ObjectMapper().createObjectNode();
        jsonNode.put("electionSaved", false);
        jsonNode.put("electionValid", false);

        assertEquals(jsonNode, electionService.saveElection(student, moduleElectionDTO));
    }

}
