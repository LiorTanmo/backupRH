package com.lior.application.rh_test.util;

import lombok.Getter;

@Getter
public class NewsNotFoundException extends RuntimeException{
    private final String message;

    public  NewsNotFoundException(){
        this.message = "Couldn't find news corresponding to your request";
    }
}
