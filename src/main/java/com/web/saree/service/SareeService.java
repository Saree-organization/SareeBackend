package com.web.saree.service;

import com.cloudinary.Cloudinary;
import com.web.saree.dto.request.SareeRequest.SareeRequest;
import com.web.saree.dto.request.SareeRequest.VariantRequest;
import com.web.saree.entity.Saree;
import com.web.saree.entity.Variant;
import com.web.saree.reopository.SareeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SareeService {

    private final Cloudinary cloudinary;
    private final SareeRepository sareeRepo;

    public ResponseEntity<?> addSaree(SareeRequest sareeRequest) {
        try {
            Saree saree = new Saree ();
            saree.setFabrics (sareeRequest.getFabrics ());
            saree.setDesign (sareeRequest.getDesign ());
            saree.setLength (sareeRequest.getLength ());
            saree.setDescription (sareeRequest.getDescription ());
            saree.setBorder (sareeRequest.getBorder ());
            saree.setCategory (sareeRequest.getCategory ());
            saree.setWeight (sareeRequest.getWeight ());

            List<Variant> variants = new ArrayList<> ();
            for (VariantRequest vr : sareeRequest.getVariants()) {
                Variant variant = new Variant();
                variant.setSkuCode(vr.getSkuCode());
                variant.setName(vr.getName());
                variant.setColor(vr.getColor());
                variant.setSalesPrice(vr.getSalesPrice());
                variant.setCostPrice(vr.getCostPrice());
                variant.setDiscountPercent(vr.getDiscountPercent());
                variant.setStock(vr.getStock());

                // Already uploaded → just save URLs
                variant.setImages(vr.getImageUrls());
                variant.setVideos(vr.getVideoUrl());

                variant.setSaree(saree);
                variants.add(variant);
            }


            saree.setVariants (variants);
            sareeRepo.save (saree);

            return ResponseEntity.ok ("Saree saved successfully!");
        } catch (Exception e) {
            e.printStackTrace ();
            return ResponseEntity.status (500).body ("Error: " + e.getMessage ());
        }
    }


}
