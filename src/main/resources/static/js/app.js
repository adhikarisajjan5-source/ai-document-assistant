const token =
    localStorage.getItem("jwtToken");


/*
 * User must be logged in.
 */
if (!token) {
    window.location.href =
        "/login.html";
}


/*
 * Selected document state
 */
let selectedDocumentId = null;
let selectedDocumentName = null;


/*
 * HTML elements
 */
const documentsContainer =
    document.getElementById(
        "documentsContainer"
    );

const selectedDocumentElement =
    document.getElementById(
        "selectedDocument"
    );

const uploadMessage =
    document.getElementById(
        "uploadMessage"
    );

const askMessage =
    document.getElementById(
        "askMessage"
    );

const answerSection =
    document.getElementById(
        "answerSection"
    );

const answerContent =
    document.getElementById(
        "answerContent"
    );

const sourcesContainer =
    document.getElementById(
        "sourcesContainer"
    );

const askLoading =
    document.getElementById(
        "askLoading"
    );

const documentsLoading =
    document.getElementById(
        "documentsLoading"
    );


/*
 * Run when page loads.
 */
document.addEventListener(
    "DOMContentLoaded",
    function () {
        loadDocuments();
    }
);


/*
 * LOGOUT
 */
document
    .getElementById("logoutButton")
    .addEventListener(
        "click",
        function () {

            localStorage.removeItem(
                "jwtToken"
            );

            window.location.href =
                "/login.html";
        }
    );


/*
 * REFRESH DOCUMENTS
 */
document
    .getElementById(
        "refreshDocumentsButton"
    )
    .addEventListener(
        "click",
        function () {
            loadDocuments();
        }
    );


/*
 * UPLOAD PDF
 */
document
    .getElementById("uploadButton")
    .addEventListener(
        "click",
        uploadPdf
    );


async function uploadPdf() {

    clearUploadMessage();


    const fileInput =
        document.getElementById(
            "pdfFile"
        );


    if (
        !fileInput.files ||
        fileInput.files.length === 0
    ) {

        showUploadError(
            "Please select a PDF file."
        );

        return;
    }


    const file =
        fileInput.files[0];


    const formData =
        new FormData();


    formData.append(
        "file",
        file
    );


    const uploadButton =
        document.getElementById(
            "uploadButton"
        );


    uploadButton.disabled = true;

    uploadButton.textContent =
        "Uploading...";


    try {

        const response =
            await authenticatedFetch(
                "/api/documents/upload",
                {
                    method: "POST",
                    body: formData
                }
            );


        const data =
            await readJsonResponse(
                response
            );


        if (!response.ok) {

            showUploadError(
                extractErrorMessage(
                    data,
                    "PDF upload failed."
                )
            );

            return;
        }


        showUploadSuccess(
            "PDF uploaded and processed successfully."
        );


        fileInput.value = "";


        await loadDocuments();


    } catch (error) {

        console.error(error);

        showUploadError(
            "Could not upload the PDF."
        );

    } finally {

        uploadButton.disabled = false;

        uploadButton.textContent =
            "Upload PDF";
    }
}


/*
 * LOAD DOCUMENTS
 */
async function loadDocuments() {

    documentsLoading.classList.remove(
        "hidden"
    );


    documentsContainer.innerHTML = "";


    try {

        const response =
            await authenticatedFetch(
                "/api/documents",
                {
                    method: "GET"
                }
            );


        const data =
            await readJsonResponse(
                response
            );


        if (!response.ok) {

            documentsContainer.innerHTML =
                "<p>Could not load documents.</p>";

            return;
        }


        renderDocuments(data);


    } catch (error) {

        console.error(error);

        documentsContainer.innerHTML =
            "<p>Could not connect to the server.</p>";

    } finally {

        documentsLoading.classList.add(
            "hidden"
        );
    }
}


/*
 * DOCUMENT LIST RENDERING
 */
function renderDocuments(documents) {

    documentsContainer.innerHTML = "";


    if (
        !Array.isArray(documents) ||
        documents.length === 0
    ) {

        documentsContainer.innerHTML =
            "<p>No documents uploaded yet.</p>";

        return;
    }


    documents.forEach(
        function (documentItem) {

            const documentElement =
                document.createElement(
                    "div"
                );


            documentElement.className =
                "document-item";


            if (
                documentItem.id ===
                selectedDocumentId
            ) {

                documentElement.classList.add(
                    "selected"
                );
            }


            const fileName =
                documentItem.originalFilename ||
                documentItem.filename ||
                "Unnamed document";


            const status =
                documentItem.status ||
                "UNKNOWN";


            documentElement.innerHTML = `

                <div class="document-info">

                    <span class="document-name">
                        ${escapeHtml(fileName)}
                    </span>

                    <span class="document-id">
                        Document ID:
                        ${documentItem.id}
                    </span>

                </div>


                <span class="
                    status
                    ${getStatusClass(status)}
                ">
                    ${escapeHtml(status)}
                </span>

            `;


            documentElement.addEventListener(
                "click",
                function () {

                    selectDocument(
                        documentItem.id,
                        fileName,
                        documentElement
                    );
                }
            );


            documentsContainer.appendChild(
                documentElement
            );
        }
    );
}


/*
 * SELECT DOCUMENT
 */
