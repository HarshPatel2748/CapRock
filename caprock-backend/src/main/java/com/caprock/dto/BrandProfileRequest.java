package com.caprock.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BrandProfileRequest {

    @NotBlank(message = "Brand name is required")
    @Size(max = 100, message = "Brand name must be under 100 characters")
    private String name;

    @Size(max = 100, message = "Industry must be under 100 characters")
    private String industry;

    private String vibe;

    private String audience;

    private String hashtags;

    private String avoid;
}
