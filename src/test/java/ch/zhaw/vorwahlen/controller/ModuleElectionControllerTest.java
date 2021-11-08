package ch.zhaw.vorwahlen.controller;

import ch.zhaw.vorwahlen.model.dto.StudentDTO;
import ch.zhaw.vorwahlen.model.user.User;
import ch.zhaw.vorwahlen.service.ClassListService;
import ch.zhaw.vorwahlen.service.ElectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

    ModuleElectionController controller;

    private WebSocketStompClient webSocketStompClient;

    static {
        System.setProperty("ADMIN", "dev@zhaw.ch");
    }

    @BeforeEach
    void setUp() {
        this.webSocketStompClient = new WebSocketStompClient(new SockJsClient(List.of(new WebSocketTransport(new StandardWebSocketClient()))));

        controller = new ModuleElectionController(electionService, classListService);

        var studentDTO = StudentDTO.builder()
                .name("dev")
                .clazz("dev")
                .email("dev@zhaw.ch")
                .build();
        when(classListService.getStudentById(anyString())).thenReturn(Optional.of(studentDTO));
    }

    /* **************************************************************************************************************
     * Positive tests
     * ************************************************************************************************************** */

    @Test
    void testIsElectionValid() throws ExecutionException, InterruptedException, TimeoutException {
        var blockingQueue = new ArrayBlockingQueue<Boolean>(1);

        webSocketStompClient.setMessageConverter(new MappingJackson2MessageConverter());

        var session = webSocketStompClient
                .connect(CONNECT_URL, new StompSessionHandlerAdapter() {})
                .get(1, TimeUnit.SECONDS);

        session.subscribe("/user/queue/electionStatus", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Boolean.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                blockingQueue.add((Boolean) payload);
            }
        });

        session.send("/app/validate", "{}");
        assertFalse(blockingQueue.poll(1, TimeUnit.SECONDS));
    }

    @Test
    void testSaveElection() {
    }

    /* **************************************************************************************************************
     * Negative tests
     * ************************************************************************************************************** */

}
