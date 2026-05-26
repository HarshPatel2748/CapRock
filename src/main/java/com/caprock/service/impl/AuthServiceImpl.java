package com.caprock.service.impl;

import com.caprock.dto.LoginRequest;
import com.caprock.dto.LoginResponse;
import com.caprock.dto.RegisterRequest;
import com.caprock.model.User;
import com.caprock.repository.UserRepository;
import com.caprock.security.jwt.JwtUtil;
import com.caprock.service.AuthService;
import com.caprock.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.Random;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    @Override
    public String register(RegisterRequest request) {

        //Check duplicate email
        if(userRepository.existsByEmail(request.getEmail())){
            throw  new ResponseStatusException(
                    HttpStatus.CONFLICT, "Email already registered");
        }

        //Generate 6 digit verification code
        String code = String.format("%06d", new Random().nextInt(999999));

        //Build and save user
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .plan("free")
                .credits(10)
                .verified(false)
                .verificationCode(code)
                .verificationCodeExpiry(OffsetDateTime.now().plusMinutes(10))
                .build();

        userRepository.save(user);

        //Send verification email
        try {
            emailService.sendVerificationEmail(request.getEmail(), request.getName(), code);
        }catch (Exception e){
            System.err.println("Failed to send verification email: " + e.getMessage());
        }

        return "Verification code sent to " + request.getEmail();
    }

    @Override
    public LoginResponse login(LoginRequest request) {

        //Check admin credentials first - no DB lookup
        if(request.getEmail().equals(adminEmail) && request.getPassword().equals(adminPassword)){
            String token = jwtUtil.generateToken(0L, adminEmail, "ADMIN");

            return new LoginResponse(token, 0L, "ADMIN", adminEmail, "ADMIN", "admin", 0);
        }

        //Normal user - find in DB
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        //Verify password
        if(!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())){
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid email or passwword");
        }

        //Block unverified users
        if(!user.getVerified()){
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Please verify your email before logging in."
            );
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), "USER");

        return new LoginResponse(
                token, user.getId(), user.getName(), user.getEmail(), "USER", user.getPlan(), user.getCredits());
    }

    @Override
    public LoginResponse me(String email, String role) {

        //Admin has no DB record
        if(role.equals("ROLE_ADMIN")){
            return new LoginResponse(
                    null, 0L, "Admin", email, "ADMIN", "admin", 0);
        }

        //Fetch fresh user data from DB
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        return new LoginResponse(
                null, user.getId(), user.getName(), user.getEmail(), "USER", user.getPlan(), user.getCredits());
    }

    @Override
    public void updateName(String email, String name){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"
                ));

        user.setName(name.trim());
        userRepository.save(user);
    }

    //Verify email
    @Override
    public LoginResponse verifyEmail(String email, String code){

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"
                ));

        //check if already verified
        if(user.getVerified()){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Email already verified"
            );
        }

        //Check code expiry
        if(user.getVerificationCodeExpiry() == null || OffsetDateTime.now().isAfter(user.getVerificationCodeExpiry())){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Verification code expired. Please register again."
            );
        }

        //Check code match
        if(!user.getVerificationCode().equals(code.trim())){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid verification code."
            );
        }

        //Mark as verified and clear code
        user.setVerified(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiry(null);
        userRepository.save(user);

        //Issue JWT
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), "USER");

        return new LoginResponse(
                token, user.getId(), user.getName(), user.getEmail(), "USER", user.getPlan(), user.getCredits()
        );
    }
}
