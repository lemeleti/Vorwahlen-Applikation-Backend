package ch.zhaw.vorwahlen.controller;

import ch.zhaw.vorwahlen.model.dto.ElectionStructureDTO;
import ch.zhaw.vorwahlen.model.dto.ElectionTransferDTO;
import ch.zhaw.vorwahlen.model.dto.StudentDTO;
import ch.zhaw.vorwahlen.model.modules.ModuleCategory;
import ch.zhaw.vorwahlen.model.modulestructure.ModuleStructureElement;
import ch.zhaw.vorwahlen.service.ClassListService;
import ch.zhaw.vorwahlen.service.ElectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * https://rieckpil.de/write-integration-tests-for-your-spring-websocket-endpoints/
 */
@ActiveProfiles("dev")
@AutoConfigureMockMvc
@SpringBootTest(properties = "classpath:settings.properties", webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class ModuleElectionControllerTest {
    public static final String CONNECT_URL = "http://localhost:8080/stomp-ws-endpoint";

    @MockBean
    ElectionService electionService;

    @MockBean
    ClassListService classListService;

    private WebSocketStompClient webSocketStompClient;

    static {
        System.setProperty("ADMIN", "dev@zhaw.ch");
    }

    @BeforeEach
    void setUp() {
        this.webSocketStompClient = new WebSocketStompClient(new SockJsClient(List.of(new WebSocketTransport(new StandardWebSocketClient()))));

        var studentDTO = StudentDTO.builder()
                .name("dev")
                .clazz("dev")
                .email("dev@zhaw.ch")
                .build();
        when(classListService.getStudentById(anyString())).thenReturn(Optional.of(studentDTO));
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

        var electionStructure = new ElectionStructureDTO(List.of(structureElement), new ArrayList<>());
        var electionTransfer = new ElectionTransferDTO(electionStructure, true, false);

        when(electionService.saveElection(any(), any())).thenReturn(electionTransfer);

        var moduleToElect = "t.BA.WM.RASOP-EN.19HS"; // List.of(, "t.BA.WV.ESE.19HS", "t.BA.WM.SASEN-EN.19HS");
        session.send("/app/save", moduleToElect);
        assertEquals(electionTransfer, blockingQueue.poll(5, TimeUnit.SECONDS));
    }
}
