package ch.zhaw.vorwahlen.controller;

import ch.zhaw.vorwahlen.model.core.election.ElectionDTO;
import ch.zhaw.vorwahlen.model.core.election.ElectionStatusDTO;
import ch.zhaw.vorwahlen.model.core.election.ElectionStatusElementDTO;
import ch.zhaw.vorwahlen.model.modulestructure.ElectionStructureDTO;
import ch.zhaw.vorwahlen.model.ElectionTransferDTO;
import ch.zhaw.vorwahlen.model.core.validationsetting.ValidationSettingDTO;
import ch.zhaw.vorwahlen.model.core.module.ModuleCategory;
import ch.zhaw.vorwahlen.model.modulestructure.ModuleStructureElement;
import ch.zhaw.vorwahlen.service.ElectionService;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static ch.zhaw.vorwahlen.util.ObjectMapperUtil.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * https://rieckpil.de/write-integration-tests-for-your-spring-websocket-endpoints/
 */
@ActiveProfiles("dev")
@AutoConfigureMockMvc
@SpringBootTest(properties = "classpath:settings.properties", webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class ElectionControllerTest {
    public static final String CONNECT_URL = "http://localhost:8080/stomp-ws-endpoint";
    private static final String REQUEST_MAPPING_PREFIX = "/elections";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ElectionService electionService;

    private WebSocketStompClient webSocketStompClient;

    public static final String DEV_USER_MAIL = "dev@zhaw.ch";

    static {
        System.setProperty("ADMIN", DEV_USER_MAIL);
    }

    @BeforeEach
    void setUp() {
        this.webSocketStompClient = new WebSocketStompClient(new SockJsClient(List.of(new WebSocketTransport(new StandardWebSocketClient()))));
    }

    @Test
    void testGetElection() {
        // prepare
        var statusDto = new ElectionStatusDTO(new ElectionStatusElementDTO(ModuleCategory.SUBJECT_MODULE, true, null),
                                              new ElectionStatusElementDTO(ModuleCategory.CONTEXT_MODULE, true, null),
                                              new ElectionStatusElementDTO(ModuleCategory.INTERDISCIPLINARY_MODULE, true, null),
                                              new ElectionStatusElementDTO(null, true, null));
        var transferDto = new ElectionTransferDTO(new ElectionStructureDTO(new ArrayList<>(), new ArrayList<>()),
                                                  statusDto, false, true);
        when(electionService.getElection(any())).thenReturn(transferDto);

        // execute
        try {
            var results = mockMvc.perform(MockMvcRequestBuilders
                                                  .get(REQUEST_MAPPING_PREFIX + "/" + DEV_USER_MAIL + "/structure")
                                                  .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").exists())
                    .andDo(print())
                    .andReturn();

            var resultDto = fromJsonResult(results, ElectionTransferDTO.class);
            assertEquals(transferDto, resultDto);
        } catch (Exception e) {
            fail(e);
        }

        // verify
        verify(electionService, times(1)).getElection(any());
    }

    @Test
    void testGetAllElections() {
        // prepare
        var electionDtoList = List.of(ElectionDTO.builder().id(1L).build());
        when(electionService.getAllElections()).thenReturn(electionDtoList);

        // execute
        try {
            var results = mockMvc.perform(MockMvcRequestBuilders
                                                  .get(REQUEST_MAPPING_PREFIX)
                                                  .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").exists())
                    .andDo(print())
                    .andReturn();

            var resultDtos = fromJsonResult(results, new TypeReference<List<ElectionDTO>>(){});
            assertIterableEquals(electionDtoList, resultDtos);
        } catch (Exception e) {
            fail(e);
        }

        // verify
        verify(electionService, times(1)).getAllElections();
    }

    @Test
    void testGetElectionById() {
        // prepare
        var electionDTO = ElectionDTO.builder().id(1L).build();
        when(electionService.getElectionById(anyLong())).thenReturn(electionDTO);

        // execute
        try {
            var results = mockMvc.perform(MockMvcRequestBuilders
                                                  .get(REQUEST_MAPPING_PREFIX + "/" + electionDTO.getId())
                                                  .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").exists())
                    .andDo(print())
                    .andReturn();

            var resultDto = fromJsonResult(results, ElectionDTO.class);
            assertEquals(electionDTO, resultDto);
        } catch (Exception e) {
            fail(e);
        }

        // verify
        verify(electionService, times(1)).getElectionById(anyLong());
    }

    @Test
    void testAddElection() {
        // prepare
        var electionDTO = ElectionDTO.builder()
                .id(1L)
                .studentEmail("hello@mail.ch")
                .electedModules(new HashSet<>())
                .validationSettingDTO(new ValidationSettingDTO(false, false, false, 0))
                .build();
        when(electionService.createElection(any())).thenReturn(electionDTO);

        // execute
        try {
            var result = mockMvc.perform(MockMvcRequestBuilders
                                                  .post(REQUEST_MAPPING_PREFIX)
                                                  .contentType(MediaType.APPLICATION_JSON)
                                                  .content(toJson(electionDTO))
                                                  .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").exists())
                    .andDo(print())
                    .andReturn();
            assertEquals(electionDTO, fromJsonResult(result, ElectionDTO.class));
        } catch (Exception e) {
            fail(e);
        }

        // verify
        verify(electionService, times(1)).createElection(any());
    }

    @Test
    void testDeleteElection() {
        // prepare
        var electionDTO = ElectionDTO.builder().id(1L).build();

        // execute
        try {
            mockMvc.perform(MockMvcRequestBuilders
                                                  .delete(REQUEST_MAPPING_PREFIX + "/" + electionDTO.getId())
                                                  .with(csrf()))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn();
        } catch (Exception e) {
            fail(e);
        }

        // verify
        verify(electionService, times(1)).deleteElectionById(anyLong());
    }

    @Test
    void testReplaceElectionById() {
        // prepare
        var electionDTO = ElectionDTO.builder()
                .id(1L)
                .studentEmail("hello@mail.ch")
                .electedModules(new HashSet<>())
                .validationSettingDTO(new ValidationSettingDTO(false, false, false, 0))
                .build();
        doNothing().when(electionService).updateElection(anyLong(), any());

        // execute
        try {
            mockMvc.perform(MockMvcRequestBuilders
                                    .put(REQUEST_MAPPING_PREFIX + "/" + electionDTO.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(electionDTO))
                                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn();
        } catch (Exception e) {
            fail(e);
        }

        // verify
        verify(electionService, times(1)).updateElection(anyLong(), any());
    }

    @Test
    void testExportElection() {
        // prepare
        var content = "Hello World!";
        when(electionService.exportElection()).thenReturn(content.getBytes());

        // execute
        try {
            var results = mockMvc.perform(MockMvcRequestBuilders
                                                  .get(REQUEST_MAPPING_PREFIX + "/export")
                                                  .with(csrf()))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn();

            assertEquals(ElectionController.EXCEL_MIME, results.getResponse().getContentType());
            assertEquals("attachment; filename=module_election.xlsx", results.getResponse().getHeader(HttpHeaders.CONTENT_DISPOSITION));
            assertEquals(content, results.getResponse().getContentAsString());
        } catch (Exception e) {
            fail(e);
        }

        // verify
        verify(electionService, times(1)).exportElection();
    }

    @Test
    void testSaveElection() throws InterruptedException, ExecutionException, TimeoutException {
        var blockingQueue = new ArrayBlockingQueue<ElectionTransferDTO>(1);

        webSocketStompClient.setMessageConverter(new MappingJackson2MessageConverter());

        var session = webSocketStompClient
                .connect(CONNECT_URL, new StompSessionHandlerAdapter() {})
                .get(1, TimeUnit.SECONDS);

        session.subscribe("/user/queue/electionSaveStatus", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ElectionTransferDTO.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                blockingQueue.add((ElectionTransferDTO) payload);
            }
        });

        var structureElement = new ModuleStructureElement(
                "Rapid Software Prototyping for Engineering Science",
                "t.BA.WM.RASOP-EN.19HS",
                false,
                5,
                ModuleCategory.INTERDISCIPLINARY_MODULE,
                4
        );

        var electionStatus = new ElectionStatusDTO(new ElectionStatusElementDTO(ModuleCategory.SUBJECT_MODULE, false, null),
                                                   new ElectionStatusElementDTO(ModuleCategory.CONTEXT_MODULE, false, null),
                                                   new ElectionStatusElementDTO(ModuleCategory.INTERDISCIPLINARY_MODULE, false, null),
                                                   new ElectionStatusElementDTO(null, false, null));
        var electionStructure = new ElectionStructureDTO(List.of(structureElement), new ArrayList<>());
        var electionTransfer = new ElectionTransferDTO(electionStructure, electionStatus,true, false);

        when(electionService.saveElection(any(), any(), any())).thenReturn(electionTransfer);

        var moduleToElect = "t.BA.WM.RASOP-EN.19HS"; // List.of(, "t.BA.WV.ESE.19HS", "t.BA.WM.SASEN-EN.19HS");
        session.send("/app/save", moduleToElect);
        assertEquals(electionTransfer, blockingQueue.poll(5, TimeUnit.SECONDS));
    }
}
