package com.caprock.controller;

import com.caprock.dto.LoginRequest;
import com.caprock.dto.LoginResponse;
import com.caprock.dto.RegisterRequest;
import com.caprock.security.services.UserDetailsImpl;
import com.caprock.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(
            @Valid @RequestBody RegisterRequest request){

        LoginResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request){

        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<LoginResponse> me(
            @AuthenticationPrincipal UserDetailsImpl userDetails){

        LoginResponse response = authService.me(
                userDetails.getUsername(), userDetails.getRole());
        return ResponseEntity.ok(response);
    }
}
