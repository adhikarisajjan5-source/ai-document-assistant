const loginForm =
    document.getElementById("loginForm");

const registerForm =
    document.getElementById("registerForm");

const authMessage =
    document.getElementById("authMessage");


/*
 * If the user already has a token,
 * go directly to the application.
 */
const existingToken =
    localStorage.getItem("jwtToken");

if (existingToken) {
    window.location.href = "/app.html";
}


/*
 * LOGIN
 */
loginForm.addEventListener(
    "submit",
    async function (event) {

        event.preventDefault();

        clearMessage();


        const email =
            document
                .getElementById("loginEmail")
                .value
                .trim();


        const password =
            document
                .getElementById("loginPassword")
                .value;


        const requestBody = {
            email: email,
            password: password
        };


        try {

            const response =
                await fetch(
                    "/api/auth/login",
                    {
                        method: "POST",

                        headers: {
                            "Content-Type": "application/json"
                        },

                        body: JSON.stringify(requestBody)
                    }
                );


            const data =
                await readJsonResponse(response);


            if (!response.ok) {

                showError(
                    extractErrorMessage(
                        data,
                        "Login failed."
                    )
                );

                return;
            }


            /*
             * Supports a few common names.
             * Your backend most likely returns "token".
             */
            const token =
                data.token ||
                data.accessToken ||
                data.jwt;


            if (!token) {

                showError(
                    "Login succeeded, but no JWT token was returned."
                );

                console.error(
                    "Login response:",
                    data
                );

                return;
            }


            localStorage.setItem(
                "jwtToken",
                token
            );


            window.location.href =
                "/app.html";

        } catch (error) {

            console.error(error);

            showError(
                "Could not connect to the server."
            );
        }
    }
);


/*
 * REGISTER
 */
registerForm.addEventListener(
    "submit",
    async function (event) {

        event.preventDefault();

        clearMessage();


        const email =
            document
                .getElementById("registerEmail")
                .value
                .trim();


        const password =
            document
                .getElementById("registerPassword")
                .value;


        const requestBody = {
            email: email,
            password: password
        };


        try {

            const response =
                await fetch(
                    "/api/auth/register",
                    {
                        method: "POST",

                        headers: {
                            "Content-Type": "application/json"
                        },

                        body: JSON.stringify(requestBody)
                    }
                );


            const data =
                await readJsonResponse(response);


            if (!response.ok) {

                showError(
                    extractErrorMessage(
                        data,
                        "Registration failed."
                    )
                );

                return;
            }


            showSuccess(
                "Registration successful. You can now login."
            );


            document
                .getElementById("registerForm")
                .reset();


        } catch (error) {

            console.error(error);

            showError(
                "Could not connect to the server."
            );
        }
    }
);


/*
 * Utility functions
 */

async function readJsonResponse(response) {

    const text =
        await response.text();


    if (!text) {
        return {};
    }


    try {

        return JSON.parse(text);

    } catch {

        return {
            message: text
        };
    }
}


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


function showSuccess(message) {

    authMessage.textContent =
        message;

    authMessage.className =
        "message success-message";
}


function showError(message) {

    authMessage.textContent =
        message;

    authMessage.className =
        "message error-message";
}


function clearMessage() {

    authMessage.textContent = "";

    authMessage.className =
        "message";
}