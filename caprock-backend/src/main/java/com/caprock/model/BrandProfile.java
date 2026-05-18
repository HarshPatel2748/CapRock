package com.caprock.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "brand_profiles", indexes = {
        @Index(name = "idx_brand_profiles_user_id", columnList = "user_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrandProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String industry;

    @Column(columnDefinition = "TEXT")
    private String vibe;

    @Column(columnDefinition = "TEXT")
    private String audience;

    @Column(columnDefinition = "TEXT")
    private String hashtags;

    @Column(columnDefinition = "TEXT")
    private String avoid;

    @Column(name = "create_at", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
