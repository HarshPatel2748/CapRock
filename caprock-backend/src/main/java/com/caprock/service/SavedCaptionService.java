package com.caprock.service;

import com.caprock.dto.SavedCaptionRequest;
import com.caprock.dto.SavedCaptionResponse;

import java.util.List;

public interface SavedCaptionService {

    List<SavedCaptionResponse> getAllSaved(String userEmail);

    SavedCaptionResponse saveCaption(SavedCaptionRequest request, String userEmail);

    void deleteCaption(Long captionId, String userEmail);
}
