package dev.docmind.support;

import dev.docmind.dto.LoginRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Component
public class TestAuthHelper {

    private final ObjectMapper objectMapper;

    public TestAuthHelper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String getLoginToken(
            MockMvc mockMvc,
            String email,
            String password
    ) throws Exception {

        LoginRequest loginRequest = new LoginRequest();

        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        String loginJson =
                objectMapper.writeValueAsString(loginRequest);

        MvcResult loginResult =
                mockMvc.perform(
                                post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(loginJson)
                        )
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.token").exists())
                        .andReturn();

        String responseBody =
                loginResult.getResponse().getContentAsString();

        return objectMapper
                .readTree(responseBody)
                .get("token")
                .asText();
    }
}