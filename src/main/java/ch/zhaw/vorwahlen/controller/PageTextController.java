package ch.zhaw.vorwahlen.controller;

import ch.zhaw.vorwahlen.model.dto.PageTextDTO;
import ch.zhaw.vorwahlen.service.PageTextService;
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
import java.util.Set;

/**
 * Controller for page texts.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("texts")
public class PageTextController {

    private final PageTextService pageTextService;

    /**
     * Return all page texts.
     * @return {@link ResponseEntity} containing list of {@link PageTextDTO}.
     */
    @GetMapping(path = "")
    public ResponseEntity<Set<PageTextDTO>> getAllPageTexts() {
        return ResponseEntity.ok(pageTextService.getAllPageTexts());
    }

    /**
     * Return all page texts for a certain page.
     * @return {@link ResponseEntity} containing list of {@link PageTextDTO}.
     */
    @GetMapping(path = "/{page}")
    public ResponseEntity<Set<PageTextDTO>> getPageTexts(@PathVariable String page) {
        return ResponseEntity.ok(pageTextService.getPageTexts(page));
    }

    /**
     * Return all page texts for a certain page and usertype.
     * @return {@link ResponseEntity} containing list of {@link PageTextDTO}.
     */
    @GetMapping(path = "/{page}/{userType}")
    public ResponseEntity<Set<PageTextDTO>> getPageTextsByUserType(@PathVariable("page") String page,
                                                                   @PathVariable("userType") String userType) {
        return ResponseEntity.ok(pageTextService.getPageTextsByUserType(page, userType));
    }

    /**
     * Add a new page text.
     * @param pageTextDto to be added page text.
     * @return {@link ResponseEntity} containing {@link PageTextDTO}.
     */
    @PostMapping(path = "" )
    public ResponseEntity<PageTextDTO> addPageText(@Valid @RequestBody PageTextDTO pageTextDto) {
        return ResponseEntity.ok(pageTextService.addPageText(pageTextDto));
    }

    /**
     * Replace a page text.
     * @param pageTextId id of page text to be replaced.
     * @param pageTextDto new page text
     * @return {@link ResponseEntity} containing {@link Void}.
     */
    @PutMapping(path = "/{pageTextId}")
    public ResponseEntity<Void> replacePageText(@PathVariable long pageTextId,
                                                @Valid @RequestBody PageTextDTO pageTextDto) {
        pageTextService.replacePageText(pageTextId, pageTextDto);
        return ResponseEntity.ok().build();
    }

    /**
     * Delete a page text
     * @param pageTextId id of page text to be deleted
     * @return {@link ResponseEntity} containing {@link Void}.
     */
    @DeleteMapping(path = "/{pageTextId}")
    public ResponseEntity<Void> deletePageText(@PathVariable long pageTextId) {
        pageTextService.deletePageText(pageTextId);
        return ResponseEntity.ok().build();
    }

}
