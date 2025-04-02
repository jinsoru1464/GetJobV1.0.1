package com.example.GetJobV101.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PortfolioFullResponseDto {
    private Object user;
    private PortfolioResponseDto portfolio;
}
