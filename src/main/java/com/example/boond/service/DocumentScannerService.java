package com.example.boond.service;

import com.example.boond.model.ScanResult;
import net.sourceforge.tess4j.Tesseract;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentScannerService {

    private final Tesseract tesseract;

    public DocumentScannerService() {
        tesseract = new Tesseract();
        tesseract.setDatapath("C:\\Program Files\\Tesseract-OCR\\tessdata"); // Adjust path based on your environment
    }

    public ScanResult scanDocument(File file, String fileName) throws Exception {
        if (fileName.toLowerCase().endsWith(".pdf")) {
            return scanPdfDocument(file);
        } else if (fileName.toLowerCase().endsWith(".xlsx")) {
            return scanExcelDocument(file);
        }
        throw new IllegalArgumentException("Unsupported file format");
    }

    private ScanResult scanPdfDocument(File pdfFile) throws Exception {
        PDDocument document = PDDocument.load(pdfFile);
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        BufferedImage image = pdfRenderer.renderImageWithDPI(0, 300);
        String result = tesseract.doOCR(image);
        document.close();

        return processExtractedText(result);
    }

    private ScanResult scanExcelDocument(File excelFile) throws Exception {
        Workbook workbook = WorkbookFactory.create(excelFile);
        Sheet sheet = workbook.getSheetAt(0);
        List<String> cellValues = new ArrayList<>();

        for (Row row : sheet) {
            for (Cell cell : row) {
                cellValues.add(getCellValueAsString(cell));
            }
        }
        workbook.close();

        return processExcelData(cellValues);
    }

    private String getCellValueAsString(Cell cell) {
        return switch (cell.getCellType()) {
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case STRING -> cell.getStringCellValue();
            default -> "";
        };
    }

    private ScanResult processExtractedText(String text) {
        ScanResult result = new ScanResult();
        List<Integer> daysWorked = new ArrayList<>();

        // Simple heuristic for processing text
        String[] lines = text.split("\n");
        int totalDays = 0;

        for (String line : lines) {
            line = line.trim().toLowerCase();
            if (line.contains("hours") || line.contains("h")) {
                double hours = extractHours(line);
                daysWorked.add(hours >= 8 ? 1 : 0);
                if (hours >= 8) totalDays++;
            } else if (line.matches(".*\\b[01]\\b.*")) {
                int day = line.contains("1") ? 1 : 0;
                daysWorked.add(day);
                if (day == 1) totalDays++;
            }
        }

        result.setDaysWorked(daysWorked);
        result.setTotalDaysWorked(totalDays);
        result.setDocumentType("PDF");
        result.setProcessingMethod(daysWorked.isEmpty() ? "TOTAL_DAYS" : "BINARY_BASED");

        return result;
    }

    private ScanResult processExcelData(List<String> cellValues) {
        ScanResult result = new ScanResult();
        List<Integer> daysWorked = new ArrayList<>();
        int totalDays = 0;

        for (String value : cellValues) {
            if (value.matches(".*\\d+.*")) {
                double numValue = Double.parseDouble(value.replaceAll("[^\\d.]", ""));
                if (value.toLowerCase().contains("h")) {
                    int day = numValue >= 8 ? 1 : 0;
                    daysWorked.add(day);
                    if (day == 1) totalDays++;
                } else {
                    int day = numValue >= 1 ? 1 : 0;
                    daysWorked.add(day);
                    if (day == 1) totalDays++;
                }
            }
        }

        result.setDaysWorked(daysWorked);
        result.setTotalDaysWorked(totalDays);
        result.setDocumentType("XLSX");
        result.setProcessingMethod("HOURS_BASED");

        return result;
    }

    private double extractHours(String text) {
        try {
            return Double.parseDouble(text.replaceAll("[^\\d.]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}