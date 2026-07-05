package org.xcepto.xceptoj.docs.models;

public class RegisterRequest {
    public String username;
    public String password;

    public RegisterRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
