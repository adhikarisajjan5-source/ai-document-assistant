package dev.docmind.dto;

public class LoginResponse {

    private final Long id;
    private final String email;
    private final String role;
    private final String token;

    public LoginResponse(
            Long id,
            String email,
            String role,
            String token) {

        this.id = id;
        this.email = email;
        this.role = role;
        this.token = token;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getToken() {
        return token;
    }
}