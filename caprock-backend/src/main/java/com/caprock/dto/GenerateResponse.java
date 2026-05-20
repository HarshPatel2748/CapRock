package com.caprock.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GenerateResponse {

    private String rawText;
    private Integer creditsLeft;
}
