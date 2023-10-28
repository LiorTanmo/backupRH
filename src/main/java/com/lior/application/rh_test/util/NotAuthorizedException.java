package com.lior.application.rh_test.util;

public class NotAuthorizedException extends IllegalAccessException{

    public NotAuthorizedException (String msg){
        super(msg);
    }
}
