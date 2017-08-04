package com.gosenk.transit.parser;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = {"com.gosenk.transit.api.repository"})
@ComponentScan({"com.gosenk.transit.api"})
@EntityScan(basePackages = "com.gosenk.transit.api")
public class Application {
    public static void main(String[] args) {
        new SpringApplicationBuilder(Main.class).web(false).run(args);
    }
}