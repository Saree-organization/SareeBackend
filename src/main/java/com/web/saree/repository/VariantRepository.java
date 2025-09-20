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

    List<Variant> findBySareeId(Long sareeId);

    @Query("""
       SELECT v FROM Variant v
       JOIN v.saree s
       WHERE (:fabrics IS NULL OR s.fabrics = :fabrics)
         AND (:category IS NULL OR s.category = :category)
         AND (:color IS NULL OR v.color = :color)
         AND (:minPrice IS NULL OR v.priceAfterDiscount >= :minPrice)
         AND (:maxPrice IS NULL OR v.priceAfterDiscount <= :maxPrice)
       """)
    List<Variant> findFilteredVariants(@Param("fabrics") String fabrics,
                                       @Param("category") String category,
                                       @Param("color") String color,
                                       @Param("minPrice") Double minPrice,
                                       @Param("maxPrice") Double maxPrice);

}