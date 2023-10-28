package com.lior.application.rh_test.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

//TODO probably needs adjustments to User
@Getter@Setter
public class CommentDTO {

    int id;

    @Size(max = 300, message = "Up to 300 characters")
    @NotNull
    private String text;

    private UserDTO comment_author;
}
