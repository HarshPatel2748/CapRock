package com.caprock.repository;

import com.caprock.model.BrandProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BrandProfileRepository extends JpaRepository<BrandProfile, Long> {

    List<BrandProfile> findByUserId(Long userId);
    int countByUserId(Long userId);
}
