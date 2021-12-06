package ch.zhaw.vorwahlen.controller;

import ch.zhaw.vorwahlen.model.dto.StudentDTO;
import ch.zhaw.vorwahlen.service.StudentService;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import static ch.zhaw.vorwahlen.service.StudentService.*;
import static ch.zhaw.vorwahlen.util.ObjectMapperUtil.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("dev")
@SpringBootTest(properties = "classpath:settings.properties")
@AutoConfigureMockMvc
class StudentControllerTest {

    private static final String CLASS_1 = "class1";
    private static final String CLASS_2 = "class2";
    private static final String REQUEST_MAPPING_PREFIX = "/students";
    private static final String DISPENSATION_REQUEST_MAPPING_PREFIX = "/students/dispensations";
    private static final String MULTIPART_FILE_REQUEST_PARAMETER = "file";
    private static final String CLASS_LIST_FILE_NAME = "Vorlage_Klassenzuteilungen.xlsx";
    private static final String DISPENSATION_LIST_FILE_NAME = "Vorlage_Dispensationen.xlsx";
    private static final String WORKSHEET = "Sheet1";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    StudentService studentService;

    StudentDTO nonExistentStudentDto;

    @BeforeEach
    void setUp() {
        nonExistentStudentDto = StudentDTO.builder()
                .email("hello@zhaw.ch")
                .name("Hello World!")
                .clazz("Hello Clazz!")
                .build();
    }

    /* **************************************************************************************************************
     * Positive tests
     * ************************************************************************************************************** */
    
