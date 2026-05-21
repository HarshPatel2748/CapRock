package com.caprock.dto;

import com.caprock.model.User;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class AdminUserResponse {

    private Long id;
    private String name;
    private String email;
    private String plan;
    private Integer credits;
    private OffsetDateTime createdAt;

    public AdminUserResponse(User user){
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.plan = user.getPlan();
        this.credits = user.getCredits();
        this.createdAt = user.getCreatedAt();
    }
}
