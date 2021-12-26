package ch.zhaw.vorwahlen.controller;

import ch.zhaw.vorwahlen.model.mailtemplate.MailTemplateDTO;
import ch.zhaw.vorwahlen.service.MailTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * Controller to manage mail templates.
 */
@RestController
@RequestMapping(path = "mailtemplates")
@RequiredArgsConstructor
public class MailTemplateController {
    private final MailTemplateService mailTemplateService;

    /**
     * Get all mail templates.
     * @return {@link ResponseEntity} containing a list of {@link MailTemplateDTO}
     */
    @GetMapping(path = "")
    public ResponseEntity<List<MailTemplateDTO>> getAllMailTemplates() {
        return ResponseEntity.ok(mailTemplateService.getAllMailTemplates());
    }

    /**
     * Get mail template by id.
     * @param id of the template.
     * @return {@link MailTemplateDTO}
     */
    @GetMapping(path = "/{id}")
    public ResponseEntity<MailTemplateDTO> getMailTemplateById(@PathVariable Long id) {
        return ResponseEntity.ok(mailTemplateService.getMailTemplateById(id));
    }

    /**
     * Create and save mail template.
     * @param mailTemplateDTO to be saved.
     * @return {@link ResponseEntity} containing {@link MailTemplateDTO}.
     */
    @PostMapping(path = "")
    public ResponseEntity<MailTemplateDTO> createMailTemplate(@Valid @RequestBody MailTemplateDTO mailTemplateDTO) {
        return ResponseEntity.ok(mailTemplateService.createMailTemplate(mailTemplateDTO));
    }

    /**
     * Delete mail template by id.
     * @param id of the mail template.
     * @return {@link ResponseEntity} containing {@link Void}.
     */
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteMailTemplate(@PathVariable Long id) {
        mailTemplateService.deleteMailTemplateById(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Update mail template data.
     * @param id of the mail template.
     * @param mailTemplateDTO containing updates fields.
     * @return {@link ResponseEntity} containing {@link Void}.
     */
    @PutMapping(path = "/{id}")
    public ResponseEntity<Void> updateMailTemplate(@PathVariable Long id,
                                                   @Valid @RequestBody MailTemplateDTO mailTemplateDTO) {
        mailTemplateService.updateMailTemplate(id, mailTemplateDTO);
        return ResponseEntity.ok().build();
    }
}
