package dev.docmind.controller;

import dev.docmind.support.TestAuthHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestAuthHelper testAuthHelper;


    @Test
    void getDocumentsWithoutTokenReturns401() throws Exception {

        mockMvc.perform(
                        get("/api/documents")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(
                        jsonPath("$.status")
                                .value(401)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Authentication is required")
                );
    }


    @Test
    void uploadWithoutTokenReturns401() throws Exception {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "empty.pdf",
                        "application/pdf",
                        new byte[0]
                );

        mockMvc.perform(
                        multipart("/api/documents/upload")
                                .file(file)
                )
                .andExpect(status().isUnauthorized());
    }


    @Test
    void authenticatedUserCanGetDocuments() throws Exception {

        String token =
                testAuthHelper.getLoginToken(
                        mockMvc,
                        "test@example.com",
                        "sajjan"
                );

        mockMvc.perform(
                        get("/api/documents")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk());
    }


    @Test
    void authenticatedEmptyPdfUploadReturns400() throws Exception {

        String token =
                testAuthHelper.getLoginToken(
                        mockMvc,
                        "test@example.com",
                        "sajjan"
                );

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "empty.pdf",
                        "application/pdf",
                        new byte[0]
                );

        mockMvc.perform(
                        multipart("/api/documents/upload")
                                .file(file)
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message")
                                .value("File must not be empty")
                );
    }


    @Test
    void authenticatedNonPdfUploadReturns400() throws Exception {

        String token =
                testAuthHelper.getLoginToken(
                        mockMvc,
                        "test@example.com",
                        "sajjan"
                );

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "notes.txt",
                        "text/plain",
                        "hello".getBytes()
                );

        mockMvc.perform(
                        multipart("/api/documents/upload")
                                .file(file)
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message")
                                .value("Only PDF files are allowed")
                );
    }
}