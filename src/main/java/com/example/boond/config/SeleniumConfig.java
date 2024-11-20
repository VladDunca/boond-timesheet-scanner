package com.example.boond.config;

import jakarta.annotation.PostConstruct;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SeleniumConfig {

    private static final Logger logger = LoggerFactory.getLogger(SeleniumConfig.class);

    @PostConstruct
    void setup() {
        try {
            String chromeDriverPath = "C:\\Program Files\\Chrome Driver\\chromedriver-win64";
            System.setProperty("webdriver.chrome.driver", chromeDriverPath);
            logger.info("ChromeDriver setup completed successfully: {}", chromeDriverPath);
        } catch (Exception e) {
            logger.error("Failed to setup ChromeDriver", e);
            throw new RuntimeException("ChromeDriver setup failed", e);
        }
    }

    @Bean
    public WebDriver webDriver() {
        ChromeOptions options = new ChromeOptions();
        //options.addArguments("--headless");
        options.addArguments("--start-maximized");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        return new ChromeDriver(options);
    }
}
