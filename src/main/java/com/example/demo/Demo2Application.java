package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Demo2Application {

    public static void main(String[] args) {
        System.out.println("hotfix");
        System.out.println("hotfix2");
        System.out.println("hotfix3");
        SpringApplication.run(Demo2Application.class, args);
    }

}
