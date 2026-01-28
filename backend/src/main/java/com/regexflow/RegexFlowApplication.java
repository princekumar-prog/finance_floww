package com.regexflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class RegexFlowApplication {
    public static void main(String[] args) {
        SpringApplication.run(RegexFlowApplication.class, args);
    }
}
