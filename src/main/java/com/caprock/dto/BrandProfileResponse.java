package com.caprock.dto;

import com.caprock.model.BrandProfile;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class BrandProfileResponse {

    private Long id;
    private String name;
    private String industry;
    private String vibe;
    private String audience;
    private String hashtags;
    private String avoid;
    private OffsetDateTime createdAt;

    public BrandProfileResponse(BrandProfile profile) {
        this.id = profile.getId();
        this.name = profile.getName();
        this.industry = profile.getIndustry();
        this.vibe = profile.getVibe();
        this.audience = profile.getAudience();
        this.hashtags = profile.getHashtags();
        this.avoid = profile.getAvoid();
        this.createdAt = profile.getCreatedAt();
    }
}
