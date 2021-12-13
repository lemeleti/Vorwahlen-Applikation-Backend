package ch.zhaw.vorwahlen.service;

import ch.zhaw.vorwahlen.config.ResourceBundleMessageLoader;
import ch.zhaw.vorwahlen.constants.ResourceMessageConstants;
import ch.zhaw.vorwahlen.exception.PageTextConflictException;
import ch.zhaw.vorwahlen.exception.PageTextNotFoundException;
import ch.zhaw.vorwahlen.mapper.Mapper;
import ch.zhaw.vorwahlen.model.dto.PageTextDTO;
import ch.zhaw.vorwahlen.model.pagetext.PageText;
import ch.zhaw.vorwahlen.model.pagetext.UserType;
import ch.zhaw.vorwahlen.repository.PageTextRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * CRUD Methods for {@link PageText}
 */
@RequiredArgsConstructor
@Service
@Log
public class PageTextService {

    private final PageTextRepository pageTextRepository;
    private final Mapper<PageTextDTO, PageText> mapper;

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
        if(pageTextRepository.existsById(pageTextDTO.id())) {
            var formatString = ResourceBundleMessageLoader.getMessage(ResourceMessageConstants.ERROR_PAGE_TEXT_CONFLICT);
            var message = String.format(formatString, pageTextDTO.id());
            throw new PageTextConflictException(message);
        }
        var entity = mapper.toInstance(pageTextDTO);
        entity = pageTextRepository.save(entity);
        return mapper.toDto(entity);
    }

    /**
     * Replace a page text by id.
     * @param pageTextId identifier of the page text to be replaced.
     * @param pageTextDTO new page text.
     * @throws PageTextNotFoundException if the page text id does not exist.
     */
    public void replacePageText(long pageTextId, PageTextDTO pageTextDTO) {
        if(!pageTextRepository.existsById(pageTextId)) {
            var unformattedMessage = ResourceBundleMessageLoader.getMessage(ResourceMessageConstants.ERROR_PAGE_TEXT_NOT_FOUND);
            var message = String.format(unformattedMessage, pageTextId);
            throw new PageTextNotFoundException(message);
        }
        var entity = mapper.toInstance(pageTextDTO);
        entity.setId(pageTextId);
        pageTextRepository.save(entity);
    }

    /**
     * Delete a page text.
     * @param pageTextId identifier of the page text to be deleted.
     * @throws PageTextNotFoundException if the page text id does not exist.
     */
    public void deletePageText(long pageTextId) {
        if(!pageTextRepository.existsById(pageTextId)) {
            var unformattedMessage = ResourceBundleMessageLoader.getMessage(ResourceMessageConstants.ERROR_PAGE_TEXT_NOT_FOUND);
            var message = String.format(unformattedMessage, pageTextId);
            throw new PageTextNotFoundException(message);
        }

        pageTextRepository.deleteById(pageTextId);
    }

}