function selectDocument(
    documentId,
    documentName,
    clickedElement
) {

    selectedDocumentId =
        documentId;

    selectedDocumentName =
        documentName;


    selectedDocumentElement.textContent =
        documentName +
        " (ID: " +
        documentId +
        ")";


    answerSection.classList.add(
        "hidden"
    );


    /*
     * Remove selected style
     * from all documents.
     */
    const allDocuments =
        document.querySelectorAll(
            ".document-item"
        );


    allDocuments.forEach(
        function (item) {

            item.classList.remove(
                "selected"
            );
        }
    );


    /*
     * Highlight clicked document.
     */
    if (clickedElement) {

        clickedElement.classList.add(
            "selected"
        );
    }
}


/*
 * ASK QUESTION
 */
document
    .getElementById("askButton")
    .addEventListener(
        "click",
        askQuestion
    );


async function askQuestion() {

    clearAskMessage();


    if (!selectedDocumentId) {

        showAskError(
            "Please select a document first."
        );

        return;
    }


    const questionInput =
        document.getElementById(
            "questionInput"
        );


    const question =
        questionInput.value.trim();


    if (!question) {

        showAskError(
            "Please enter a question."
        );

        return;
    }


    const requestBody = {

        documentId:
            selectedDocumentId,

        query:
            question
    };


    const askButton =
        document.getElementById(
            "askButton"
        );


    askButton.disabled = true;


    askLoading.classList.remove(
        "hidden"
    );


    answerSection.classList.add(
        "hidden"
    );


    try {

        const response =
            await authenticatedFetch(
                "/api/ask",
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body:
                        JSON.stringify(
                            requestBody
                        )
                }
            );


        const data =
            await readJsonResponse(
                response
            );


        if (!response.ok) {

            showAskError(
                extractErrorMessage(
                    data,
                    "Question could not be processed."
                )
            );

            return;
        }


        displayAnswer(data);


    } catch (error) {

        console.error(error);

        showAskError(
            "Could not connect to the AI service."
        );

    } finally {

        askButton.disabled = false;

        askLoading.classList.add(
            "hidden"
        );
    }
}


/*
 * DISPLAY AI ANSWER
 */
function displayAnswer(data) {

    answerSection.classList.remove(
        "hidden"
    );


    answerContent.textContent =
        data.answer ||
        "No answer was returned.";


    sourcesContainer.innerHTML = "";


    const sources =
        data.sources || [];


    if (
        !Array.isArray(sources) ||
        sources.length === 0
    ) {

        sourcesContainer.innerHTML =
            "<p>No source information returned.</p>";

        return;
    }


    sources.forEach(
        function (source) {

            const sourceElement =
                document.createElement(
                    "div"
                );


            sourceElement.className =
                "source-item";


            const pageNumber =
                source.pageNumber ??
                source.page ??
                "Unknown";


            const chunkNumber =
                source.chunkNumber ??
                source.chunk ??
                "Unknown";


            sourceElement.textContent =
                "Page " +
                pageNumber +
                " · Chunk " +
                chunkNumber;


            sourcesContainer.appendChild(
                sourceElement
            );
        }
    );
}


/*
 * AUTHENTICATED HTTP REQUEST
 */
async function authenticatedFetch(
    url,
    options = {}
) {

    const headers =
        new Headers(
            options.headers || {}
        );


    headers.set(
        "Authorization",
        "Bearer " + token
    );


    const requestOptions = {
        ...options,
        headers: headers
    };


    const response =
        await fetch(
            url,
            requestOptions
        );


    /*
     * JWT expired or invalid.
     */
    if (
        response.status === 401
    ) {

        localStorage.removeItem(
            "jwtToken"
        );


        window.location.href =
            "/login.html";


        throw new Error(
            "Authentication expired."
        );
    }


    return response;
}


/*
 * RESPONSE HANDLING
 */
async function readJsonResponse(
    response
) {

    const text =
        await response.text();


    if (!text) {
        return {};
    }


    try {

        return JSON.parse(
            text
        );

    } catch {

        return {
            message: text
        };
    }
}


/*
 * ERROR MESSAGE EXTRACTION
 */
function extractErrorMessage(
    data,
    defaultMessage
) {

    if (!data) {
        return defaultMessage;
    }


    if (data.message) {
        return data.message;
    }


    if (data.error) {
        return data.error;
    }


    return defaultMessage;
}


/*
 * DOCUMENT STATUS CSS
 */
function getStatusClass(status) {

    switch (
        String(status).toUpperCase()
        ) {

        case "READY":
            return "status-ready";

        case "PROCESSING":
            return "status-processing";

        case "FAILED":
            return "status-failed";

        case "UPLOADED":
            return "status-uploaded";

        default:
            return "";
    }
}


/*
 * Prevent HTML injection.
 */
function escapeHtml(value) {

    const div =
        document.createElement(
            "div"
        );


    div.textContent =
        String(value);


    return div.innerHTML;
}


/*
 * UPLOAD MESSAGES
 */
function showUploadSuccess(message) {

    uploadMessage.textContent =
        message;

    uploadMessage.className =
        "message success-message";
}


function showUploadError(message) {

    uploadMessage.textContent =
        message;

    uploadMessage.className =
        "message error-message";
}


function clearUploadMessage() {

    uploadMessage.textContent = "";

    uploadMessage.className =
        "message";
}


/*
 * ASK MESSAGES
 */
function showAskError(message) {

    askMessage.textContent =
        message;

    askMessage.className =
        "message error-message";
}


function clearAskMessage() {

    askMessage.textContent = "";

    askMessage.className =
        "message";
}