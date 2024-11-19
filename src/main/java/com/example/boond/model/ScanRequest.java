package com.example.boond.model;

import lombok.Data;

@Data
public class ScanRequest {
    private String timesheetId;
    private String documentName;
    private String username;
    private String password;
}