package ch.zhaw.vorwahlen.service;

import ch.zhaw.vorwahlen.config.ResourceBundleMessageLoader;
import ch.zhaw.vorwahlen.exception.MailTemplateNotFoundException;
import ch.zhaw.vorwahlen.mapper.Mapper;
import ch.zhaw.vorwahlen.model.dto.MailTemplateDTO;
import ch.zhaw.vorwahlen.model.modules.MailTemplate;
import ch.zhaw.vorwahlen.repository.MailTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static ch.zhaw.vorwahlen.constants.ResourceMessageConstants.ERROR_MAIL_TEMPLATE_NOT_FOUND;

/**
 * CRUD methods for mail templates
 */
@Service
@RequiredArgsConstructor
public class MailTemplateService {
    private final MailTemplateRepository mailTemplateRepository;
    private final Mapper<MailTemplateDTO, MailTemplate> mapper;

    /**
     * Retrieve and map all mail templates from the database
     * @return list of {@link MailTemplateDTO}
     */
    public List<MailTemplateDTO> getAllMailTemplates() {
        return mailTemplateRepository.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    /**
     * Retrieve and map mail template by id
     * @param id of the mail template
     * @return {@link MailTemplateDTO}
     */
    public MailTemplateDTO getMailTemplateById(Long id) {
        return mapper.toDto(fetchMailTemplate(id));
    }

    /**
     * Map mail template to entity and persist in database
     * @param mailTemplateDTO containing mail template data
     * @return MailTemplateDTO
     */
    public MailTemplateDTO createMailTemplate(MailTemplateDTO mailTemplateDTO) {
        var template = mapper.toInstance(mailTemplateDTO);
        template = mailTemplateRepository.save(template);
        return mapper.toDto(template);
    }

    /**
     * Remove mail template by id from database
     * @param id of the mail template
     */
    public void deleteMailTemplateById(Long id) {
        var mailTemplate = fetchMailTemplate(id);
        mailTemplateRepository.delete(mailTemplate);
    }

    /**
     * Map and update mail template content in database
     * @param id of the mail template
     * @param mailTemplateDTO with updated fields
     */
    public void updateMailTemplate(Long id, MailTemplateDTO mailTemplateDTO) {
        var storedMailTemplate = fetchMailTemplate(id);
        var mailTemplate = mapper.toInstance(mailTemplateDTO);
        mailTemplate.setId(storedMailTemplate.getId());
        mailTemplateRepository.save(mailTemplate);
    }

    private MailTemplate fetchMailTemplate(Long id) {
        return mailTemplateRepository
                .findById(id)
                .orElseThrow(() -> {
                    var errorMessage =
                            String.format(ResourceBundleMessageLoader.getMessage(ERROR_MAIL_TEMPLATE_NOT_FOUND), id);
                    return new MailTemplateNotFoundException(errorMessage);
                });
    }
}
