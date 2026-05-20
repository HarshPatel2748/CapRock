package com.caprock.service;

import com.caprock.model.BrandProfile;

public interface GeminiService {

    String generateCaptions(String imageBase64,
                            String platform,
                            BrandProfile brandProfile,
                            String keyDetail,
                            String styleNote);
}
