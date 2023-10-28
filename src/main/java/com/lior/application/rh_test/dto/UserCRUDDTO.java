package com.lior.application.rh_test.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter@Setter
public class UserCRUDDTO {

    @NotEmpty(message = "username can't be empty")
    private String username;

    @NotEmpty(message = "password can't be empty")
    private String password;

    @NotEmpty(message = "name can't be empty")
    private String name;

    @NotEmpty(message = "surname can't be empty")
    private String surname;

    @NotEmpty(message = "parentName can't be empty")
    private String parentName;

    //@NotEmpty(message = "Role can't be empty")
    private String role;
}
