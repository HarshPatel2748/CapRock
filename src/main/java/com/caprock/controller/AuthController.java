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
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

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

    @PostMapping("/verify-email")
    public ResponseEntity<LoginResponse> verifyEmail(
            @RequestBody Map<String, String> body
    ){
        String email = body.get("email");
        String code = body.get("code");

        if(email == null || code == null){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Email and code are required."
            );
        }

        LoginResponse response = authService.verifyEmail(email, code);
        return ResponseEntity.ok(response);
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

    @PutMapping("/me")
    public ResponseEntity<?> updateMe(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetailsImpl userDetails
            ){

        String name = body.get("name");
        if(name == null || name.isBlank()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name cannot be empty.");
        }

        authService.updateName(userDetails.getUsername(), name);
        return ResponseEntity.ok(Map.of("message", "Name updated successfully"));
    }
}
