package ch.zhaw.vorwahlen.controller;

import ch.zhaw.vorwahlen.model.dto.MailTemplateDTO;
import ch.zhaw.vorwahlen.service.MailTemplateService;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.List;

import static ch.zhaw.vorwahlen.util.ObjectMapperUtil.fromJsonResult;
import static ch.zhaw.vorwahlen.util.ObjectMapperUtil.toJson;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("dev")
@SpringBootTest(properties = "classpath:settings.properties")
@AutoConfigureMockMvc
class MailTemplateControllerTest {

    private static final String REQUEST_MAPPING_PREFIX = "/mailtemplates";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    MailTemplateService mailTemplateService;

    static {
        System.setProperty("ADMIN", "dev@zhaw.ch");
    }

    @Test
    void testGetAllMailTemplates() {
        var expectedList = new ArrayList<MailTemplateDTO>();
        expectedList.add(new MailTemplateDTO(1L, "descrption 1", "subject 1", "message 1"));
        expectedList.add(new MailTemplateDTO(2L, "descrption 2", "subject 2", "message 2"));
        when(mailTemplateService.getAllMailTemplates()).thenReturn(expectedList);

        try {
            var result = mockMvc.perform(MockMvcRequestBuilders
                                                 .get(REQUEST_MAPPING_PREFIX)
                                                 .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").exists())
                    .andReturn();
            assertIterableEquals(expectedList, fromJsonResult(result, new TypeReference<List<MailTemplateDTO>>(){}));
        } catch (Exception e) {
            fail(e);
        }

        // verify
        verify(mailTemplateService, times(1)).getAllMailTemplates();
    }

    @Test
    void testGetMailTemplateById() {
        var expected = new MailTemplateDTO(1L, "descrption 1", "subject 1", "message 1");
        when(mailTemplateService.getMailTemplateById(anyLong())).thenReturn(expected);

        try {
            var result = mockMvc.perform(MockMvcRequestBuilders
                                                 .get(REQUEST_MAPPING_PREFIX + "/1")
                                                 .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").exists())
                    .andReturn();
            assertEquals(expected, fromJsonResult(result, MailTemplateDTO.class));
        } catch (Exception e) {
            fail(e);
        }

        // verify
        verify(mailTemplateService, times(1)).getMailTemplateById(anyLong());
    }

    @Test
    void testCreateMailTemplate() {
        var expected = new MailTemplateDTO(1L, "descrption 1", "subject 1", "message 1");
        when(mailTemplateService.createMailTemplate(any())).thenReturn(expected);

        try {
            var result = mockMvc.perform(MockMvcRequestBuilders
                                                 .post(REQUEST_MAPPING_PREFIX)
                                                 .contentType(MediaType.APPLICATION_JSON)
                                                 .content(toJson(expected))
                                                 .with(csrf())
                                                 .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").exists())
                    .andReturn();
            assertEquals(expected, fromJsonResult(result, MailTemplateDTO.class));
        } catch (Exception e) {
            fail(e);
        }

        // verify
        verify(mailTemplateService, times(1)).createMailTemplate(any());
    }

    @Test
    void testDeleteMailTemplate() {
        doNothing().when(mailTemplateService).deleteMailTemplateById(anyLong());

        try {
            mockMvc.perform(MockMvcRequestBuilders
                                                 .delete(REQUEST_MAPPING_PREFIX + "/1")
                                                 .with(csrf())
                                                 .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();
        } catch (Exception e) {
            fail(e);
        }

        // verify
        verify(mailTemplateService, times(1)).deleteMailTemplateById(anyLong());
    }

    @Test
    void testUpdateMailTemplate() {
        var expected = new MailTemplateDTO(1L, "descrption 1", "subject 1", "message 1");
        doNothing().when(mailTemplateService).updateMailTemplate(anyLong(), any());

        try {
            mockMvc.perform(MockMvcRequestBuilders
                                                 .put(REQUEST_MAPPING_PREFIX + "/1")
                                                 .contentType(MediaType.APPLICATION_JSON)
                                                 .content(toJson(expected))
                                                 .with(csrf())
                                                 .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();
        } catch (Exception e) {
            fail(e);
        }

        // verify
        verify(mailTemplateService, times(1)).updateMailTemplate(anyLong(), any());
    }

}
