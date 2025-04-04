package com.example.GetJobV101.dto;

import org.springframework.http.HttpStatus;

public class ErrorResponse {

    private int status;
    private String message;

    // 생성자
    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }

    // Getter 및 Setter
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
