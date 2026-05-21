package com.caprock.controller;

import com.caprock.dto.AdminUserResponse;
import com.caprock.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(){
        return ResponseEntity.ok(adminService.getStats());
    }

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserResponse>> getAllUsers(){
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PatchMapping("/users/{id}")
    public ResponseEntity<AdminUserResponse> updateUser(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body
    ){

        String plan = (String) body.get("plan");
        Integer credits = body.get("credits") != null
                ? ((Number) body.get("credits")).intValue()
                : null;

        return ResponseEntity.ok(adminService.updateUser(id, plan, credits));
    }
}