    @Test
    void testGetAllStudents() {
        // prepare
        var expectedList = new ArrayList<StudentDTO>();

        expectedList.add(StudentDTO.builder().email("mail1").name("name1").clazz(CLASS_1).paDispensation(PA_DISPENSATION).wpmDispensation(WPM_DISPENSATION).build());
        expectedList.add(StudentDTO.builder().email("mail2").name("name2").clazz(CLASS_1).paDispensation(PA_DISPENSATION).wpmDispensation(WPM_DISPENSATION).build());
        expectedList.add(StudentDTO.builder().email("mail3").name("name3").clazz(CLASS_2).paDispensation(PA_DISPENSATION).wpmDispensation(WPM_DISPENSATION).build());

        when(studentService.getAllStudents()).thenReturn(expectedList);

        // execute
        try {
            mockMvc.perform(MockMvcRequestBuilders
                    .get(REQUEST_MAPPING_PREFIX)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").exists())
                    .andExpect(jsonPath("$.[*].email").isNotEmpty())
                    .andExpect(jsonPath("$.[*].email", anyOf(
                            hasItem(expectedList.get(PA_DISPENSATION).getEmail()),
                            hasItem(expectedList.get(1).getEmail()),
                            hasItem(expectedList.get(2).getEmail())
                    )))
                    .andExpect(jsonPath("$.[*].name").isNotEmpty())
                    .andExpect(jsonPath("$.[*].name", anyOf(
                            hasItem(expectedList.get(PA_DISPENSATION).getName()),
                            hasItem(expectedList.get(1).getName()),
                            hasItem(expectedList.get(2).getName())
                    )))
                    .andExpect(jsonPath("$.[*].class").isNotEmpty())
                    .andExpect(jsonPath("$.[*].class", anyOf(
                            hasItem(CLASS_1),
                            hasItem(CLASS_2)
                    )))
                    .andExpect(jsonPath("$.[*].paDispensation").isNotEmpty())
                    .andExpect(jsonPath("$.[*].paDispensation", anyOf(hasItem(PA_DISPENSATION))))
                    .andExpect(jsonPath("$.[*].wpmDispensation").isNotEmpty())
                    .andExpect(jsonPath("$.[*].wpmDispensation", anyOf(hasItem(PA_DISPENSATION))))
                    .andDo(print());
            // todo: test ip and tz flag
        } catch (Exception e) {
            fail(e);
        }

        // verify
        verify(studentService, times(1)).getAllStudents();
    }

    @Test
    void testGetAllStudents_ElectionValid() {
        // prepare
        var expectedList = new ArrayList<StudentDTO>();

        expectedList.add(StudentDTO.builder().email("mail1").name("name1").clazz(CLASS_1).paDispensation(PA_DISPENSATION).wpmDispensation(WPM_DISPENSATION).build());
        expectedList.add(StudentDTO.builder().email("mail2").name("name2").clazz(CLASS_1).paDispensation(PA_DISPENSATION).wpmDispensation(WPM_DISPENSATION).build());
        expectedList.add(StudentDTO.builder().email("mail3").name("name3").clazz(CLASS_2).paDispensation(PA_DISPENSATION).wpmDispensation(WPM_DISPENSATION).build());

        when(studentService.getAllStudentsByElection(anyBoolean())).thenReturn(expectedList);

        // execute
        try {
            mockMvc.perform(MockMvcRequestBuilders
                    .get(REQUEST_MAPPING_PREFIX + "?electionvalid=true")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").exists())
                    .andExpect(jsonPath("$.[*].email").isNotEmpty())
                    .andExpect(jsonPath("$.[*].email", anyOf(
                            hasItem(expectedList.get(PA_DISPENSATION).getEmail()),
                            hasItem(expectedList.get(1).getEmail()),
                            hasItem(expectedList.get(2).getEmail())
                    )))
                    .andExpect(jsonPath("$.[*].name").isNotEmpty())
                    .andExpect(jsonPath("$.[*].name", anyOf(
                            hasItem(expectedList.get(PA_DISPENSATION).getName()),
                            hasItem(expectedList.get(1).getName()),
                            hasItem(expectedList.get(2).getName())
                    )))
                    .andExpect(jsonPath("$.[*].class").isNotEmpty())
                    .andExpect(jsonPath("$.[*].class", anyOf(
                            hasItem(CLASS_1),
                            hasItem(CLASS_2)
                    )))
                    .andExpect(jsonPath("$.[*].paDispensation").isNotEmpty())
                    .andExpect(jsonPath("$.[*].paDispensation", anyOf(hasItem(PA_DISPENSATION))))
                    .andExpect(jsonPath("$.[*].wpmDispensation").isNotEmpty())
                    .andExpect(jsonPath("$.[*].wpmDispensation", anyOf(hasItem(PA_DISPENSATION))))
                    .andDo(print());
            // todo: test ip and tz flag
        } catch (Exception e) {
            fail(e);
        }

        // verify
        verify(studentService, times(1)).getAllStudentsByElection(anyBoolean());
    }

    @Test
    void testAddStudent() throws URISyntaxException {
        // prepare
        when(studentService.addAndReturnLocation(any())).thenReturn(new URI("/students/".concat(nonExistentStudentDto.getEmail())));

        // execute
        try {
            mockMvc.perform(MockMvcRequestBuilders
                                    .post(REQUEST_MAPPING_PREFIX)
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(nonExistentStudentDto)))
                    .andExpect(status().isCreated())
                    .andDo(print());
        } catch (Exception e) {
            fail(e);
        }

        // verify
        verify(studentService, times(1)).addAndReturnLocation(any());
    }

    @Test
    void testGetStudentById() {
        // prepare
        when(studentService.getStudentById(anyString())).thenReturn(nonExistentStudentDto);

        // execute
        try {
            var results = mockMvc.perform(MockMvcRequestBuilders
                                                  .get(REQUEST_MAPPING_PREFIX + "/" + nonExistentStudentDto.getEmail())
                                                  .with(csrf()))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn();

            var studentDto = fromJsonResult(results, StudentDTO.class);
            assertEquals(nonExistentStudentDto, studentDto);
        } catch (Exception e) {
            fail(e);
        }

        // verify
        verify(studentService, times(1)).getStudentById(anyString());
    }

    @Test
    void testDeleteStudentById() {
        // prepare

        // execute
        try {
            mockMvc.perform(MockMvcRequestBuilders
                                    .delete(REQUEST_MAPPING_PREFIX + "/" + nonExistentStudentDto.getEmail())
                                    .with(csrf()))
                    .andExpect(status().isNoContent())
                    .andDo(print());
        } catch (Exception e) {
            fail(e);
        }

        // verify
        verify(studentService, times(1)).deleteStudentById(anyString());
    }

    @Test
    void testReplaceStudentById() {
        // prepare
        when(studentService.replaceStudent(anyString(), any())).thenReturn(nonExistentStudentDto);

        // execute
        try {
            var results = mockMvc.perform(MockMvcRequestBuilders
                                                  .put(REQUEST_MAPPING_PREFIX + "/" + nonExistentStudentDto.getEmail())
                                                  .with(csrf())
                                                  .contentType(MediaType.APPLICATION_JSON)
                                                  .content(toJson(nonExistentStudentDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").exists())
                    .andDo(print())
                    .andReturn();
            var studentDTO = fromJsonResult(results, StudentDTO.class);
            assertEquals(nonExistentStudentDto, studentDTO);
        } catch (Exception e) {
            fail(e);
        }

        // verify
        verify(studentService, times(1)).replaceStudent(anyString(), any());
    }

    @Test
    void testSaveClassListsFromExcel() throws IOException {
        // prepare
        var fis = getClass().getClassLoader().getResourceAsStream(CLASS_LIST_FILE_NAME);
        var mockMultipartFile = new MockMultipartFile(MULTIPART_FILE_REQUEST_PARAMETER, CLASS_LIST_FILE_NAME, "", fis);

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
        verify(studentService, times(1)).importClassListExcel(mockMultipartFile, WORKSHEET);
    }

    @Test
    void testSaveDispensationsFromExcel() throws IOException {
        // prepare
        var fis = getClass().getClassLoader().getResourceAsStream(DISPENSATION_LIST_FILE_NAME);
        var mockMultipartFile = new MockMultipartFile(MULTIPART_FILE_REQUEST_PARAMETER, DISPENSATION_LIST_FILE_NAME, "", fis);

        // execute
        try {
            mockMvc.perform(MockMvcRequestBuilders
                            .multipart(DISPENSATION_REQUEST_MAPPING_PREFIX)
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
        verify(studentService, times(1)).importDispensationExcel(mockMultipartFile, WORKSHEET);
    }

    /* **************************************************************************************************************
     * Negative tests
     * ************************************************************************************************************** */

    @Test
    void testSaveClassListsFromExcel_WithoutAFile() {
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
        verify(studentService, times(0)).importClassListExcel(mockMultipartFile, WORKSHEET);
    }

    @Test
    void testSaveDispensationsFromExcel_WithoutAFile() {
        // prepare
        var mockMultipartFile = new MockMultipartFile(MULTIPART_FILE_REQUEST_PARAMETER, "", "", "".getBytes());

        // execute
        try {
            mockMvc.perform(MockMvcRequestBuilders
                            .multipart(DISPENSATION_REQUEST_MAPPING_PREFIX)
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
        verify(studentService, times(0)).importDispensationExcel(mockMultipartFile, WORKSHEET);
    }
}