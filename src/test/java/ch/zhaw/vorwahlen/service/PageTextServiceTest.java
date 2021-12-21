package ch.zhaw.vorwahlen.service;

import ch.zhaw.vorwahlen.config.UserBean;
import ch.zhaw.vorwahlen.exception.PageTextConflictException;
import ch.zhaw.vorwahlen.exception.PageTextNotFoundException;
import ch.zhaw.vorwahlen.exception.UserTypeInvalidException;
import ch.zhaw.vorwahlen.mapper.Mapper;
import ch.zhaw.vorwahlen.model.dto.PageTextDTO;
import ch.zhaw.vorwahlen.model.pagetext.PageText;
import ch.zhaw.vorwahlen.model.pagetext.UserType;
import ch.zhaw.vorwahlen.repository.PageTextRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
class PageTextServiceTest {

    public static final String PAGE_1 = "page 1";
    public static final String PAGE_2 = "page 2";
    public static final String PAGE_3 = "page 3";
    public static final long NON_EXISTING_ID = 9999L;
    public static final String UNKNOWN_USER_TYPE = "UNKNOWN_USERTYPE";

    private final PageTextRepository pageTextRepository;
    private final Mapper<PageTextDTO, PageText> mapper;
    private final UserBean userBean;

    private PageTextService pageTextService;

    private List<PageText> pageTexts;

    @Autowired
    public PageTextServiceTest(PageTextRepository pageTextRepository, Mapper<PageTextDTO, PageText> mapper, UserBean userBean) {
        this.pageTextRepository = pageTextRepository;
        this.mapper = mapper;
        this.userBean = userBean;
    }

    @BeforeEach
    void setUp() {
        pageTexts = List.of(
                PageText.builder().page(PAGE_1).userType(UserType.ANONYMOUS).textNumber(1).text("text 1").build(),
                PageText.builder().page(PAGE_2).userType(UserType.FULL_TIME).textNumber(1).text("text 1").build(),
                PageText.builder().page(PAGE_2).userType(UserType.FULL_TIME).isIpText(true).textNumber(2).text("text 2").build(),
                PageText.builder().page(PAGE_2).userType(UserType.PART_TIME_FIRST_ELECTION).textNumber(1).text("text 1").build(),
                PageText.builder().page(PAGE_3).userType(UserType.PART_TIME_SECOND_ELECTION).textNumber(1).text("text 1").build()
        );
        pageTextRepository.saveAll(pageTexts);
        pageTextService = new PageTextService(pageTextRepository, mapper, userBean);
    }

    @AfterEach
    void tearDown() {
        pageTextRepository.deleteAll();
    }

    @Test
    void testGetAllPageTexts() {
        // prepare
        var expected = pageTexts.stream().map(mapper::toDto).collect(Collectors.toSet());

        // execute
        var result = pageTextService.getAllPageTexts();

        // verify
        assertEquals(expected, result);
    }

    @Test
    void testGetPageTexts() {
        // prepare
        var expected = pageTexts.stream()
                .filter(pageText -> pageText.getPage().equals(PAGE_2))
                .map(mapper::toDto)
                .collect(Collectors.toSet());

        // execute
        var result = pageTextService.getPageTexts(PAGE_2);

        // verify
        assertEquals(expected, result);
    }

    @Test
    void testGetPageTextsByUserType() {
        // prepare
        var expected = pageTexts.stream()
                .filter(pageText -> pageText.getPage().equals(PAGE_2) && pageText.getUserType().equals(UserType.FULL_TIME))
                .map(mapper::toDto)
                .collect(Collectors.toSet());

        // execute
        var result = pageTextService.getPageTextsByUserType(PAGE_2, UserType.FULL_TIME.getPathString());

        // verify
        assertEquals(expected, result);
    }

    @Test
    void testAddPageText() {
        // prepare
        var nonExistentPageText = new PageTextDTO(0L, "PAGE 4", UserType.ANONYMOUS, false, 1, "text 1");

        // execute
        var result = pageTextService.addPageText(nonExistentPageText);

        // verify
        var expected = new PageTextDTO(result.id(), "PAGE 4", UserType.ANONYMOUS, false, 1, "text 1");
        assertEquals(expected, result);
    }

    @Test
    void testReplacePageText() {
        // prepare
        var existingPageText = pageTexts.stream().findFirst().get();
        existingPageText.setPage("ABCD");

        // execute
        pageTextService.replacePageText(existingPageText.getId(), mapper.toDto(existingPageText));

        // verify
        var updatedPageText = pageTextService.getAllPageTexts()
                .stream()
                .filter(pageTextDTO -> pageTextDTO.id().equals(existingPageText.getId()))
                .findFirst().get();
        assertEquals(mapper.toDto(existingPageText), updatedPageText);
    }

    @Test
    void testDeletePageText() {
        // prepare
        var existingPageText = pageTexts.stream().findFirst().get();
        assertTrue(pageTextRepository.existsById(existingPageText.getId()));

        // execute
        pageTextService.deletePageText(existingPageText.getId());

        // verify
        assertFalse(pageTextRepository.existsById(existingPageText.getId()));
    }

    @Test
    void testAddPageText_AlreadyExisting() {
        var nonExistentPageText = new PageTextDTO(0L, "PAGE 4", UserType.ANONYMOUS, false, 1, "text 1");
        var storedPageText = assertDoesNotThrow(() -> pageTextService.addPageText(nonExistentPageText));
        assertThrows(PageTextConflictException.class, () -> pageTextService.addPageText(storedPageText));
    }

    @Test
    void testGetPageTextsByUserType_UserTypeInvalid() {
        assertThrows(UserTypeInvalidException.class, () -> pageTextService.getPageTextsByUserType(PAGE_2, UNKNOWN_USER_TYPE));
    }

    @Test
    void testReplacePageText_PageTextNotFound() {
        assertThrows(PageTextNotFoundException.class, () -> pageTextService.replacePageText(NON_EXISTING_ID, null));
    }

    @Test
    void testDeletePageText_PageTextNotFound() {
        assertThrows(PageTextNotFoundException.class, () -> pageTextService.deletePageText(NON_EXISTING_ID));
    }

}
