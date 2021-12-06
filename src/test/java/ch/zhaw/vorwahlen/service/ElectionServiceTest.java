package ch.zhaw.vorwahlen.service;

import ch.zhaw.vorwahlen.exporter.ModuleElectionExporter;
import ch.zhaw.vorwahlen.mapper.Mapper;
import ch.zhaw.vorwahlen.model.dto.ElectionStatusDTO;
import ch.zhaw.vorwahlen.model.dto.ElectionTransferDTO;
import ch.zhaw.vorwahlen.model.dto.ModuleElectionDTO;
import ch.zhaw.vorwahlen.model.modules.*;
import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.model.modulestructure.ModuleDefinition;
import ch.zhaw.vorwahlen.model.modulestructure.ModuleStructureElement;
import ch.zhaw.vorwahlen.repository.StudentClassRepository;
import ch.zhaw.vorwahlen.repository.StudentRepository;
import ch.zhaw.vorwahlen.security.model.User;
import ch.zhaw.vorwahlen.validation.ElectionValidator;
import ch.zhaw.vorwahlen.repository.ElectionRepository;
import ch.zhaw.vorwahlen.repository.ModuleRepository;
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

import static java.util.function.Predicate.*;
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
    private final StudentClassRepository studentClassRepository;
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
                               StudentClassRepository studentClassRepository,
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
        this.studentClassRepository = studentClassRepository;
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
        studentClassRepository.deleteAll();
        validationSettingRepository.deleteAll();
    }

    // ================================================================================================================
    // Positive tests
    // ================================================================================================================

    @Test
    @Sql("classpath:sql/election_service_test_user.sql")
    @Sql("classpath:sql/modules_test_election.sql")
    void testExportModuleElection() {
        // prepare
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

        // execute
        var result = electionService.exportModuleElection();

        // verify
        assertNotNull(result);
    }

    @Test
    @Sql("classpath:sql/election_service_test_user.sql")
    @Sql("classpath:sql/modules_test_election.sql")
    void testGetElection_Fulltime() {
        testGetElection(student, validElectionSetForElection(), 12);
    }

    @Test
    @Sql("classpath:sql/election_service_test_user.sql")
    @Sql("classpath:sql/modules_test_election.sql")
    void testGetElection_ParttimeFirst() {
        var student = Student.builder()
                .email(this.student.getEmail())
                .isTZ(true)
                .isSecondElection(false)
                .build();

        var validElection = Set.of(consecutiveSubjectModules.get(0),
                                   consecutiveSubjectModules.get(1),
                                   contextModules.get(0),
                                   contextModules.get(1));

        testGetElection(student, validElection, 4);
    }

    @Test
    @Sql("classpath:sql/election_service_test_user.sql")
    @Sql("classpath:sql/modules_test_election.sql")
    void testGetElection_ParttimeSecond() {
        var student = Student.builder()
                .email(this.student.getEmail())
                .isTZ(true)
                .isSecondElection(true)
                .build();

        var validElection = Set.of(consecutiveSubjectModules.get(0),
                                   consecutiveSubjectModules.get(1),
                                   consecutiveSubjectModules.get(2),
                                   consecutiveSubjectModules.get(3),
                                   consecutiveSubjectModules.get(4),
                                   consecutiveSubjectModules.get(5),
                                   contextModules.get(0),
                                   interdisciplinaryModules.get(0));

        testGetElection(student, validElection, 8);
    }

    private void testGetElection(Student student, Set<String> validElection, int expectedElectedModulesSize) {
        // prepare
        setAuthentication(student);
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

        // execute
        var resultDTO = electionService.getElection(student);

        // verify
        assertNotNull(resultDTO);

        assertNotNull(resultDTO.electionStatusDTO());
        assertNotNull(resultDTO.electionStatusDTO().additionalValidation());
        assertNotNull(resultDTO.electionStatusDTO().contextValidation());
        assertNotNull(resultDTO.electionStatusDTO().subjectValidation());
        assertNotNull(resultDTO.electionStatusDTO().interdisciplinaryValidation());

        assertTrue(resultDTO.electionStatusDTO().additionalValidation().isValid());
        assertTrue(resultDTO.electionStatusDTO().contextValidation().isValid());
        assertTrue(resultDTO.electionStatusDTO().subjectValidation().isValid());
        assertTrue(resultDTO.electionStatusDTO().interdisciplinaryValidation().isValid());

        assertNull(resultDTO.electionStatusDTO().additionalValidation().reasons());
        assertNull(resultDTO.electionStatusDTO().contextValidation().reasons());
        assertNull(resultDTO.electionStatusDTO().subjectValidation().reasons());
        assertNull(resultDTO.electionStatusDTO().interdisciplinaryValidation().reasons());

        assertNull(resultDTO.electionStatusDTO().additionalValidation().moduleCategory());
        assertEquals(ModuleCategory.CONTEXT_MODULE, resultDTO.electionStatusDTO().contextValidation().moduleCategory());
        assertEquals(ModuleCategory.SUBJECT_MODULE, resultDTO.electionStatusDTO().subjectValidation().moduleCategory());
        assertEquals(ModuleCategory.INTERDISCIPLINARY_MODULE, resultDTO.electionStatusDTO().interdisciplinaryValidation().moduleCategory());

        assertNotNull(resultDTO.electionStructure());
        assertNotNull(resultDTO.electionStructure().electedModules());
        assertNotNull(resultDTO.electionStructure().overflowedModules());

        var electedModulesWithoutPlaceHolder = resultDTO.electionStructure().electedModules().stream()
                                .filter(not(ModuleStructureElement::isPlaceholder))
                                .collect(Collectors.toList());

        assertEquals(0, resultDTO.electionStructure().overflowedModules().size());
        assertEquals(expectedElectedModulesSize, electedModulesWithoutPlaceHolder.size());

        var countPlaceholder = resultDTO.electionStructure().electedModules()
                .stream()
                .filter(moduleStructureElement -> {
                    var category =  moduleStructureElement.category();
                    return ModuleCategory.PROJECT_MODULE != category && ModuleCategory.BACHELOR_MODULE != category;
                })
                .filter(ModuleStructureElement::isPlaceholder)
                .count();
        assertEquals(0, countPlaceholder);

        assertFalse(resultDTO.electionSaved());
    }

    @Test
    @Sql("classpath:sql/election_service_test_user.sql")
    @Sql("classpath:sql/modules_test_election.sql")
    void testGetModuleElectionByStudent() {
        // prepare
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

        // execute
        var moduleElectionDTO = electionService.getModuleElectionForStudent(student);

        // verify
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
