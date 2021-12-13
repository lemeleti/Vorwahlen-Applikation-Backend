package ch.zhaw.vorwahlen.service;

import ch.zhaw.vorwahlen.exception.ModuleElectionConflictException;
import ch.zhaw.vorwahlen.exception.ModuleElectionNotFoundException;
import ch.zhaw.vorwahlen.exporter.ModuleElectionExporter;
import ch.zhaw.vorwahlen.mapper.Mapper;
import ch.zhaw.vorwahlen.model.dto.ElectionStatusDTO;
import ch.zhaw.vorwahlen.model.dto.ElectionTransferDTO;
import ch.zhaw.vorwahlen.model.dto.ModuleElectionDTO;
import ch.zhaw.vorwahlen.model.modules.ElectionSemesters;
import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.model.modules.ModuleCategory;
import ch.zhaw.vorwahlen.model.modules.ModuleElection;
import ch.zhaw.vorwahlen.model.modules.ModuleElectionStatus;
import ch.zhaw.vorwahlen.model.modules.Student;
import ch.zhaw.vorwahlen.model.modules.ValidationSetting;
import ch.zhaw.vorwahlen.model.modulestructure.ModuleDefinition;
import ch.zhaw.vorwahlen.model.modulestructure.ModuleStructureElement;
import ch.zhaw.vorwahlen.repository.ElectionRepository;
import ch.zhaw.vorwahlen.repository.ModuleRepository;
import ch.zhaw.vorwahlen.repository.StudentClassRepository;
import ch.zhaw.vorwahlen.repository.StudentRepository;
import ch.zhaw.vorwahlen.repository.ValidationSettingRepository;
import ch.zhaw.vorwahlen.security.model.User;
import ch.zhaw.vorwahlen.validation.ElectionValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.MessageChannel;
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

import static java.util.function.Predicate.not;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
class ElectionServiceTest {

    public static final long NON_EXISTENT_ID = 9999L;
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

