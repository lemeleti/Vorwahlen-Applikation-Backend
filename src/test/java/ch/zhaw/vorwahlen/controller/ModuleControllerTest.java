package ch.zhaw.vorwahlen.controller;

import ch.zhaw.vorwahlen.model.dto.ModuleDTO;
import ch.zhaw.vorwahlen.service.ModuleService;
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
import java.util.ArrayList;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ModuleControllerTest {

    private static final String LANGUAGE_DE = "de";
    private static final String LANGUAGE_EN = "en";
    private static final String REQUEST_MAPPING_PREFIX = "/module";
    private static final String MULTIPART_FILE_REQUEST_PARAMETER = "file";
    private static final String MODULE_LIST_FILE_NAME = "Liste_alle_Module_SM2025_SGL_Def_1.7-2021-03-29.xlsx";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ModuleService moduleService;

    /* **************************************************************************************************************
     * Positive tests
     * ************************************************************************************************************** */
    
    @Test
    void testGetAllModules() {
        // prepare
        var expectedList = new ArrayList<ModuleDTO>();
        expectedList.add(new ModuleDTO("nr1", "title1", LANGUAGE_DE));
        expectedList.add(new ModuleDTO("nr2", "title2", LANGUAGE_EN));
        expectedList.add(new ModuleDTO("nr3", "title3", LANGUAGE_DE));

        when(moduleService.getAllModules()).thenReturn(expectedList);

        // execute
        try {
            mockMvc.perform(MockMvcRequestBuilders
                    .get(REQUEST_MAPPING_PREFIX)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").exists())
                    .andExpect(jsonPath("$.[*].module_no").isNotEmpty())
                    .andExpect(jsonPath("$.[*].module_no", anyOf(
                            hasItem(expectedList.get(0).getModuleNo()),
                            hasItem(expectedList.get(1).getModuleNo()),
                            hasItem(expectedList.get(2).getModuleNo())
                    )))
                    .andExpect(jsonPath("$.[*].module_title").isNotEmpty())
                    .andExpect(jsonPath("$.[*].module_title", anyOf(
                            hasItem(expectedList.get(0).getModuleTitle()),
                            hasItem(expectedList.get(1).getModuleTitle()),
                            hasItem(expectedList.get(2).getModuleTitle())
                    )))
                    .andExpect(jsonPath("$.[*].language").isNotEmpty())
                    .andExpect(jsonPath("$.[*].language", anyOf(
                            hasItem(LANGUAGE_DE),
                            hasItem(LANGUAGE_EN)
                    )))
                    .andDo(print());
        } catch (Exception e) {
            fail(e);
        }

        // verify
        verify(moduleService, times(1)).getAllModules();
    }

    @Test
    void testSaveModulesFromExcel() throws IOException {
        // prepare
        var fis = getClass().getClassLoader().getResourceAsStream(MODULE_LIST_FILE_NAME);
        var mockMultipartFile = new MockMultipartFile(MULTIPART_FILE_REQUEST_PARAMETER, MODULE_LIST_FILE_NAME, "", fis);

        // execute
        try {
            mockMvc.perform(MockMvcRequestBuilders
                    .multipart(REQUEST_MAPPING_PREFIX)
                    .file(mockMultipartFile)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(print());
        } catch (Exception e) {
            fail(e);
        }

        // verify
        verify(moduleService, times(1)).importModuleExcel(mockMultipartFile);
    }


    @Test
    void testFetchAdditionalModuleData() {
        // prepare

        // execute
        try {
            mockMvc.perform(MockMvcRequestBuilders
                    .post(REQUEST_MAPPING_PREFIX + "/scrape")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(print());
        } catch (Exception e) {
            fail(e);
        }

        // verify
        verify(moduleService, times(1)).fetchAdditionalModuleData();
    }

    /* **************************************************************************************************************
     * Negative tests
     * ************************************************************************************************************** */


    @Test
    void testSaveModulesFromExcel_WithoutAFile() {
        // prepare
        var mockMultipartFile = new MockMultipartFile(MULTIPART_FILE_REQUEST_PARAMETER, "", "", "".getBytes());

        // execute
        try {
            mockMvc.perform(MockMvcRequestBuilders
                    .multipart(REQUEST_MAPPING_PREFIX)
                    .file(mockMultipartFile)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andDo(print());
        } catch (Exception e) {
            fail(e);
        }

        // verify
        verify(moduleService, times(0)).importModuleExcel(mockMultipartFile);
    }

}
