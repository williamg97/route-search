package com.wjg.mapsearch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RouteSearchApplication implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(RouteSearchApplication.class);

    public static void main(String[] args) {
        logger.info("Starting route POI search application");
        SpringApplication.run(RouteSearchApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

    }
}
