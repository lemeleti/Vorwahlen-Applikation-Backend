package ch.zhaw.vorwahlen.service;

import ch.zhaw.vorwahlen.exception.MailTemplateConflictException;
import ch.zhaw.vorwahlen.exception.MailTemplateNotFoundException;
import ch.zhaw.vorwahlen.mapper.Mapper;
import ch.zhaw.vorwahlen.model.dto.MailTemplateDTO;
import ch.zhaw.vorwahlen.model.modules.MailTemplate;
import ch.zhaw.vorwahlen.repository.MailTemplateRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
class MailTemplateServiceTest {

    public static final long DTO_ID_1 = 1L;
    public static final long NON_EXISTING_ID = 9999L;
    private final MailTemplateRepository mailTemplateRepository;
    private final Mapper<MailTemplateDTO, MailTemplate> mapper;

    private MailTemplateService mailTemplateService;

    private MailTemplateDTO dto;

    @Autowired
    public MailTemplateServiceTest(MailTemplateRepository mailTemplateRepository, Mapper<MailTemplateDTO, MailTemplate> mapper) {
        this.mailTemplateRepository = mailTemplateRepository;
        this.mapper = mapper;
    }

    @BeforeEach
    void setUp() {
        mailTemplateService = new MailTemplateService(mailTemplateRepository, mapper);
        dto = new MailTemplateDTO(DTO_ID_1, "descrption 1", "subject 1", "message 1");
    }

    @AfterEach
    void tearDown() {
        mailTemplateRepository.deleteAll();
    }

    @Test
    void testCreateMailTemplate() {
        assertEquals(0, mailTemplateRepository.count());
        mailTemplateService.createMailTemplate(dto);
        assertEquals(1, mailTemplateRepository.count());
    }

    @Test
    void testGetMailTemplateById() {
        var created = mailTemplateService.createMailTemplate(dto);
        var result = mailTemplateService.getMailTemplateById(created.id());
        assertEquals(created, result);
    }

    @Test
    void testGetAllMailTemplates() {
        var created = mailTemplateService.createMailTemplate(dto);
        var dto2 = new MailTemplateDTO(created.id() + 1, "descrption 2", "subject 2", "message 2");
        var created2 = mailTemplateService.createMailTemplate(dto2);

        var result = mailTemplateService.getAllMailTemplates();

        assertThat(result, anyOf(is(List.of(created, created2)),
                                 is(List.of(created2, created))));
    }

    @Test
    void testUpdateMailTemplate() {
        var created = mailTemplateService.createMailTemplate(dto);
        var updatedDto = new MailTemplateDTO(created.id(), "new descrption 1", "new subject 1", "new message 1");
        mailTemplateService.updateMailTemplate(created.id(), updatedDto);
        var result = mailTemplateService.getMailTemplateById(created.id());
        assertEquals(updatedDto, result);
    }

    @Test
    void testDeleteMailTemplateById() {
        var dto2 = new MailTemplateDTO(2L, "descrption 2", "subject 2", "message 2");
        var created = mailTemplateService.createMailTemplate(dto);
        var created2 = mailTemplateService.createMailTemplate(dto2);

        assertEquals(2, mailTemplateRepository.count());
        mailTemplateService.deleteMailTemplateById(created.id());
        assertEquals(1, mailTemplateRepository.count());
        mailTemplateService.deleteMailTemplateById(created2.id());
        assertEquals(0, mailTemplateRepository.count());
    }

    @Test
    void testDeleteMailTemplateById_NonExistentId() {
        assertThrows(MailTemplateNotFoundException.class, () -> mailTemplateService.deleteMailTemplateById(NON_EXISTING_ID));
    }

    @Test
    void testCreateMailTemplate_AlreadyExisting() {
        var created = assertDoesNotThrow(() -> mailTemplateService.createMailTemplate(dto));
        assertThrows(MailTemplateConflictException.class, () -> mailTemplateService.createMailTemplate(created));
    }

}
