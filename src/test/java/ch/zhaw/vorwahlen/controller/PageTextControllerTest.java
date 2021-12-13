package ch.zhaw.vorwahlen.controller;

import ch.zhaw.vorwahlen.model.dto.PageTextDTO;
import ch.zhaw.vorwahlen.model.pagetext.UserType;
import ch.zhaw.vorwahlen.service.PageTextService;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Set;
import java.util.stream.Collectors;

import static ch.zhaw.vorwahlen.util.ObjectMapperUtil.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("dev")
@SpringBootTest(properties = "classpath:settings.properties")
@AutoConfigureMockMvc
class PageTextControllerTest {

    private static final String REQUEST_MAPPING_PREFIX = "/texts";
    public static final String PAGE_1 = "page 1";
    public static final String PAGE_2 = "page 2";
    public static final String PAGE_3 = "page 3";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    PageTextService pageTextService;

    Set<PageTextDTO> allPageDtos;

    @BeforeEach
    void setUp() {
        allPageDtos = Set.of(
                new PageTextDTO(1L, PAGE_1, UserType.ANONYMOUS, false, 1, "text 1"),
                new PageTextDTO(2L, PAGE_2, UserType.FULL_TIME, false, 1, "text 1"),
                new PageTextDTO(3L, PAGE_2, UserType.FULL_TIME, true, 2, "text 2"),
                new PageTextDTO(4L, PAGE_2, UserType.PART_TIME_FIRST_ELECTION, false, 1, "text 1"),
                new PageTextDTO(5L, PAGE_3, UserType.PART_TIME_SECOND_ELECTION, false, 1, "text 1")
        );
    }

    @Test
    void testGetAllPageTexts() {
        when(pageTextService.getAllPageTexts()).thenReturn(allPageDtos);

        try {
            var result = mockMvc.perform(MockMvcRequestBuilders
                                    .get(REQUEST_MAPPING_PREFIX)
                                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").exists())
                    .andDo(print())
                    .andReturn();

            var resultSet = fromJsonResult(result, new TypeReference<Set<PageTextDTO>>() {});
            assertEquals(allPageDtos, resultSet);
        } catch (Exception e) {
            fail(e);
        }

        verify(pageTextService, times(1)).getAllPageTexts();
    }

    @Test
    void testGetPageTexts() {
        var expectedSet = allPageDtos.stream()
                .filter(dto -> dto.page().equals(PAGE_2))
                .collect(Collectors.toSet());

        when(pageTextService.getPageTexts(anyString())).thenReturn(expectedSet);

        try {
            var result = mockMvc.perform(MockMvcRequestBuilders
                                    .get(REQUEST_MAPPING_PREFIX + "/" + PAGE_2)
                                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").exists())
                    .andDo(print())
                    .andReturn();

            var resultSet = fromJsonResult(result, new TypeReference<Set<PageTextDTO>>() {});
            assertEquals(expectedSet, resultSet);
        } catch (Exception e) {
            fail(e);
        }

        verify(pageTextService, times(1)).getPageTexts(anyString());
    }

    @Test
    void testGetPageTextsByUserType() {
        var expectedSet = allPageDtos.stream()
                .filter(dto -> dto.page().equals(PAGE_2) && dto.userType().equals(UserType.FULL_TIME))
                .collect(Collectors.toSet());

        when(pageTextService.getPageTextsByUserType(anyString(), anyString())).thenReturn(expectedSet);

        try {
            var result = mockMvc.perform(MockMvcRequestBuilders
                                    .get(REQUEST_MAPPING_PREFIX + "/" + PAGE_2 + "/" + UserType.FULL_TIME.getPathString())
                                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").exists())
                    .andDo(print())
                    .andReturn();

            var resultSet = fromJsonResult(result, new TypeReference<Set<PageTextDTO>>() {});
            assertEquals(expectedSet, resultSet);
        } catch (Exception e) {
            fail(e);
        }

        verify(pageTextService, times(1)).getPageTextsByUserType(anyString(), anyString());
    }

    @Test
    void testAddPageText() {
        var newPageText = allPageDtos.stream().findAny().get();
        when(pageTextService.addPageText(any())).thenReturn(newPageText);
        try {
            var result = mockMvc.perform(MockMvcRequestBuilders
                                    .post(REQUEST_MAPPING_PREFIX)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(newPageText))
                                    .with(csrf())
                                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").exists())
                    .andDo(print())
                    .andReturn();

            assertEquals(newPageText, fromJsonResult(result, PageTextDTO.class));
        } catch (Exception e) {
            fail(e);
        }

        verify(pageTextService, times(1)).addPageText(any());
    }

    @Test
    void testReplacePageText() {
        doNothing().when(pageTextService).replacePageText(anyLong(), any());
        var newPageText = allPageDtos.stream().findAny().get();
        try {
            mockMvc.perform(MockMvcRequestBuilders
                                    .put(REQUEST_MAPPING_PREFIX + "/" + newPageText.id())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(newPageText))
                                    .with(csrf())
                                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(print());
        } catch (Exception e) {
            fail(e);
        }

        verify(pageTextService, times(1)).replacePageText(anyLong(), any());
    }

    @Test
    void testDeletePageText() {
        doNothing().when(pageTextService).deletePageText(anyLong());
        var newPageText = allPageDtos.stream().findAny().get();
        try {
            mockMvc.perform(MockMvcRequestBuilders
                                    .delete(REQUEST_MAPPING_PREFIX + "/" + newPageText.id())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(newPageText))
                                    .with(csrf())
                                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(print());
        } catch (Exception e) {
            fail(e);
        }

        verify(pageTextService, times(1)).deletePageText(anyLong());
    }

}
