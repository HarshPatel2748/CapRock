package com.caprock.dto;

import com.caprock.model.SavedCaption;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class SavedCaptionResponse {

    private Long id;
    private String captionText;
    private String hashtags;
    private String style;
    private String platform;
    private OffsetDateTime createdAt;

    public SavedCaptionResponse(SavedCaption caption){
        this.id = caption.getId();
        this.captionText = caption.getCaptionText();
        this.hashtags =  caption.getHashtags();
        this.style = caption.getStyle();
        this.platform = caption.getPlatform();
        this.createdAt = caption.getCreatedAt();
    }
}
