package com.web.saree.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.web.saree.dto.request.SareeRequest.SareeRequest;
import com.web.saree.dto.request.SareeRequest.VariantRequest;
import com.web.saree.service.SareeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sarees")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class SareeController {
    private final Cloudinary cloudinary;
    private final SareeService sareeService;
    private SareeRequest sareeRequest = new SareeRequest();

    @PostMapping("/addSareeDetails")
    public ResponseEntity<?> addSareeDetails(@RequestBody Map<String, Object> data) {
        try {
            sareeRequest.setFabrics((String) data.get("fabrics"));
            sareeRequest.setDesign((String) data.get("design"));
            sareeRequest.setLength(Double.parseDouble(data.get("length").toString()));
            sareeRequest.setDescription((String) data.get("description"));
            sareeRequest.setBorder((String) data.get("border"));
            sareeRequest.setCategory((String) data.get("category"));
            sareeRequest.setWeight(Double.parseDouble(data.get("weight").toString()));
            System.out.println ("Step 1: SareeRequest object created");
            return ResponseEntity.ok("Step 1 saved");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error in step 1");
        }
    }

    @PostMapping(value = "/addVariant", consumes = "multipart/form-data")
    public ResponseEntity<?> addVariant(
            @RequestParam("skuCode") String skuCode,
            @RequestParam("name") String name,
            @RequestParam("color") String color,
            @RequestParam("salesPrice") String salesPrice,
            @RequestParam("costPrice") String costPrice,
            @RequestParam("discountPercent") String discountPercent,
            @RequestParam("stock") String stock,
            @RequestParam(value = "images", required = false) MultipartFile[] images,
            @RequestParam(value = "videos", required = false) MultipartFile[] videos
    ) {
        try {
            VariantRequest variant = new VariantRequest();
            variant.setSkuCode(skuCode);
            variant.setName(name);
            variant.setColor(color);
            variant.setSalesPrice(Double.parseDouble(salesPrice));
            variant.setCostPrice(Double.parseDouble(costPrice));
            variant.setDiscountPercent(Double.parseDouble(discountPercent));
            variant.setStock(Integer.parseInt(stock));

            // Upload Images immediately
            if (images != null && images.length > 0) {
                List<String> imageUrls = new ArrayList<> ();
                for (MultipartFile image : images) {
                    Map uploadResult = cloudinary.uploader().upload(
                            image.getBytes(),
                            ObjectUtils.asMap("folder", "Saree/Images")
                    );

                    imageUrls.add(uploadResult.get("url").toString());
                }
                variant.setImageUrls(imageUrls); // <-- store URLs, not MultipartFile
            }

            // Upload Video (only 1 allowed as per your rule)
            if (videos != null && videos.length > 0) {
                Map uploadResult = cloudinary.uploader().upload(
                        videos[0].getBytes(),
                        ObjectUtils.asMap("folder", "Saree/Videos", "resource_type", "video")
                );

                variant.setVideoUrl(uploadResult.get("url").toString()); // <-- single video URL
            }

            sareeRequest.getVariants().add(variant);
            System.out.println("Step 2: Variant object created with uploaded files");
            return ResponseEntity.ok("Variant added successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error adding variant: " + e.getMessage());
        }
    }


    @PostMapping("/addSaree")
    public ResponseEntity<?> finalSave() {
        try {
            return sareeService.addSaree(sareeRequest);
        } finally {
            sareeRequest = new SareeRequest(); // reset after save
        }
    }
}
