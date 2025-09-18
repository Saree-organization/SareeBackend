package com.web.saree.repository;

import com.web.saree.entity.Variant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VariantRepository extends JpaRepository<Variant, Long> {

    // You can add custom query methods here if needed, for example:

    // Find all variants for a specific saree ID
    List<Variant> findBySareeId(Long sareeId);

    // Find a variant by its SKU code
    Optional<Variant> findBySkuCode(String skuCode);

    // Find variants by color and saree ID
    List<Variant> findByColorAndSareeId(String color, Long sareeId);


    @Query("""
       SELECT v FROM Variant v 
       WHERE (:name IS NULL OR v.name LIKE %:name%)
         AND (:color IS NULL OR v.color = :color)
         AND (:minPrice IS NULL OR v.salesPrice >= :minPrice)
         AND (:maxPrice IS NULL OR v.salesPrice <= :maxPrice)
         AND (:discountPercent IS NULL OR v.discountPercent >= :discountPercent)
       """)
    List<Variant> filterVariants(
            @Param("name") String name,
            @Param("color") String color,
            @Param("minPrice") Double minPrice,     // ✅ added
            @Param("maxPrice") Double maxPrice,     // ✅ added
            @Param("discountPercent") Double discountPercent
    );

}

