package com.caprock.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SavedCaptionRequest {

    @NotBlank(message = "Caption txt is required")
    private String captionText;

    private String hashtags;

    @NotBlank(message = "Style is required")
    private String style;

    @NotBlank(message = "Platform is required")
    private String platform;
}
