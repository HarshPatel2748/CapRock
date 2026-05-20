package com.caprock.controller;

import com.caprock.dto.GenerateRequest;
import com.caprock.dto.GenerateResponse;
import com.caprock.model.BrandProfile;
import com.caprock.model.User;
import com.caprock.repository.BrandProfileRepository;
import com.caprock.repository.UserRepository;
import com.caprock.security.services.UserDetailsImpl;
import com.caprock.service.CreditService;
import com.caprock.service.GeminiService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class CaptionController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BrandProfileRepository brandProfileRepository;

    @Autowired
    private CreditService creditService;

    @Autowired
    private GeminiService geminiService;


    @PostMapping("/generate")
    public ResponseEntity<GenerateResponse> generate(
            @Valid @RequestBody GenerateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
            ){

        //Load user
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"
                ));

        //Step 1 -> check credits before doing anything
        creditService.checkCredits(user);

        //Step 2 -> load brand profile if provided
        BrandProfile brandProfile = null;
        if(request.getBrandProfileId() != null){
            brandProfile = brandProfileRepository
                    .findById(request.getBrandProfileId())
                    .orElse(null);
        }

        //Step 3 -> call Gemini
        String rawText = geminiService.generateCaptions(
                request.getImageBase64(),
                request.getPlatform(),
                brandProfile,
                request.getKeyDetail(),
                request.getStyleNote());

        //Step 4 -> deduct credit only after successful generation
        creditService.deductCredit(user);

        return ResponseEntity.ok(
                new GenerateResponse(rawText, user.getCredits())
        );
    }
}
