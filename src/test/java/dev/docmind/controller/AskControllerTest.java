package dev.docmind.controller;

import dev.docmind.support.TestAuthHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestAuthHelper testAuthHelper;


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

        String token =
                testAuthHelper.getLoginToken(
                        mockMvc,
                        "test@example.com",
                        "sajjan"
                );

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
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message")
                                .value("Query must not be empty")
                );
    }


    @Test
    void askWithMissingDocumentIdReturns400() throws Exception {

        String token =
                testAuthHelper.getLoginToken(
                        mockMvc,
                        "test@example.com",
                        "sajjan"
                );

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
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message")
                                .value("Document ID is required")
                );
    }


    @Test
    void askWithInvalidDocumentIdReturns400() throws Exception {

        String token =
                testAuthHelper.getLoginToken(
                        mockMvc,
                        "test@example.com",
                        "sajjan"
                );

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
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message")
                                .value("Document ID must be greater than zero")
                );
    }


    @Test
    void askForNonExistingDocumentReturns404() throws Exception {

        String token =
                testAuthHelper.getLoginToken(
                        mockMvc,
                        "test@example.com",
                        "sajjan"
                );

        String requestJson = """
                {
                  "documentId": 999999,
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
                .andExpect(status().isNotFound());
    }


    @Test
    void askExistingDocumentReturns200() throws Exception {

        String token =
                testAuthHelper.getLoginToken(
                        mockMvc,
                        "test@example.com",
                        "sajjan"
                );

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
                                .exists()
                )
                .andExpect(
                        jsonPath("$.sources")
                                .exists()
                );
    }


    @Test
    void askExistingDocumentReturnsNonEmptyAnswer() throws Exception {

        String token =
                testAuthHelper.getLoginToken(
                        mockMvc,
                        "test@example.com",
                        "sajjan"
                );

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