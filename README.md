# AI Document Assistant

[English](README.md) | [日本語](README_ja.md)

AI Document Assistant is a secure, locally operated RAG (Retrieval-Augmented Generation) backend built with Spring Boot and Spring AI.

Users can upload PDF documents and ask questions about their contents in natural language. The application extracts and chunks PDF text, generates embeddings, stores them in PostgreSQL with pgvector, retrieves relevant document sections, and uses a locally running LLM through Ollama to generate answers with source information.

The project was designed with document privacy, user isolation, and backend security as important requirements.

---

## Features

- User registration and login
- JWT-based authentication
- BCrypt password hashing
- PDF upload and validation
- PDF text extraction with Apache PDFBox
- Text chunking
- Local embedding generation with Ollama
- Vector storage using PostgreSQL + pgvector
- Semantic similarity search
- RAG-based document question answering
- Source/page information returned with answers
- Document ownership and user isolation
- Request validation
- Centralized exception handling
- Database schema management with Flyway
- Automated controller/integration tests

---

## Architecture

```text
Client / Postman
       |
       v
Spring Security
       |
       v
JWT Authentication Filter
       |
       v
REST Controllers
       |
       v
Application Services
       |
       +----------------------+
       |                      |
       v                      v
PostgreSQL / JPA        PDF Processing
       |                      |
       |                      v
       |                 Text Extraction
       |                      |
       |                      v
       |                  Chunking
       |                      |
       |                      v
       |                 Embedding Model
       |                      |
       +-----------> PostgreSQL + pgvector
                              |
                              v
                       Similarity Search
                              |
                              v
                         RAG Context
                              |
                              v
                         Local LLM
                           (Ollama)
                              |
                              v
                     Answer + Sources
```

---

## RAG Processing Flow

### Document Upload

```text
PDF Upload
    |
    v
Request Validation
    |
    v
PDF Signature / Content Validation
    |
    v
Apache PDFBox
    |
    v
Text Extraction by Page
    |
    v
Text Chunking
    |
    v
EmbeddingGemma
    |
    v
PostgreSQL + pgvector
```

Each vector record contains metadata such as the document ID, owner ID, page number, and chunk number.

### Question Answering

```text
Question
    |
    v
JWT Authentication
    |
    v
Document Ownership Check
    |
    v
Question Embedding
    |
    v
Vector Similarity Search
    |
    v
Relevant Document Chunks
    |
    v
RAG Prompt
    |
    v
Qwen3 via Ollama
    |
    v
Answer + Sources
```

The LLM is instructed to answer using only the retrieved document context.

---

## Security Design

Security is handled by the application before document content reaches the LLM.

### Authentication

The application uses JWT-based stateless authentication.

```text
Login
  |
  v
Email + Password Verification
  |
  v
JWT Issued
  |
  v
Authorization: Bearer <token>
  |
  v
JwtAuthenticationFilter
  |
  v
Authenticated Spring Security Context
```

Passwords are stored using BCrypt hashing.

### Document Authorization

Document access is restricted by ownership.

```text
Authenticated User
        |
        v
Document ID + Owner ID Check
        |
        v
Authorized Document
        |
        v
Vector Search
```

Vector searches are also filtered using both:

```text
documentId
ownerId
```

The client does not provide a trusted `userId` for authorization. The authenticated user is determined by the server from the security context.

### PDF Security

Uploaded files are checked using multiple validation layers, including:

- File presence
- MIME type
- Filename validation
- PDF file signature
- PDF parsing with Apache PDFBox
- Page validation
- Maximum upload size

### Error Handling

Unexpected server errors are logged server-side while clients receive a generic error response. Internal stack traces and database details are not intentionally exposed through API responses.

### Secrets

Database passwords and JWT signing secrets are not stored directly in the committed application configuration.

The application reads:

```properties
spring.datasource.password=${DB_PASSWORD}
app.jwt.secret=${JWT_SECRET}
```

from environment variables.

---

## Technology Stack

| Category | Technology |
|---|---|
| Language | Java 21 |
| Backend | Spring Boot |
| AI Framework | Spring AI |
| Security | Spring Security |
| Authentication | JWT |
| ORM | Spring Data JPA / Hibernate |
| Database | PostgreSQL |
| Vector Database | PostgreSQL + pgvector |
| PDF Processing | Apache PDFBox |
| Embedding Model | EmbeddingGemma |
| LLM | Qwen3 1.7B |
| Local AI Runtime | Ollama |
| Database Migration | Flyway |
| Build Tool | Maven |
| Testing | JUnit / Spring Boot Test / MockMvc |

