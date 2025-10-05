package com.web.saree.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.web.saree.dto.request.SareeRequest.SareeRequest;
import com.web.saree.dto.request.SareeRequest.VariantRequest;
import com.web.saree.dto.response.VariantDto;
import com.web.saree.dto.response.sareeResponse.AllSareeResponse;
import com.web.saree.entity.Saree;
import com.web.saree.entity.Variant;
import com.web.saree.service.SareeService;
import com.web.saree.service.VariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sarees")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")

public class SareeController {
    private final Cloudinary cloudinary;
    private final SareeService sareeService;
    private final VariantService variantService;
    private SareeRequest sareeRequest = new SareeRequest ();

    @PostMapping("/addSareeDetails")
    public ResponseEntity<?> addSareeDetails(@RequestBody Map<String, Object> data) {
        try {
            sareeRequest.setFabrics ((String) data.get ("fabrics"));
            sareeRequest.setDesign ((String) data.get ("design"));
            sareeRequest.setLength (Double.parseDouble (data.get ("length").toString ()));
            sareeRequest.setDescription ((String) data.get ("description"));
            sareeRequest.setBorder ((String) data.get ("border"));
            sareeRequest.setCategory ((String) data.get ("category"));
            sareeRequest.setWeight (Double.parseDouble (data.get ("weight").toString ()));
            return ResponseEntity.ok ("Step 1 saved");
        } catch (Exception e) {
            e.printStackTrace ();
            return ResponseEntity.status (500).body ("Error in step 1");
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
        return sareeService.addVariant (sareeRequest, skuCode, name, color, salesPrice, costPrice, discountPercent, stock, images, videos);
    }


    @PostMapping("/addSaree")
    public ResponseEntity<?> finalSave() {
        try {
            return sareeService.addSaree (sareeRequest);
        } finally {
            sareeRequest = new SareeRequest (); // reset after save
        }
    }

    @GetMapping("/allSarees")
    private ResponseEntity<?> getAllSarees() {
        try {
            return ResponseEntity.ok (sareeService.getAllSarees ());
        } catch (Exception e) {
            e.printStackTrace ();
            return ResponseEntity.status (500).body ("Error in getting all sarees");
        }
    }

    @GetMapping("/latestSarees")
    private ResponseEntity<?> getLatestSarees() {
        try {
            return ResponseEntity.ok (sareeService.getLatestSarees ());
        } catch (Exception e) {
            e.printStackTrace ();
            return ResponseEntity.status (500).body ("Error in getting latest sarees");
        }
    }

    @GetMapping("/{id}")
    private ResponseEntity<?> getSareeById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok (sareeService.getSareeById (id));
        } catch (Exception e) {
            e.printStackTrace ();
            return ResponseEntity.status (500).body ("Error in getting saree by id");
        }
    }

    @GetMapping("/filters")
    public ResponseEntity<?> filterSarees(
            @RequestParam(required = false) String fabrics,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double discount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        // Call service method that returns Page<AllSareeResponse>
        Page<AllSareeResponse> sareePage = sareeService.filterSarees(fabrics, category, color, minPrice, maxPrice, discount, page, size);

        // Prepare response
        Map<String, Object> response = new HashMap<> ();
        response.put("sarees", sareePage.getContent());
        response.put("currentPage", sareePage.getNumber());
        response.put("totalItems", sareePage.getTotalElements());
        response.put("totalPages", sareePage.getTotalPages());

        return ResponseEntity.ok(response);
    }



    @GetMapping("/byDiscount")
    public ResponseEntity<List<VariantDto>> getSareesByDiscount() {
        List<VariantDto> result = sareeService.getByDiscount ();
        return ResponseEntity.ok (result);
    }

    @GetMapping("/byVideo")
    public ResponseEntity<List<VariantDto>> getSareesByVideo() {
        List<VariantDto> result = sareeService.getByVideo ();
        return ResponseEntity.ok (result);
    }

    @GetMapping("/highestSales")
    public ResponseEntity<List<VariantDto>> getHighestSale() {
        List<VariantDto> result = sareeService.getHighestSale ();
        return ResponseEntity.ok (result);
    }

    @GetMapping("/byColor")
    public ResponseEntity<List<VariantDto>> getSareesByColor() {
        List<VariantDto> result = sareeService.getByColor ();
        return ResponseEntity.ok (result);
    }
}
