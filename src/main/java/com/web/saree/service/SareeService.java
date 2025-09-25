package com.web.saree.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.web.saree.dto.request.SareeRequest.SareeRequest;
import com.web.saree.dto.request.SareeRequest.VariantRequest;
import com.web.saree.dto.response.sareeResponse.AllSareeResponse;
import com.web.saree.dto.response.sareeResponse.SareeResponse;
import com.web.saree.entity.Saree;
import com.web.saree.entity.Variant;
import com.web.saree.repository.SareeRepository;
import com.web.saree.repository.VariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SareeService {

    private final Cloudinary cloudinary;
    private final SareeRepository sareeRepo;
    private final VariantRepository variantRepo;

    public ResponseEntity<?> addVariant( SareeRequest sareeRequest,String skuCode, String name, String color, String salesPrice, String costPrice, String discountPercent, String stock, MultipartFile[] images, MultipartFile[] videos) {
        try {
            VariantRequest variant = new VariantRequest();
            variant.setSkuCode(skuCode);
            variant.setName(name);
            variant.setColor(color);

            variant.setSalesPrice(Double.parseDouble(salesPrice));
            variant.setCostPrice(Double.parseDouble(costPrice));
            variant.setDiscountPercent(Double.parseDouble(discountPercent));
            variant.setPriceAfterDiscount(variant.getSalesPrice() - (variant.getSalesPrice() * (variant.getDiscountPercent() / 100)));

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
            return ResponseEntity.ok("Variant added successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error adding variant: " + e.getMessage());
        }
    }


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
                variant.setPriceAfterDiscount(vr.getPriceAfterDiscount());
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


    public List<AllSareeResponse> getAllSarees() {
        List<Saree> sarees = sareeRepo.findAll ();
        List<AllSareeResponse> sareeResponses = new ArrayList<> ();
        for (Saree saree : sarees) {
            AllSareeResponse sareeResponse = new AllSareeResponse ();
            sareeResponse.setSarees (saree);
            sareeResponses.add (sareeResponse);
        }
        return sareeResponses;
    }


    public SareeResponse getSareeById(Long id) {
        Saree saree = sareeRepo.findById (id).orElse (null);
        if (saree == null) {
            return null;
        }
        SareeResponse sareeResponse = new SareeResponse ();
        sareeResponse.setSarees (saree);
        return sareeResponse;
    }


    public List<AllSareeResponse> filterSarees(String fabrics, String category, String color,
                                               Double minPrice, Double maxPrice) {

        // 1. Get sarees by fabrics & category
        List<Saree> sarees = sareeRepo.findByFabricsAndCategory(fabrics, category);


        // 2. Get filtered variants
        List<Variant> filteredVariants = variantRepo.findFilteredVariants(fabrics, category, color, minPrice, maxPrice);


        // 3. Create final DTO list
        List<AllSareeResponse> responses  = new ArrayList<>();

        for (Variant variant : filteredVariants) {
            Saree saree = variant.getSaree();

            // show only this variant
            saree.setVariants(List.of(variant));

            AllSareeResponse dto = new AllSareeResponse();
            dto.setSarees(saree);
            responses.add(dto);
        }

        return responses;
    }

}
