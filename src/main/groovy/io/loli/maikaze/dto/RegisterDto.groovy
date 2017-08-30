package io.loli.maikaze.dto;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty

import javax.validation.constraints.NotNull;

class RegisterDto {
    @NotEmpty
    @NotNull
    private String userName;
    @NotEmpty
    @Length(min = 6, max = 32)
    @NotNull
    private String password;
    @NotEmpty
    @NotNull
    @Email
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


}