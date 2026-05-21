package com.caprock.service;

import com.caprock.dto.LoginRequest;
import com.caprock.dto.LoginResponse;
import com.caprock.dto.RegisterRequest;

public interface AuthService {

    LoginResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    LoginResponse me(String email, String role);
}
