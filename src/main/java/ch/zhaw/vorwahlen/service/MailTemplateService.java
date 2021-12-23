package ch.zhaw.vorwahlen.service;

import ch.zhaw.vorwahlen.config.ResourceBundleMessageLoader;
import ch.zhaw.vorwahlen.config.UserBean;
import ch.zhaw.vorwahlen.constants.ResourceMessageConstants;
import ch.zhaw.vorwahlen.exception.MailTemplateConflictException;
import ch.zhaw.vorwahlen.exception.MailTemplateNotFoundException;
import ch.zhaw.vorwahlen.mapper.Mapper;
import ch.zhaw.vorwahlen.model.mailtemplate.MailTemplateDTO;
import ch.zhaw.vorwahlen.model.mailtemplate.MailTemplate;
import ch.zhaw.vorwahlen.repository.MailTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static ch.zhaw.vorwahlen.constants.ResourceMessageConstants.ERROR_MAIL_TEMPLATE_NOT_FOUND;

/**
 * CRUD methods for mail templates
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MailTemplateService {
    private final MailTemplateRepository mailTemplateRepository;
    private final Mapper<MailTemplateDTO, MailTemplate> mapper;
    private final UserBean userBean;

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
        userBean.getUserFromSecurityContext().ifPresent(user ->
            log.debug("User: {} requested to add a mail template: {}", user.getMail(), mailTemplateDTO)
        );
        if(mailTemplateRepository.existsById(mailTemplateDTO.id())) {
            log.debug("Throwing MailTemplateConflictException because mail template with id {} already exists", mailTemplateDTO.id());
            var formatString = ResourceBundleMessageLoader.getMessage(ResourceMessageConstants.ERROR_MAIL_TEMPLATE_CONFLICT);
            var message = String.format(formatString, mailTemplateDTO.id());
            throw new MailTemplateConflictException(message);
        }
        var template = mapper.toInstance(mailTemplateDTO);
        template = mailTemplateRepository.save(template);
        log.debug("Mail template: {} was saved successfully to the database", template);
        return mapper.toDto(template);
    }

    /**
     * Remove mail template by id from database
     * @param id of the mail template
     */
    public void deleteMailTemplateById(Long id) {
        userBean.getUserFromSecurityContext().ifPresent(user ->
            log.debug("User: {} requested to delete a mail template with id: {}", user.getMail(), id)
        );
        var mailTemplate = fetchMailTemplate(id);
        mailTemplateRepository.delete(mailTemplate);
        log.debug("mail template was deleted successfully");
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
        userBean.getUserFromSecurityContext().ifPresent(user ->
            log.debug("User: {} requested to update mail template {} with {}",
                      user.getMail(), storedMailTemplate, mailTemplate)
        );
        mailTemplateRepository.save(mailTemplate);
        log.debug("mail template was updated successfully");
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
