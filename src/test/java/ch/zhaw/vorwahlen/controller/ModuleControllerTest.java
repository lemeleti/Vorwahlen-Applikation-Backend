package ch.zhaw.vorwahlen.controller;

import ch.zhaw.vorwahlen.model.dto.ModuleDTO;
import ch.zhaw.vorwahlen.service.ModuleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.IOException;
import java.util.ArrayList;

import static ch.zhaw.vorwahlen.util.ObjectMapperUtil.fromJsonResult;
import static ch.zhaw.vorwahlen.util.ObjectMapperUtil.toJson;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("dev")
@SpringBootTest(properties = "classpath:settings.properties")
@AutoConfigureMockMvc
class ModuleControllerTest {

    private static final String LANGUAGE_DE = "de";
    private static final String LANGUAGE_EN = "en";
    private static final String REQUEST_MAPPING_PREFIX = "/modules";
    private static final String MULTIPART_FILE_REQUEST_PARAMETER = "file";
    private static final String MODULE_LIST_FILE_NAME = "Liste_alle_Module_SM2025_SGL_Def_1.7-2021-03-29.xlsx";
    private static final String WORKSHEET = "Module 2025";
    private static final int CREDIT_2 = 2;
    private static final int CREDIT_4 = 4;
    private static final int CREDIT_6 = 6;
    private static final int SEMESTER_5 = 5;
    private static final int SEMESTER_5_6 = 7;

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ModuleService moduleService;

    ModuleDTO nonExistentModuleDto;

    static {
        System.setProperty("ADMIN", "dev@zhaw.ch");
    }

    @BeforeEach
    void setUp() {
        nonExistentModuleDto = ModuleDTO.builder()
                .moduleNo("t.BA.WM.HELLO.19HS")
                .shortModuleNo("WM.HELLO")
                .moduleTitle("Hello")
                .moduleId(1)
                .moduleGroup("AV6,DS6,ET5,EU6,IT6,MT7,ST5,VS6,WI6")
                .institute("INIT")
                .credits((byte) 4)
                .language("English")
                .semester(5)
                .consecutiveModuleNo("")
                .build();
    }

    /* **************************************************************************************************************
     * Positive tests
     * ************************************************************************************************************** */
    
    @Test
    void testGetAllModules() {
        // prepare
        var expectedList = new ArrayList<ModuleDTO>();
        expectedList.add(ModuleDTO.builder().moduleNo("nr1").moduleTitle("title1").language(LANGUAGE_DE).credits((byte) CREDIT_2)
                                 .semester(SEMESTER_5)
                                 .build());
        expectedList.add(ModuleDTO.builder().moduleNo("nr2").moduleTitle("title2").language(LANGUAGE_EN).credits((byte) CREDIT_4)
                                 .semester(SEMESTER_5_6)
                                 .build());
        expectedList.add(ModuleDTO.builder().moduleNo("nr3").moduleTitle("title3").language(LANGUAGE_DE).credits((byte) CREDIT_6)
                                 .semester(SEMESTER_5)
                                 .build());

        when(moduleService.getAllModules()).thenReturn(expectedList);

        // execute
        try {
            mockMvc.perform(MockMvcRequestBuilders
                    .get(REQUEST_MAPPING_PREFIX)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").exists())
                    .andExpect(jsonPath("$.[*].moduleNo").isNotEmpty())
                    .andExpect(jsonPath("$.[*].moduleNo", anyOf(
                            hasItem(expectedList.get(0).getModuleNo()),
                            hasItem(expectedList.get(1).getModuleNo()),
                            hasItem(expectedList.get(2).getModuleNo())
                    )))
                    .andExpect(jsonPath("$.[*].moduleTitle").isNotEmpty())
                    .andExpect(jsonPath("$.[*].moduleTitle", anyOf(
                            hasItem(expectedList.get(0).getModuleTitle()),
                            hasItem(expectedList.get(1).getModuleTitle()),
                            hasItem(expectedList.get(2).getModuleTitle())
                    )))
                    .andExpect(jsonPath("$.[*].credits").isNotEmpty())
                    .andExpect(jsonPath("$.[*].credits", anyOf(
                            hasItem(CREDIT_2),
                            hasItem(CREDIT_4),
                            hasItem(CREDIT_6)
                    )))
                    .andExpect(jsonPath("$.[*].language").isNotEmpty())
                    .andExpect(jsonPath("$.[*].language", anyOf(
                            hasItem(LANGUAGE_DE),
                            hasItem(LANGUAGE_EN)
                    )))
                    .andExpect(jsonPath("$.[*].semester").isNotEmpty())
                    .andExpect(jsonPath("$.[*].semester", anyOf(
                            hasItem(SEMESTER_5),
                            hasItem(SEMESTER_5_6)
                    )))
                    .andDo(print());
        } catch (Exception e) {
            fail(e);
        }

        // verify
        verify(moduleService, times(1)).getAllModules();
    }

    @Test
    void testAddModule() {
        // prepare
        when(moduleService.addModule(any())).thenReturn(nonExistentModuleDto);

        // execute
        try {
            var result = mockMvc.perform(MockMvcRequestBuilders
                                    .post(REQUEST_MAPPING_PREFIX)
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(nonExistentModuleDto)))
                                    .andExpect(status().isOk())
                                    .andExpect(jsonPath("$").exists())
                                    .andDo(print())
                                    .andReturn();
            assertEquals(nonExistentModuleDto, fromJsonResult(result, ModuleDTO.class));
        } catch (Exception e) {
            fail(e);
        }

        // verify
        verify(moduleService, times(1)).addModule(any());
    }

