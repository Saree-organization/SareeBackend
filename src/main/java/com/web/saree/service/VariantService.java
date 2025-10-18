package com.web.saree.service;

import com.web.saree.dto.response.sareeResponse.AllSareeResponse;
import com.web.saree.entity.Saree;
import com.web.saree.entity.Variant;
import com.web.saree.repository.VariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class VariantService {
    private final VariantRepository variantRepository;


    public ResponseEntity<?> updateVariant(Long sareeId, Long variantId, Map<String, Object> updates) {
        Variant variant = variantRepository.findById(variantId).orElse(null);
        if (variant == null)return ResponseEntity.notFound().build();


        // Safe handling for salesPrice
        if (updates.containsKey("salesPrice")) {
            Object priceObj = updates.get("salesPrice");
            if (priceObj instanceof Number) {
                variant.setSalesPrice(((Number) priceObj).doubleValue());
            }
        }

        // Safe handling for discountPercent
        if (updates.containsKey("discountPercent")) {
            Object discountObj = updates.get("discountPercent");
            if (discountObj instanceof Number) {
                variant.setDiscountPercent(((Number) discountObj).doubleValue());
            }
        }
        variant.setPriceAfterDiscount(
                Double.parseDouble(
                        String.format("%.2f", variant.getSalesPrice() * (1 - variant.getDiscountPercent() / 100.0))
                )
        );


        Variant updatedVariant =  variantRepository.save(variant);
        return ResponseEntity.ok("Variant updated successfully");

    }
}
