package ch.zhaw.vorwahlen.service;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

@DataJpaTest
@ContextConfiguration
class ElectionServiceTest {
    /* TODO resolve ParameterResolutionException
    private final ElectionRepository electionRepository;
    private final Function<Student, ElectionValidator> validatorFunction;
    private final Function<Student, ModuleDefinition> definitionFunction;
    private final ModuleRepository moduleRepository;
    private final ValidationSettingRepository validationSettingRepository;

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
                               Function<Student, ElectionValidator> validatorFunction,
                               Function<Student, ModuleDefinition> definitionFunction) {
    public ElectionServiceTest(ElectionRepository electionRepository, ModuleRepository moduleRepository, ValidationSettingRepository validationSettingRepository) {
        this.electionRepository = electionRepository;
        this.validatorFunction = validatorFunction;
        this.definitionFunction = definitionFunction;

        this.moduleRepository = moduleRepository;
        this.validationSettingRepository = validationSettingRepository;
    }

    @BeforeEach
    void setUp() {
        electionService = new ElectionService(electionRepository, validatorFunction, definitionFunction);
        electionService = new ElectionService(electionRepository, moduleRepository, validationSettingRepository, structureFullTime, structurePartTime);
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
        assertNull(electionService.getModuleElectionForStudent(student));

        var validElection = validElectionSetForElectionDTO();
        var moduleElection = new ModuleElection();
        var moduleElectionDTO = new ModuleElectionDTO();
        moduleElectionDTO.setElectedModules(validElection);
        moduleElectionDTO.setOverflowedElectedModules(new HashSet<>());
        moduleElectionDTO.setValidationSettingDTO(new ValidationSettingDTO(false, false, false));

        var electedModules = validElection
                .stream()
                .map(s -> Module.builder().moduleNo(s).build())
                .collect(Collectors.toSet());

        moduleElection.setElectedModules(electedModules);
        electionRepository.save(moduleElection);
        var moduleElectionDTO = electionService.getModuleElectionForStudent(student);
        assertNotNull(moduleElectionDTO);

        var recvElectedModules = moduleElectionDTO.getElectedModules().stream().toList();
        var sentElectedModules = validElection.stream().toList();

        Collections.sort(recvElectedModules);
        Collections.sort(sentElectedModules);

        assertIterableEquals(sentElectedModules, recvElectedModules);
        electionService.saveElection(student, moduleElectionDTO);
        assertNotNull(electionService.getModuleElectionByStudent(student));
    }

    private Set<String> validElectionSetForElectionDTO() {
        var set = new HashSet<String>();
        set.addAll(interdisciplinaryModules);
        set.addAll(contextModules);
        set.addAll(subjectModules);
        set.addAll(consecutiveSubjectModules);
        return set;
    }


    /*
    @Test
    @Sql("classpath:sql/election_service_test_user.sql")
    @Sql("classpath:sql/modules_test_election.sql")
    void testSaveElection() {
        var validElection = validElectionSetForElectionDTO();
        var moduleElectionDTO = new ModuleElectionDTO();
        moduleElectionDTO.setElectedModules(validElection);
        moduleElectionDTO.setOverflowedElectedModules(new HashSet<>());
        moduleElectionDTO.setValidationSettingDTO(new ValidationSettingDTO(false, false, false));

        var jsonNode = new ObjectMapper().createObjectNode();
        jsonNode.put("electionSaved", true);
        jsonNode.put("electionValid", true);



        assertEquals(jsonNode, electionService.saveElection(student, moduleElection));
        assertTrue(moduleElection.isElectionValid());
        assertEquals(jsonNode, electionService.saveElection(student, moduleElectionDTO));
        assertTrue(moduleElectionDTO.isElectionValid());

        jsonNode.put("electionValid", false);
        moduleElectionDTO.setOverflowedElectedModules(
                moduleRepository.findAll().stream()
                        .filter(module -> ModuleCategory.parse(module.getModuleNo(), module.getModuleGroup()) == ModuleCategory.INTERDISCIPLINARY_MODULE)
                        .filter(module -> !validElection.contains(module.getModuleNo()))
                        .findAny()
                        .map(Module::getModuleNo)
                        .stream()
                        .collect(Collectors.toSet()));

        assertFalse(moduleElectionDTO.getOverflowedElectedModules().isEmpty());
        assertEquals(jsonNode, electionService.saveElection(student, moduleElectionDTO));
        assertFalse(moduleElectionDTO.isElectionValid());
    }

    @Test
    @Sql("classpath:sql/election_service_test_user.sql")
    @Sql("classpath:sql/modules_test_election.sql")
    void testSaveElectionWithTwoUsers() {
        // student 1
        assertNull(electionService.getModuleElectionByStudent(student));

        var validElection = validElectionSetForElectionDTO();
        var moduleElectionDTO = new ModuleElectionDTO();
        moduleElectionDTO.setElectedModules(validElection);
        moduleElectionDTO.setOverflowedElectedModules(new HashSet<>());
        moduleElectionDTO.setValidationSettingDTO(new ValidationSettingDTO(false, false, false));

        electionService.saveElection(student, moduleElectionDTO);
        assertNotNull(electionService.getModuleElectionByStudent(student));
        assertEquals(1, validationSettingRepository.count());

        moduleElectionDTO.setValidationSettingDTO(new ValidationSettingDTO(false, true, false));
        assertEquals(1, validationSettingRepository.count());

        // student 2
        assertNull(electionService.getModuleElectionByStudent(student2));

        var validElection2 = validElectionSetForElectionDTO();
        var moduleElectionDTO2 = new ModuleElectionDTO();
        moduleElectionDTO2.setElectedModules(validElection2);
        moduleElectionDTO2.setOverflowedElectedModules(new HashSet<>());
        moduleElectionDTO2.setValidationSettingDTO(new ValidationSettingDTO(false, false, false));

        electionService.saveElection(student2, moduleElectionDTO2);
        assertNotNull(electionService.getModuleElectionByStudent(student2));
        assertEquals(2, validationSettingRepository.count());

        moduleElectionDTO2.setValidationSettingDTO(new ValidationSettingDTO(false, true, false));
        electionService.saveElection(student2, moduleElectionDTO2);
        assertEquals(2, validationSettingRepository.count());
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

     */

}
