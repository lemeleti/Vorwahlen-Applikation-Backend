package ch.zhaw.vorwahlen.mapper;

import ch.zhaw.vorwahlen.model.dto.MailTemplateDTO;
import ch.zhaw.vorwahlen.model.modules.MailTemplate;
import org.springframework.stereotype.Component;

/**
 * Mapping class for {@link MailTemplate}.
 */
@Component
public class MailTemplateMapper implements Mapper<MailTemplateDTO, MailTemplate> {
    @Override
    public MailTemplateDTO toDto(MailTemplate mailTemplate) {
        return new MailTemplateDTO(mailTemplate.getId(), mailTemplate.getDescription(),
                mailTemplate.getSubject(), mailTemplate.getMessage());
    }

    @Override
    public MailTemplate toInstance(MailTemplateDTO mailTemplateDTO) {
        return MailTemplate.builder()
                .description(mailTemplateDTO.description())
                .subject(mailTemplateDTO.subject())
                .message(mailTemplateDTO.message())
                .build();
    }
}