---

## Project Structure

```text
src/main/java/dev/docmind
|
+-- chunk
|   +-- TextChunker
|
+-- config
|   +-- SecurityConfig
|
+-- controller
|   +-- AskController
|   +-- AuthController
|   +-- DocumentController
|   +-- HealthController
|   +-- SearchController
|
+-- dto
|
+-- entity
|
+-- exception
|
+-- pdf
|
+-- repository
|
+-- security
|
+-- service
|
+-- AiDocumentAssistantApplication
```

Database migrations are located in:

```text
src/main/resources/db/migration
```

---

## Database

The application uses PostgreSQL for relational data and pgvector for vector embeddings.

Main application tables include:

```text
users
documents
vector_store
flyway_schema_history
```

Flyway manages the core application schema.

Spring AI currently initializes the pgvector vector store schema.

Hibernate is configured to validate the database schema rather than automatically modifying it:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

---

## Prerequisites

Install the following before running the application:

- Java 21
- Maven
- PostgreSQL
- pgvector PostgreSQL extension
- Ollama

The following Ollama models are used:

```text
embeddinggemma
qwen3:1.7b
```

Pull them with:

```bash
ollama pull embeddinggemma
ollama pull qwen3:1.7b
```

---

## Environment Variables

The following environment variables are required:

```text
DB_PASSWORD
JWT_SECRET
```

Do not commit real credentials to the repository.

---

## Configuration

Example application configuration:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/ai_document_db
spring.datasource.username=ai_document_user
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=validate

spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.embedding.model=embeddinggemma
spring.ai.ollama.chat.model=qwen3:1.7b

spring.ai.vectorstore.pgvector.dimensions=768

app.jwt.secret=${JWT_SECRET}
```

---

## Running the Application

Make sure PostgreSQL and Ollama are running.

On Windows:

```bash
mvnw.cmd spring-boot:run
```

Or with Maven:

```bash
mvn spring-boot:run
```

The backend starts on:

```text
http://localhost:8080
```

---

## Main API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/health` | Application health check |
| POST | `/api/auth/register` | Register a user |
| POST | `/api/auth/login` | Login and obtain JWT |
| POST | `/api/documents/upload` | Upload and process a PDF |
| GET | `/api/documents` | Get documents owned by the authenticated user |
| POST | `/api/search` | Search relevant document chunks |
| POST | `/api/ask` | Ask a question about a document |

Protected endpoints require:

```http
Authorization: Bearer <JWT_TOKEN>
```

---

## Testing

The project includes automated tests for the main API flows, including:

- Authentication
- Document operations
- Vector search
- Document question answering

Tests can be run with:

```bash
mvn test
```

They can also be run directly through IntelliJ IDEA.

---

## Privacy

The current AI pipeline uses Ollama locally.

```text
PDF
 |
 v
Local Spring Boot Application
 |
 v
Local Embedding Model
 |
 v
Local PostgreSQL / pgvector
 |
 v
Local LLM
```

This design avoids sending document contents to a third-party cloud LLM API during local operation.

For an organizational deployment, the same architecture could use privately hosted AI infrastructure inside an organization's controlled environment.

---

## Current Limitations

- PDF only
- Image-only/scanned PDFs require OCR, which is not currently implemented
- Text chunking is relatively simple
- Local LLM quality depends on the selected model and available hardware
- No frontend UI is currently included
- The current development setup uses a local PostgreSQL instance and local Ollama
- Production deployment configuration is not yet included

---

## Future Improvements

Possible future improvements include:

- OCR support for scanned PDFs
- Improved chunking strategies
- Reranking of retrieved chunks
- Additional document formats
- Frontend dashboard
- Improved citation presentation
- Dedicated test database/profile
- Production deployment configuration
- Monitoring and observability
- Additional security testing

---

## Purpose

This project was created as a practical implementation of a secure AI-enabled backend using Spring Boot.

The main goals are to understand and demonstrate:

- Spring Boot application architecture
- REST API design
- Authentication and authorization
- Secure document handling
- PostgreSQL and database migration management
- Vector search
- Retrieval-Augmented Generation
- Local LLM integration
- Automated testing
- End-to-end application development

---

## License

No license has been specified for this project.