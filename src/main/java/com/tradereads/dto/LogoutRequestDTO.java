package com.tradereads.dto;

public class LogoutRequestDTO {
    private String refreshToken;

    public LogoutRequestDTO() {}

    public String getRefreshToken() {
        return refreshToken;
    }
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
