package org.example.boond.service;

import org.example.boond.model.ScanRequest;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

@Service
public class BoondScraperService {

    private final WebDriver driver;

    public BoondScraperService(WebDriver driver) {
        this.driver = driver;
    }

    public File downloadTimesheet(ScanRequest request) throws Exception {
        try {
            login(request.getUsername(), request.getPassword());
            navigateToTimesheet(request.getTimesheetId());
            return downloadDocument(request.getDocumentName());
        } catch (Exception e) {
            throw new RuntimeException("Failed to download timesheet: " + e.getMessage(), e);
        }
    }

    private void login(String username, String password) {
        driver.get("https://ui.boondmanager.com/login");
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        WebElement usernameField = driver.findElement(By.name("username"));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        usernameField.sendKeys(username);
        passwordField.sendKeys(password);
        loginButton.click();

        // Wait for login to complete
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.urlContains("dashboard"));
    }

    private void navigateToTimesheet(String timesheetId) {
        driver.get("https://ui.boondmanager.com/times-reports/" + timesheetId);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    private File downloadDocument(String documentName) throws Exception {
        WebElement document = driver.findElement(By.xpath("//a[contains(text(), '" + documentName + "')]"));
        String href = document.getAttribute("href");

        // Create a temporary file
        Path tempFile = Files.createTempFile("timesheet", getFileExtension(documentName));

        // Download the file using Selenium
        driver.get(href);
        Thread.sleep(2000); // Wait for download to complete

        return tempFile.toFile();
    }

    private String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }
}