package com.caprock.repository;

import com.caprock.model.SavedCaption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SavedCaptionRepository extends JpaRepository<SavedCaption, Long> {

    List<SavedCaption> findByUserIdOrderByCreatedAtDesc(Long userId);
}
