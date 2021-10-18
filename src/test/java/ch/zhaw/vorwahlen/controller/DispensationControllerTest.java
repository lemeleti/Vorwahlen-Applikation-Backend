package ch.zhaw.vorwahlen.controller;

import ch.zhaw.vorwahlen.service.DispensationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class DispensationControllerTest {

    private static final String REQUEST_MAPPING_PREFIX = "/dispensation";
    private static final String MULTIPART_FILE_REQUEST_PARAMETER = "file";
    private static final String CLASS_LIST_FILE_NAME = "Vorlage_Dispensationen.xlsx";
    private static final String WORKSHEET = "Sheet1";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    DispensationService dispensationService;

    /* **************************************************************************************************************
     * Positive tests
     * ************************************************************************************************************** */

    @Test
    void testSaveDispensationsFromExcel() throws IOException {
        // prepare
        var fis = getClass().getClassLoader().getResourceAsStream(CLASS_LIST_FILE_NAME);
        var mockMultipartFile = new MockMultipartFile(MULTIPART_FILE_REQUEST_PARAMETER, CLASS_LIST_FILE_NAME, "", fis);

        // execute
        try {
            mockMvc.perform(MockMvcRequestBuilders
                    .multipart(REQUEST_MAPPING_PREFIX)
                    .file(mockMultipartFile)
                    .param("worksheet", WORKSHEET)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(print());
        } catch (Exception e) {
            fail(e);
        }

        // verify
        verify(dispensationService, times(1)).importDispensationExcel(mockMultipartFile, WORKSHEET);
    }

    /* **************************************************************************************************************
     * Negative tests
     * ************************************************************************************************************** */

    @Test
    void testSaveDispensationsFromExcel_WithoutAFile() {
        // prepare
        var mockMultipartFile = new MockMultipartFile(MULTIPART_FILE_REQUEST_PARAMETER, "", "", "".getBytes());

        // execute
        try {
            mockMvc.perform(MockMvcRequestBuilders
                    .multipart(REQUEST_MAPPING_PREFIX)
                    .file(mockMultipartFile)
                    .param("worksheet", WORKSHEET)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andDo(print());
        } catch (Exception e) {
            fail(e);
        }

        // verify
        verify(dispensationService, times(0)).importDispensationExcel(mockMultipartFile, WORKSHEET);
    }

}
