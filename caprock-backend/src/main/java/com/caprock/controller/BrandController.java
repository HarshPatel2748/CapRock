package com.caprock.controller;

import com.caprock.dto.BrandProfileRequest;
import com.caprock.dto.BrandProfileResponse;
import com.caprock.security.services.UserDetailsImpl;
import com.caprock.service.BrandService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/brands")
public class BrandController {

    @Autowired
    private BrandService brandService;

    @GetMapping
    public ResponseEntity<List<BrandProfileResponse>> getAllBrands(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        return ResponseEntity.ok(
                brandService.getAllBrands(userDetails.getUsername())
        );
    }

    @PostMapping
    public ResponseEntity<BrandProfileResponse> createBrand(
            @Valid @RequestBody BrandProfileRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {

        return ResponseEntity.status(HttpStatus.CREATED).body(
                brandService.createdBrand(request, userDetails.getUsername())
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<BrandProfileResponse> updateBrand(
            @PathVariable Long id,
            @Valid @RequestBody BrandProfileRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {

        return ResponseEntity.ok(
                brandService.updateBrand(id, request, userDetails.getUsername())
        );
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBrand(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ){

        brandService.deleteBrand(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
