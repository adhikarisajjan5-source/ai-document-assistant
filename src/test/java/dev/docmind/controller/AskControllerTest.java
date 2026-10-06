package dev.docmind.controller;

import dev.docmind.dto.AskResponse;
import dev.docmind.service.RagService;
import dev.docmind.support.TestAuthHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestAuthHelper testAuthHelper;

    @MockitoBean
    private RagService ragService;

    private String token;

    @BeforeAll
    void setUp() throws Exception {

        testAuthHelper.registerTestUser(
                mockMvc,
                "test@example.com",
                "testpass123"
        );

        token = testAuthHelper.getLoginToken(
                mockMvc,
                "test@example.com",
                "testpass123"
        );
    }

    @Test
    void askWithoutTokenReturns401() throws Exception {

        String requestJson = """
                {
                  "documentId": 4,
                  "query": "What is the remote work policy?"
                }
                """;

        mockMvc.perform(
                        post("/api/ask")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void askWithEmptyQueryReturns400() throws Exception {

        String requestJson = """
                {
                  "documentId": 4,
                  "query": ""
                }
                """;

        mockMvc.perform(
                        post("/api/ask")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void askWithMissingDocumentIdReturns400() throws Exception {

        String requestJson = """
                {
                  "query": "What is the remote work policy?"
                }
                """;

        mockMvc.perform(
                        post("/api/ask")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void askWithInvalidDocumentIdReturns400() throws Exception {

        String requestJson = """
                {
                  "documentId": 0,
                  "query": "What is the remote work policy?"
                }
                """;

        mockMvc.perform(
                        post("/api/ask")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void askExistingDocumentReturns200() throws Exception {

        AskResponse mockResponse =
                new AskResponse(
                        "Employees may work remotely.",
                        List.of()
                );

        when(
                ragService.ask(
                        anyLong(),
                        anyString()
                )
        ).thenReturn(mockResponse);

        String requestJson = """
                {
                  "documentId": 4,
                  "query": "What is the remote work policy?"
                }
                """;

        mockMvc.perform(
                        post("/api/ask")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.answer")
                                .value("Employees may work remotely.")
                )
                .andExpect(
                        jsonPath("$.sources")
                                .exists()
                );
    }

    @Test
    void askExistingDocumentReturnsNonEmptyAnswer() throws Exception {

        AskResponse mockResponse =
                new AskResponse(
                        "Employees may work remotely.",
                        List.of()
                );

        when(
                ragService.ask(
                        anyLong(),
                        anyString()
                )
        ).thenReturn(mockResponse);

        String requestJson = """
                {
                  "documentId": 4,
                  "query": "What is the remote work policy?"
                }
                """;

        mockMvc.perform(
                        post("/api/ask")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.answer")
                                .isNotEmpty()
                );
    }
}