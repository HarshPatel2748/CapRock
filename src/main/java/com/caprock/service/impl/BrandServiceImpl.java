package com.caprock.service.impl;

import com.caprock.dto.BrandProfileRequest;
import com.caprock.dto.BrandProfileResponse;
import com.caprock.model.BrandProfile;
import com.caprock.model.User;
import com.caprock.repository.BrandProfileRepository;
import com.caprock.repository.UserRepository;
import com.caprock.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class BrandServiceImpl implements BrandService {

    @Autowired
    private BrandProfileRepository brandProfileRepository;

    @Autowired
    private UserRepository userRepository;

    //Helper
    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"
                ));
    }

    //Helper
    private void enforcePlanLimit(User user) {
        switch (user.getPlan()) {
            case "free" -> throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Brand profiles are not available on the free plan. Please upgrade."
            );

            case "starter" -> {
                int count = brandProfileRepository.countByUserId(user.getId());
                if (count >= 3) {
                    throw new ResponseStatusException(
                            HttpStatus.FORBIDDEN, "Starter plan allows a maximum of 3 brand profiles. Please upgrade to Pro."
                    );
                }
            }

            //Pro plan - unlimited, no check needed
        }
    }

    //Get all brands
    @Override
    public List<BrandProfileResponse> getAllBrands(String userEmail) {
        User user = getUser(userEmail);
        return brandProfileRepository.findByUserId(user.getId())
                .stream()
                .map(BrandProfileResponse::new)
                .toList();
    }

    //Create Brand
    @Override
    public BrandProfileResponse createdBrand(BrandProfileRequest request, String userEmail) {
        User user = getUser(userEmail);

        //Check plan limits
        enforcePlanLimit(user);

        BrandProfile profile = BrandProfile.builder()
                .user(user)
                .name(request.getName())
                .industry(request.getIndustry())
                .vibe(request.getVibe())
                .audience(request.getAudience())
                .hashtags(request.getHashtags())
                .avoid(request.getAvoid())
                .build();

        brandProfileRepository.save(profile);

        return new BrandProfileResponse(profile);
    }

    //Update Brand
    @Override
    public BrandProfileResponse updateBrand(Long brandId, BrandProfileRequest request, String userEmail) {
        User user = getUser(userEmail);

        //Check if brand exists
        BrandProfile profile = brandProfileRepository.findById(brandId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Brand profile not found"));

        //Make sure this profile belongs to the logged-in user
        if (!profile.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You do not have permission to update this profile");
        }

        profile.setName(request.getName());
        profile.setIndustry(request.getIndustry());
        profile.setVibe(request.getVibe());
        profile.setAudience(request.getAudience());
        profile.setHashtags(request.getHashtags());
        profile.setAvoid(request.getAvoid());

        brandProfileRepository.save(profile);

        return new BrandProfileResponse(profile);
    }

    //Delete Brand
    @Override
    public void deleteBrand(Long brandId, String userEmail) {
        User user = getUser(userEmail);

        BrandProfile profile = brandProfileRepository.findById(brandId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Brand profile not found"));

        //Make sure this profile belongs to the logged-in user
        if (!profile.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You do not have permission to delete this profile");
        }

        brandProfileRepository.delete(profile);
    }
}