    private final MessageChannel messageChannel;

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
                               Mapper<ElectionStatusDTO, ModuleElectionStatus> electionStatusMapper,
                               @Qualifier("clientOutboundChannel") MessageChannel messageChannel) {
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
        this.messageChannel = messageChannel;
    }

    @BeforeEach
    void setUp() {
        electionService = new ElectionService(electionRepository, moduleRepository,studentRepository,
                                              validator, moduleDefinition, exporter, electionSemesters,
                                              moduleElectionMapper, electionStatusMapper, messageChannel);
    }

    @AfterEach
    void tearDown() {
        for (var student: studentRepository.findAll()){
            if(student.getElection() != null) {
                student.setElection(null);
                studentRepository.save(student);
            }
        }

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
    void testGetAllModuleElections() {
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
        var result = electionService.getAllModuleElections();
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @Sql("classpath:sql/election_service_test_user.sql")
    @Sql("classpath:sql/modules_test_election.sql")
    void testGetModuleElectionById() {
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
        moduleElection = electionRepository.save(moduleElection);

        // execute
        var result = electionService.getModuleElectionById(moduleElection.getId());
        assertNotNull(result);
    }

    @Test
    @Sql("classpath:sql/election_service_test_user.sql")
    @Sql("classpath:sql/modules_test_election.sql")
    void testCreateModuleElection() {
        // prepare
        var moduleElectionDto = ModuleElectionDTO.builder()
                .studentEmail(student.getEmail())
                .electedModules(validElectionSetForElection())
                .build();

        // execute
        var count = electionRepository.count();
        electionService.createModuleElection(moduleElectionDto);
        assertEquals(count + 1, electionRepository.count());
    }

    @Test
    @Sql("classpath:sql/election_service_test_user.sql")
    @Sql("classpath:sql/modules_test_election.sql")
    void testDeleteModuleElectionById() {
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

        var count = electionRepository.count();
        moduleElection = electionRepository.save(moduleElection);
        assertEquals(count + 1, electionRepository.count());

        // execute
        electionService.deleteModuleElectionById(moduleElection.getId());
        assertEquals(count, electionRepository.count());
    }

    @Test
    @Sql("classpath:sql/modules_test_election.sql")
    void testUpdateModuleElection() {
        // prepare
        var electedModules = validElectionSetForElection()
                .stream()
                .map(s -> Module.builder().moduleNo(s).build())
                .collect(Collectors.toSet());

        var moduleElection = new ModuleElection();
        moduleElection.setElectedModules(electedModules);
        moduleElection.setValidationSetting(new ValidationSetting());
        moduleElection.setStudent(student);

        student.setElection(moduleElection);
        studentRepository.save(student);

        var optionalModuleElection = electionRepository.findModuleElectionByStudent(student.getEmail());
        assertTrue(optionalModuleElection.isPresent());
        assertTrue(validationSettingRepository.findValidationSettingByStudentMail(student.getEmail()).isPresent());

        moduleElection = optionalModuleElection.get();

        assertFalse(moduleElection.isElectionValid());

        var dto = moduleElectionMapper.toDto(moduleElection);
        dto.setElectionValid(true);

        // execute
        electionService.updateModuleElection(moduleElection.getId(), dto);

        // verify
        var election = electionService.getModuleElectionById(moduleElection.getId());
        assertTrue(election.isElectionValid());
    }

    @Test
    @Sql("classpath:sql/election_service_test_user.sql")
    @Sql("classpath:sql/modules_test_election.sql")
    void testExportModuleElection() {
        // prepare
        var electedModules = validElectionSetForElection()
                .stream()
                .map(s -> Module.builder().moduleNo(s).build())
                .collect(Collectors.toSet());

        var moduleElection = new ModuleElection();
        moduleElection.setElectedModules(electedModules);
        moduleElection.setValidationSetting(new ValidationSetting());
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
        testGetElection(student, validElectionSetForElection(), 12, new ValidationSetting());
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

        testGetElection(student, validElection, 4, new ValidationSetting());
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

        var validationSetting = new ValidationSetting();
        validationSetting.setElectedContextModulesInFirstElection(2);
        testGetElection(student, validElection, 8, validationSetting);
    }

    private void testGetElection(Student student, Set<String> validElection, int expectedElectedModulesSize, ValidationSetting validationSetting) {
        // prepare
        setAuthentication(student);
        var electedModules = validElection
                .stream()
                .map(s -> Module.builder().moduleNo(s).build())
                .collect(Collectors.toSet());

        var moduleElection = new ModuleElection();
        moduleElection.setElectedModules(electedModules);
        moduleElection.setValidationSetting(validationSetting);
        moduleElection.setStudent(student);
        moduleElection = electionRepository.save(moduleElection);

        student.setElection(moduleElection);
        student = studentRepository.save(student);

        // execute
        var resultDTO = electionService.getElection(student.getEmail());

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
        var electedModules = validElection
                .stream()
                .map(s -> Module.builder().moduleNo(s).build())
                .collect(Collectors.toSet());

        var moduleElection = new ModuleElection();
        moduleElection.setElectedModules(electedModules);
        moduleElection.setValidationSetting(new ValidationSetting());
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
            electionTransferDTO = electionService.saveElection(student.getEmail(), moduleNo, null);
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
            electionService.saveElection(student.getEmail(), moduleNo, null);
        }

        assertEquals(12, electionService.getModuleElectionForStudent(student).getElectedModules().size());
        assertEquals(1, validationSettingRepository.count());

        // student 2
        assertFalse(electionRepository.findModuleElectionByStudent(student2.getEmail()).isPresent());
        setAuthentication(student2);

        for (String moduleNo : validElection) {
            electionService.saveElection(student2.getEmail(), moduleNo, null);
        }

        assertEquals(12, electionService.getModuleElectionForStudent(student2).getElectedModules().size());
        assertEquals(2, validationSettingRepository.count());
    }

    @Test
    @Sql("classpath:sql/election_service_test_user.sql")
    void testCreateModuleElection_AlreadyExists() {
        // prepare
        var moduleElectionDto = ModuleElectionDTO.builder()
                .studentEmail(student.getEmail())
                .electedModules(validElectionSetForElection())
                .build();

        var created = assertDoesNotThrow(() -> electionService.createModuleElection(moduleElectionDto));
        assertThrows(ModuleElectionConflictException.class, () -> electionService.createModuleElection(created));
    }

    @Test
    void testGetModuleElectionById_NonExistentId() {
        assertThrows(ModuleElectionNotFoundException.class, () -> electionService.getModuleElectionById(NON_EXISTENT_ID));
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
        var user = User.builder().mail(student.getEmail()).isExistent(true).build();
        SecurityContextHolder.getContext().setAuthentication(new PreAuthenticatedAuthenticationToken(user, null));
    }
}
