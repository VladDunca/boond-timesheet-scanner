package org.example.boond.controller;

import org.example.boond.model.ScanRequest;
import org.example.boond.model.ScanResult;
import org.example.boond.service.BoondScraperService;
import org.example.boond.service.DocumentScannerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.io.File;

@RestController
@RequestMapping("/api/timesheet")
public class TimesheetController {

    private final BoondScraperService scraperService;
    private final DocumentScannerService scannerService;

    public TimesheetController(BoondScraperService scraperService, DocumentScannerService scannerService) {
        this.scraperService = scraperService;
        this.scannerService = scannerService;
    }

    @PostMapping("/scan")
    public ResponseEntity<ScanResult> scanTimesheet(@RequestBody ScanRequest request) {
        try {
            File document = scraperService.downloadTimesheet(request);
            ScanResult result = scannerService.scanDocument(document, request.getDocumentName());
            document.delete(); // Clean up temporary file
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}