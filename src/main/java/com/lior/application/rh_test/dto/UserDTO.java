package com.lior.application.rh_test.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {

    @NotEmpty
    private String username;

    private String name;

    private String surname;

    private String parentName;

    private String role;
}
