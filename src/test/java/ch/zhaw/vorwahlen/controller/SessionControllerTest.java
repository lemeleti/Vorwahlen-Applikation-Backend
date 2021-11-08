package ch.zhaw.vorwahlen.controller;

import ch.zhaw.vorwahlen.model.user.User;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("dev")
@SpringBootTest(properties = "classpath:settings.properties")
@AutoConfigureMockMvc
class SessionControllerTest {

    private static final String REQUEST_MAPPING_PREFIX = "/session";

    @Autowired
    MockMvc mockMvc;

    @Test
    @Order(1)
    void testGetSessionInfo() {
        try {
            var user = User.builder()
                    .name("dev")
                    .lastName("dev")
                    .affiliation("student;member")
                    .homeOrg("zhaw.ch")
                    .mail("dev@zhaw.ch")
                    .role("ADMIN")
                    .build();

            mockMvc.perform(MockMvcRequestBuilders
                    .get(REQUEST_MAPPING_PREFIX + "/info")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").exists())
                    .andExpect(jsonPath("$.name").isNotEmpty())
                    .andExpect(jsonPath("$.name", is(user.getName())))
                    .andExpect(jsonPath("$.lastName").isNotEmpty())
                    .andExpect(jsonPath("$.lastName", is(user.getLastName())))
                    .andExpect(jsonPath("$.affiliation", is(user.getAffiliation())))
                    .andExpect(jsonPath("$.affiliation").isNotEmpty())
                    .andExpect(jsonPath("$.homeOrg").isNotEmpty())
                    .andExpect(jsonPath("$.homeOrg", is(user.getHomeOrg())))
                    .andExpect(jsonPath("$.mail").isNotEmpty())
                    .andExpect(jsonPath("$.mail", is(user.getMail())))
                    .andExpect(jsonPath("$.role").isNotEmpty())
                    .andExpect(jsonPath("$.role", is(user.getRole())))
                    .andDo(print());
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    @Order(2)
    void testIsAuthenticated() {
        try {
            mockMvc.perform(MockMvcRequestBuilders
                    .get(REQUEST_MAPPING_PREFIX + "/is-authenticated")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").exists())
                    .andExpect(jsonPath("$").isBoolean())
                    .andExpect(jsonPath("$", is(true)))
                    .andDo(print());
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    @Order(3)
    void testIsUserAdmin() {
        try {
            mockMvc.perform(MockMvcRequestBuilders
                    .get(REQUEST_MAPPING_PREFIX + "/is-admin")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").exists())
                    .andExpect(jsonPath("$").isBoolean())
                    .andExpect(jsonPath("$", is(true)))
                    .andDo(print());
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    @Order(999)
    void testDestroy() {
        try {
            mockMvc.perform(MockMvcRequestBuilders
                    .get(REQUEST_MAPPING_PREFIX + "/destroy")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").doesNotExist())
                    .andDo(print());
            // todo: verify that session is gone. check also info, isuseradmin and is authenticated
        } catch (Exception e) {
            fail(e);
        }
    }

}
