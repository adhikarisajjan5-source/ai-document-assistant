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

    public void registerTestUser(
            MockMvc mockMvc,
            String email,
            String password
    ) throws Exception {

        String registerJson = """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, password);

        MvcResult result = mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(registerJson)
                )
                .andReturn();

        int status = result.getResponse().getStatus();

        // 200 = user created successfully
        if (status == 200) {
            return;
        }

        // 400 can mean the shared test user already exists.
        // Verify that the existing user can actually log in
        // with the expected test password.
        if (status == 400) {
            getLoginToken(mockMvc, email, password);
            return;
        }

        throw new IllegalStateException(
                "Could not prepare test user. HTTP status: "
                        + status
                        + ", response: "
                        + result.getResponse().getContentAsString()
        );
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