package ch.zhaw.vorwahlen.service;

import ch.zhaw.vorwahlen.exporter.ModuleElectionExporter;
import ch.zhaw.vorwahlen.mapper.Mapper;
import ch.zhaw.vorwahlen.model.dto.ElectionStatusDTO;
import ch.zhaw.vorwahlen.model.dto.ElectionTransferDTO;
import ch.zhaw.vorwahlen.model.dto.ModuleElectionDTO;
import ch.zhaw.vorwahlen.model.modules.*;
import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.model.modulestructure.ModuleDefinition;
import ch.zhaw.vorwahlen.security.model.User;
import ch.zhaw.vorwahlen.modulevalidation.ElectionValidator;
import ch.zhaw.vorwahlen.repository.ElectionRepository;
import ch.zhaw.vorwahlen.repository.ModuleRepository;
import ch.zhaw.vorwahlen.repository.StudentRepository;
import ch.zhaw.vorwahlen.repository.ValidationSettingRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
class ElectionServiceTest {
    private final ElectionRepository electionRepository;
    private final ElectionValidator validator;
    private final ModuleDefinition moduleDefinition;
    private final ModuleRepository moduleRepository;
    private final ModuleElectionExporter exporter;
    private final ValidationSettingRepository validationSettingRepository;
    private final StudentRepository studentRepository;
    private final ElectionSemesters electionSemesters;
    private final Mapper<ModuleElectionDTO, ModuleElection> moduleElectionMapper;
    private final Mapper<ElectionStatusDTO, ModuleElectionStatus> electionStatusMapper;

    private ElectionService electionService;

    private final Student student = Student.builder().email("test@mail.com").build();
    private final Student student2 = Student.builder().email("test2@mail.com").build();

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
    public ElectionServiceTest(ElectionRepository electionRepository,
                               ElectionValidator validator,
                               ModuleDefinition moduleDefinition,
                               ModuleRepository moduleRepository,
                               ModuleElectionExporter exporter,
                               ValidationSettingRepository validationSettingRepository,
                               StudentRepository studentRepository,
                               ElectionSemesters electionSemesters,
                               Mapper<ModuleElectionDTO, ModuleElection> moduleElectionMapper,
                               Mapper<ElectionStatusDTO, ModuleElectionStatus> electionStatusMapper) {
        this.electionRepository = electionRepository;
        this.validator = validator;
        this.moduleDefinition = moduleDefinition;
        this.moduleRepository = moduleRepository;
        this.exporter = exporter;
        this.validationSettingRepository = validationSettingRepository;
        this.studentRepository = studentRepository;
        this.electionSemesters = electionSemesters;
        this.moduleElectionMapper = moduleElectionMapper;
        this.electionStatusMapper = electionStatusMapper;
    }

    @BeforeEach
    void setUp() {
        electionService = new ElectionService(electionRepository, moduleRepository,
                validator, moduleDefinition, exporter, electionSemesters, moduleElectionMapper, electionStatusMapper);
    }

    @AfterEach
    void tearDown() {
        electionRepository.deleteAll();
        studentRepository.deleteAll();
        moduleRepository.deleteAll();
    }

    // ================================================================================================================
    // Positive tests
    // ================================================================================================================

    @Test
    @Sql("classpath:sql/election_service_test_user.sql")
    @Sql("classpath:sql/modules_test_election.sql")
    void testGetModuleElectionByStudent() {
        var validElection = validElectionSetForElection();
        var moduleElection = new ModuleElection();
        var validationSetting = new ValidationSetting();

        var electedModules = validElection
                .stream()
                .map(s -> Module.builder().moduleNo(s).build())
                .collect(Collectors.toSet());

        moduleElection.setElectedModules(electedModules);
        moduleElection.setValidationSetting(validationSetting);
        moduleElection.setStudent(student);
        electionRepository.save(moduleElection);
        var moduleElectionDTO = electionService.getModuleElectionForStudent(student);
        assertNotNull(moduleElectionDTO);

        var recvElectedModules = new ArrayList<>(moduleElectionDTO.getElectedModules());
        var sentElectedModules = new ArrayList<>(validElection);

        Collections.sort(recvElectedModules);
        Collections.sort(sentElectedModules);

        assertIterableEquals(sentElectedModules, recvElectedModules);
    }


    @Test
    @Sql("classpath:sql/election_service_test_user.sql")
    @Sql("classpath:sql/modules_test_election.sql")
    void testSaveElection() {
        var validElection = validElectionSetForElection();
        ElectionTransferDTO electionTransferDTO = null;
        setAuthentication(student);

        assertFalse(electionRepository.findModuleElectionByStudent(student.getEmail()).isPresent());

        for (String moduleNo : validElection) {
            electionTransferDTO = electionService.saveElection(student, moduleNo);
        }

        assertNotNull(electionTransferDTO);
        assertTrue(electionTransferDTO.electionSaved());
        assertTrue(electionTransferDTO.electionValid());

        var recvElectedModules = new ArrayList<>(electionService.getModuleElectionForStudent(student).getElectedModules());
        var sentElectedModules = new ArrayList<>(validElection);

        Collections.sort(recvElectedModules);
        Collections.sort(sentElectedModules);

        assertIterableEquals(sentElectedModules, recvElectedModules);
    }

    @Test
    @Sql("classpath:sql/election_service_test_user.sql")
    @Sql("classpath:sql/modules_test_election.sql")
    void testSaveElectionWithTwoUsers() {
        // student 1
        var validElection = validElectionSetForElection();
        assertFalse(electionRepository.findModuleElectionByStudent(student.getEmail()).isPresent());
        setAuthentication(student);

        for (String moduleNo : validElection) {
            electionService.saveElection(student, moduleNo);
        }

        assertEquals(12, electionService.getModuleElectionForStudent(student).getElectedModules().size());
        assertEquals(1, validationSettingRepository.count());

        // student 2
        assertFalse(electionRepository.findModuleElectionByStudent(student2.getEmail()).isPresent());
        setAuthentication(student2);

        for (String moduleNo : validElection) {
            electionService.saveElection(student2, moduleNo);
        }

        assertEquals(12, electionService.getModuleElectionForStudent(student2).getElectedModules().size());
        assertEquals(2, validationSettingRepository.count());
    }


    private Set<String> validElectionSetForElection() {
        var set = new HashSet<String>();
        set.addAll(interdisciplinaryModules);
        set.addAll(contextModules);
        set.addAll(subjectModules);
        set.addAll(consecutiveSubjectModules);
        return set;
    }

    private void setAuthentication(Student student) {
        var user = User.builder().student(student).build();
        SecurityContextHolder.getContext().setAuthentication(new PreAuthenticatedAuthenticationToken(user, null));
    }
}
