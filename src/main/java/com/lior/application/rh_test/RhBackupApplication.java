package com.lior.application.rh_test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class RhBackupApplication {

    static final Logger log = LoggerFactory.getLogger(RhBackupApplication.class);
    public static void main(String[] args) {
        SpringApplication.run(RhBackupApplication.class, args);
    }

    @Bean
    public ModelMapper modelMapper(){
        return new ModelMapper();
    }

}
