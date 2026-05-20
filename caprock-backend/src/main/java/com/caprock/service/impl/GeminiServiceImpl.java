package com.caprock.service.impl;


import com.caprock.model.BrandProfile;
import com.caprock.service.GeminiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Service
public class GeminiServiceImpl implements GeminiService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.model}")
    private String geminiModel;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    public String generateCaptions(String imageBase64,
                                   String platform,
                                   BrandProfile brandProfile,
                                   String keyDetail,
                                   String styleNote){

        String prompt = buildPrompt(platform, brandProfile, keyDetail, styleNote);
        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + geminiModel + ":generateContent?key=" + geminiApiKey;


        //Build the request body
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                //Image part
                                Map.of("inline_data", Map.of(
                                        "mime_type", "image/jpeg",
                                        "data", imageBase64
                                )),
                                //Text prompt part
                                Map.of("text", prompt)
                        ))
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        //Parse the response and extract the text
        return extractText(response.getBody());
    }

    //Prompt Builder
    private String buildPrompt(String platform,
                               BrandProfile brandProfile,
                               String keyDetail,
                               String styleNote){

        StringBuilder prompt = new StringBuilder();

        prompt.append("You are a social media caption expert. ")
                .append("Analyze this image and generate 3 caption styles for ")
                .append(platform).append(".\n\n");

        //Platform-specific rules
        prompt.append("PLATFORM RULES for ").append(platform).append(":\n");

        switch (platform){
            case "Instagram" -> prompt.append(
                    "- Long, engaging captions are fine\n" +
                            "- Use emojis naturally\n" +
                            "- Include 10-15 relevant hashtags\n"
            );
            case "X (Twitter)" -> prompt.append(
                    "- Maximum 280 characters for the caption (excluding hashtags)\n" +
                            "- Keep it punchy anddirect\n" +
                            "- Use only 2-3 hashtags\n"
            );
            case "LinkedIn" -> prompt.append(
                    "- Professional tone\n" +
                            "- Minimal or no emojis\n" +
                            "- 3-5 professional hashtags\n" +
                            "- Can be longer and story-driven\n"
            );
        }

        //Brand profile rules if provided
        if(brandProfile != null){
            prompt.append("\nBRAND PROFILE:\n");
            prompt.append("Brand name: ").append(brandProfile.getName()).append("\n");

            if(brandProfile.getIndustry() != null)
                prompt.append("Industry: ").append(brandProfile.getIndustry()).append("\n");
            if(brandProfile.getVibe() != null)
                prompt.append("Vibe: ").append(brandProfile.getVibe()).append("\n");
            if(brandProfile.getAudience() != null)
                prompt.append("Target audience: ").append(brandProfile.getAudience()).append("\n");
            if(brandProfile.getHashtags() != null)
                prompt.append("Always include these hashtags: ").append(brandProfile.getHashtags()).append("\n");
            if(brandProfile.getAvoid() != null)
                prompt.append("NEVER use: ").append(brandProfile.getAvoid()).append("\n");
        }

        //Optional user hints
        if(keyDetail != null && !keyDetail.isBlank())
            prompt.append("\nKEY DETAIL: ").append(keyDetail).append("\n");
        if(styleNote != null && !styleNote.isBlank())
            prompt.append("STYLE NOTE: ").append(styleNote).append("\n");

        //Output format instructions
        prompt.append(
                """
                        Generate exactly 3 captions using this format - no extra text, no explanations:
                        
                        [PROFESSIONAL]
                        <caption text here>
                        <hashtags here>
                        
                        [WITTY]
                        <caption text here>
                        <hashtags here>
                        
                        [MINIMALIST]
                        <caption text here>
                        <hashtags here>
                        """
        );

        return prompt.toString();
    }

    //Extract text from Gemini response
    private String extractText(String responseBody){
        try{
            JsonNode root = objectMapper.readTree(responseBody);
            return root.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();
        }catch (Exception e){
            throw new RuntimeException("Failed to parse Gemini response: " + e.getMessage());
        }
    }
}
