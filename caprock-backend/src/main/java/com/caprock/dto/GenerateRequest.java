package com.caprock.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GenerateRequest {

    @NotBlank(message = "Image data is required")
    private String imageBase64;

    @NotBlank(message = "Platform is required")
    private String platform;

    private Long brandProfileId;

    private String keyDetail;

    private String styleNote;
}
