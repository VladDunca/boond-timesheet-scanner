package org.example.boond.model;

import lombok.Data;
import java.util.List;

@Data
public class ScanResult {
    private List<Integer> daysWorked; // 1 for worked day, 0 for non-worked day
    private Integer totalDaysWorked;
    private String documentType; // "PDF" or "XLSX"
    private String processingMethod; // "HOURS_BASED", "BINARY_BASED", or "TOTAL_DAYS"
}