package com.example.GetJobV101.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class PortfolioResponseDto {

    private Long id;
    private String title;
    private String subject;
    private String startDate;
    private String endDate;
    private String teamSize;
    private String skills;
    private String role;
    private List<String> descriptions;
    private List<String> imagePaths;


    public PortfolioResponseDto(Long id, String title, String subject, String startDate, String endDate,
                                String teamSize, String skills, String role,
                                List<String> descriptions, List<String> imagePaths) {
        this.id = id;
        this.title = title;
        this.subject = subject;
        this.startDate = startDate;
        this.endDate = endDate;
        this.teamSize = teamSize;
        this.skills = skills;
        this.role = role;
        this.descriptions = descriptions;
        this.imagePaths = imagePaths;

    }


}
