package org.example.dto;

public class LoginRequestDTO {
    private String numberPhone;
    private String password;

    public LoginRequestDTO(String numberPhone, String password) {
        this.numberPhone = numberPhone;
        this.password = password;
    }

    public String getNumberPhone() {
        return numberPhone;
    }
    public void setNumberPhone(String numberPhone) {
        this.numberPhone = numberPhone;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}
