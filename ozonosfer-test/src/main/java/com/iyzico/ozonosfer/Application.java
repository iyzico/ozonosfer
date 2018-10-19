package com.iyzico.ozonosfer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableHystrix
@ComponentScan(basePackages = "com.iyzico")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}