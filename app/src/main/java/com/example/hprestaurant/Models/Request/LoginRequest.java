package com.example.hprestaurant.Models.Request;

public class LoginRequest {private String phone_number;
    private String password;

    public LoginRequest(String phone_number, String password) {
        this.phone_number = phone_number;
        this.password = password;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
