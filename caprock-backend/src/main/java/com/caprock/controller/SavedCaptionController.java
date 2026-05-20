package com.caprock.controller;

import com.caprock.dto.SavedCaptionRequest;
import com.caprock.dto.SavedCaptionResponse;
import com.caprock.security.services.UserDetailsImpl;
import com.caprock.service.SavedCaptionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/saved")
public class SavedCaptionController {

    @Autowired
    private SavedCaptionService savedCaptionService;

    @GetMapping
    public ResponseEntity<List<SavedCaptionResponse>> getAllSaved(
            @AuthenticationPrincipal UserDetailsImpl userDetails
            ){
        return ResponseEntity.ok(savedCaptionService.getAllSaved(userDetails.getUsername()));
    }

    @PostMapping
    public ResponseEntity<SavedCaptionResponse> saveCaption(
            @Valid @RequestBody SavedCaptionRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
            ){

        return ResponseEntity.status(HttpStatus.CREATED).body(
                savedCaptionService.saveCaption(request, userDetails.getUsername())
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCaption(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ){

        savedCaptionService.deleteCaption(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
