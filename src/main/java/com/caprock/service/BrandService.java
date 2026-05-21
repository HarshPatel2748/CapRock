package com.caprock.service;

import com.caprock.dto.BrandProfileRequest;
import com.caprock.dto.BrandProfileResponse;

import java.util.List;

public interface BrandService {

    List<BrandProfileResponse> getAllBrands(String userEmail);

    BrandProfileResponse createdBrand(BrandProfileRequest request, String userEmail);

    BrandProfileResponse updateBrand(Long brandId, BrandProfileRequest request, String userEmail);

    void deleteBrand(Long brandId, String userEmail);
}
