package com.caprock.service;

import com.caprock.dto.AdminUserResponse;

import java.util.List;
import java.util.Map;

public interface AdminService {

    //Total users, total credits used, total revenue
    Map<String, Object> getStats();

    //List of all users
    List<AdminUserResponse> getAllUsers();

    //Manually update a user's plan or credits
    AdminUserResponse updateUser(Long userId, String plan, Integer credits);
}