    @Test
    void testGetModuleById() {
        // prepare
        when(moduleService.getModuleById(anyString())).thenReturn(nonExistentModuleDto);

        // execute
        try {
            var results = mockMvc.perform(MockMvcRequestBuilders
                                    .get(REQUEST_MAPPING_PREFIX + "/" + nonExistentModuleDto.getModuleNo())
                                    .with(csrf()))
                                    .andExpect(status().isOk())
                                    .andDo(print())
                                    .andReturn();

            var moduleDto = fromJsonResult(results, ModuleDTO.class);
            assertEquals(nonExistentModuleDto, moduleDto);
        } catch (Exception e) {
            fail(e);
        }

        // verify
        verify(moduleService, times(1)).getModuleById(anyString());
    }

    @Test
    void testDeleteModuleById() {
        // prepare

        // execute
        try {
            mockMvc.perform(MockMvcRequestBuilders
                                    .delete(REQUEST_MAPPING_PREFIX + "/" + nonExistentModuleDto.getModuleNo())
                                    .with(csrf()))
                                    .andExpect(status().isOk())
                                    .andDo(print());
        } catch (Exception e) {
            fail(e);
        }

        // verify
        verify(moduleService, times(1)).deleteModuleById(anyString());
    }

    @Test
    void testReplaceModuleById() {
        // prepare
        when(moduleService.replaceModule(anyString(), any())).thenReturn(nonExistentModuleDto);

        // execute
        try {
            mockMvc.perform(MockMvcRequestBuilders
                                    .put(REQUEST_MAPPING_PREFIX + "/" + nonExistentModuleDto.getModuleNo())
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(nonExistentModuleDto)))
                                    .andExpect(status().isOk())
                                    .andDo(print())
                                    .andReturn();
        } catch (Exception e) {
            fail(e);
        }

        // verify
        verify(moduleService, times(1)).replaceModule(anyString(), any());
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
                    .param("worksheet", WORKSHEET)
                    .with(csrf())
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(print());
        } catch (Exception e) {
            fail(e);
        }

        // verify
        verify(moduleService, times(1)).importModuleExcel(mockMultipartFile, WORKSHEET);
    }


    @Test
    void testFetchAdditionalModuleData() {
        // prepare

        // execute
        try {
            mockMvc.perform(MockMvcRequestBuilders
                    .post(REQUEST_MAPPING_PREFIX + "/eventodata/scrape")
                    .with(csrf())
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(print());
        } catch (Exception e) {
            fail(e);
        }

        // verify
        verify(moduleService, times(1)).scrapeEventoDataForAllModules();
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
                    .param("worksheet", WORKSHEET)
                    .with(csrf())
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        } catch (Exception e) {
            fail(e);
        }

        // verify
        verify(moduleService, times(0)).importModuleExcel(mockMultipartFile, WORKSHEET);
    }

}
