package com.xyz.question_bank_management_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class QuestionBankManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuestionBankManagementSystemApplication.class, args);
    }
}
