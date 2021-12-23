package ch.zhaw.vorwahlen.service;

import ch.zhaw.vorwahlen.config.ResourceBundleMessageLoader;
import ch.zhaw.vorwahlen.config.UserBean;
import ch.zhaw.vorwahlen.constants.ResourceMessageConstants;
import ch.zhaw.vorwahlen.exception.PageTextConflictException;
import ch.zhaw.vorwahlen.exception.PageTextNotFoundException;
import ch.zhaw.vorwahlen.mapper.Mapper;
import ch.zhaw.vorwahlen.model.pagetext.PageTextDTO;
import ch.zhaw.vorwahlen.model.pagetext.PageText;
import ch.zhaw.vorwahlen.model.pagetext.UserType;
import ch.zhaw.vorwahlen.repository.PageTextRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * CRUD Methods for {@link PageText}
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class PageTextService {

    private final PageTextRepository pageTextRepository;
    private final Mapper<PageTextDTO, PageText> mapper;
    private final UserBean userBean;

    private Set<PageTextDTO> mapSetToDto(Stream<PageText> stream) {
        return stream.map(mapper::toDto).collect(Collectors.toSet());
    }

    /**
     * Return all page texts.
     * @return set of {@link PageTextDTO}.
     */
    public Set<PageTextDTO> getAllPageTexts() {
        return mapSetToDto(pageTextRepository.findAll().stream());
    }

    /**
     * Return all page texts by a certain page.
     * @return set of {@link PageTextDTO}.
     */
    public Set<PageTextDTO> getPageTexts(String page) {
        return mapSetToDto(pageTextRepository.findAllByPage(page).stream());
    }

    /**
     * Return all page texts by a certain page and user type.
     * @return set of {@link PageTextDTO}.
     */
    public Set<PageTextDTO> getPageTextsByUserType(String page, String userTypeString) {
        var userType = UserType.parseString(userTypeString);
        return mapSetToDto(pageTextRepository.findAllByUserType(page, userType).stream());
    }

    /**
     * Add a new page text.
     * @param pageTextDTO to be added page text.
     * @return newly added page text.
     */
    public PageTextDTO addPageText(PageTextDTO pageTextDTO) {
        userBean.getUserFromSecurityContext().ifPresent(user ->
            log.debug("User: {} requested to add a page text: {}", user.getMail(), pageTextDTO)
        );
        if(pageTextRepository.existsById(pageTextDTO.id())) {
            log.debug("Throwing PageTextConflictException because page text with id {} already exists", pageTextDTO.id());
            var formatString = ResourceBundleMessageLoader.getMessage(ResourceMessageConstants.ERROR_PAGE_TEXT_CONFLICT);
            var message = String.format(formatString, pageTextDTO.id());
            throw new PageTextConflictException(message);
        }
        var entity = mapper.toInstance(pageTextDTO);
        entity = pageTextRepository.save(entity);
        log.debug("Page Text: {} was saved successfully to the database", entity);
        return mapper.toDto(entity);
    }

    /**
     * Replace a page text by id.
     * @param pageTextId identifier of the page text to be replaced.
     * @param pageTextDTO new page text.
     * @throws PageTextNotFoundException if the page text id does not exist.
     */
    public void replacePageText(long pageTextId, PageTextDTO pageTextDTO) {
        var storedPageText = fetchPageTextById(pageTextId);
        var newPageText = mapper.toInstance(pageTextDTO);
        newPageText.setId(pageTextId);
        userBean.getUserFromSecurityContext().ifPresent(user ->
            log.debug("User: {} requested to update page text {} with {}",
                      user.getMail(), storedPageText, newPageText)
        );
        pageTextRepository.save(newPageText);
        log.debug("page text was updated successfully");
    }

    /**
     * Delete a page text.
     * @param pageTextId identifier of the page text to be deleted.
     * @throws PageTextNotFoundException if the page text id does not exist.
     */
    public void deletePageText(long pageTextId) {
        userBean.getUserFromSecurityContext().ifPresent(user ->
            log.debug("User: {} requested to delete a page text with id: {}", user.getMail(), pageTextId)
        );
        var pageText = fetchPageTextById(pageTextId);
        pageTextRepository.deleteById(pageText.getId());
        log.debug("page text was deleted successfully");
    }

    private PageText fetchPageTextById(long pageTextId) {
        return pageTextRepository
                .findById(pageTextId)
                .orElseThrow(() -> {
                    var unformattedMessage = ResourceBundleMessageLoader.getMessage(ResourceMessageConstants.ERROR_PAGE_TEXT_NOT_FOUND);
                    var message = String.format(unformattedMessage, pageTextId);
                    return new PageTextNotFoundException(message);
                });
    }

}
