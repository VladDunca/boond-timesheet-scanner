package com.example.boond.service;

import com.example.boond.model.ScanRequest;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

@Service
public class BoondScraperService {

    private static final Logger logger = LoggerFactory.getLogger(BoondScraperService.class);
    private final WebDriver driver;

    public BoondScraperService(WebDriver driver) {
        this.driver = driver;
    }

    public File downloadTimesheet(ScanRequest request) throws Exception {
        try {
            logger.info("Attempting to login...");
            login(request.getUsername(), request.getPassword());

            logger.info("Navigating to timesheet...");
            navigateToTimesheet(request.getTimesheetId());

            logger.info("Downloading document...");
            return downloadDocument(request.getDocumentName());

        } catch (Exception e) {
            logger.error("Failed to download timesheet", e);
            throw new RuntimeException("Failed to download timesheet: " + e.getMessage(), e);
        }
    }

    private void login(String username, String password) {
        driver.get("https://ui.boondmanager.com/login");
        logger.info("Waiting for login page to load...");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        usernameField.sendKeys(username);
        passwordField.sendKeys(password);
        loginButton.click();

        logger.info("Login credentials submitted");

        // Wait for login to complete
        wait.until(ExpectedConditions.urlContains("dashboard"));
        logger.info("Login successful");
    }

    private void navigateToTimesheet(String timesheetId) {
        String url = "https://ui.boondmanager.com/times-reports/" + timesheetId;
        logger.info("Navigating to: {}", url);
        driver.get(url);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }

    private File downloadDocument(String documentName) throws Exception {
        logger.info("Looking for document: {}", documentName);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement document = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//a[contains(text(), '" + documentName + "')]")
                )
        );

        String href = document.getAttribute("href");
        logger.info("Document link found: {}", href);

        // Create a temporary file
        Path tempFile = Files.createTempFile("timesheet", getFileExtension(documentName));
        logger.info("Created temporary file: {}", tempFile);

        // Download the file using Selenium
        driver.get(href);
        Thread.sleep(2000); // Wait for download to complete

        return tempFile.toFile();
    }

    private String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }
}