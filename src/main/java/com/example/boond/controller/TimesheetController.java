package com.example.boond.controller;

import com.example.boond.model.ErrorResponse;
import com.example.boond.model.ScanRequest;
import com.example.boond.model.ScanResult;
import com.example.boond.service.BoondScraperService;
import com.example.boond.service.DocumentScannerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.File;

@RestController
@RequestMapping("/api/timesheet")
@CrossOrigin(origins = "*") // Allow requests from Postman
public class TimesheetController {

    private static final Logger logger = LoggerFactory.getLogger(TimesheetController.class);
    private final BoondScraperService scraperService;
    private final DocumentScannerService scannerService;

    public TimesheetController(BoondScraperService scraperService, DocumentScannerService scannerService) {
        this.scraperService = scraperService;
        this.scannerService = scannerService;
    }

    @PostMapping("/scan")
    public ResponseEntity<?> scanTimesheet(@RequestBody ScanRequest request) {
        logger.info("Received scan request for document: {}", request.getDocumentName());

        // Validate request
        if (request.getTimesheetId() == null || request.getTimesheetId().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Invalid Request", "timesheetId is required"));
        }
        if (request.getDocumentName() == null || request.getDocumentName().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Invalid Request", "documentName is required"));
        }
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Invalid Request", "username is required"));
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Invalid Request", "password is required"));
        }

        try {
            logger.info("Downloading timesheet...");
            File document = scraperService.downloadTimesheet(request);

            logger.info("Scanning document...");
            ScanResult result = scannerService.scanDocument(document, request.getDocumentName());

            logger.info("Scan completed successfully");
            document.delete(); // Clean up temporary file
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("Error processing request", e);
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Processing Error", e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Service is running");
    }
}