package com.web.saree.service;

import com.web.saree.dto.response.sareeResponse.AllSareeResponse;
import com.web.saree.entity.Saree;
import com.web.saree.entity.Variant;
import com.web.saree.repository.VariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class VariantService {
    private final VariantRepository variantRepository;

    public List<AllSareeResponse> filterVariants(
            String name,
            String color,
            Double minPrice,    // ✅ changed
            Double maxPrice,    // ✅ changed
            Double discountPercent
    ) {
        // call repository with new min and max prices
        List<Variant> variants = variantRepository.filterVariants(name, color, minPrice, maxPrice, discountPercent);

        Set<AllSareeResponse> sareeResponses = new HashSet<>();
        for (Variant variant : variants) {
            Saree saree = variant.getSaree();

            // ✅ remove all existing variants and add only current variant
            List<Variant> singleVariantList = new ArrayList<>();
            singleVariantList.add(variant);
            saree.setVariants(singleVariantList);

            AllSareeResponse dto = new AllSareeResponse();
            dto.setSarees(saree);
            sareeResponses.add(dto);
        }

        return new ArrayList<>(sareeResponses);
    }

}
