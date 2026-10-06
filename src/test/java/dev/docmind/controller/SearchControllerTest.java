package dev.docmind.controller;

import dev.docmind.dto.SearchResultResponse;
import dev.docmind.entity.User;
import dev.docmind.service.CurrentUserService;
import dev.docmind.service.DocumentService;
import dev.docmind.service.VectorStoreService;
import dev.docmind.support.TestAuthHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestAuthHelper testAuthHelper;

    @MockitoBean
    private VectorStoreService vectorStoreService;

    @MockitoBean
    private DocumentService documentService;

    @MockitoBean
    private CurrentUserService currentUserService;

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
    void searchWithoutTokenReturns401() throws Exception {

        String requestJson = """
                {
                  "documentId": 4,
                  "query": "What is the remote work policy?"
                }
                """;

        mockMvc.perform(
                        post("/api/search")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void searchWithEmptyQueryReturns400() throws Exception {

        String requestJson = """
                {
                  "documentId": 4,
                  "query": ""
                }
                """;

        mockMvc.perform(
                        post("/api/search")
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
    void searchWithMissingDocumentIdReturns400() throws Exception {

        String requestJson = """
                {
                  "query": "What is the remote work policy?"
                }
                """;

        mockMvc.perform(
                        post("/api/search")
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
    void searchWithInvalidDocumentIdReturns400() throws Exception {

        String requestJson = """
                {
                  "documentId": 0,
                  "query": "What is the remote work policy?"
                }
                """;

        mockMvc.perform(
                        post("/api/search")
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
    void searchForNonExistingDocumentReturns404() throws Exception {

        User mockUser = mock(User.class);

        when(mockUser.getId())
                .thenReturn(1L);

        when(currentUserService.getCurrentUser())
                .thenReturn(mockUser);

        when(documentService.existsByIdAndOwnerId(
                999999L,
                1L
        )).thenReturn(false);

        String requestJson = """
                {
                  "documentId": 999999,
                  "query": "What is the remote work policy?"
                }
                """;

        mockMvc.perform(
                        post("/api/search")
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
    void searchExistingDocumentReturns200() throws Exception {

        User mockUser = mock(User.class);

        when(mockUser.getId())
                .thenReturn(1L);

        when(currentUserService.getCurrentUser())
                .thenReturn(mockUser);

        when(documentService.existsByIdAndOwnerId(
                4L,
                1L
        )).thenReturn(true);

        when(vectorStoreService.search(
                anyLong(),
                anyLong(),
                anyString()
        )).thenReturn(List.<SearchResultResponse>of());

        String requestJson = """
                {
                  "documentId": 4,
                  "query": "What is the remote work policy?"
                }
                """;

        mockMvc.perform(
                        post("/api/search")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isOk());
    }
}