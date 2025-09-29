package com.web.saree.repository;

import com.web.saree.entity.Variant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.List;


@Repository
public interface VariantRepository extends JpaRepository<Variant, Long> {

    List<Variant> findBySareeId(Long sareeId);
    Variant findTop1ByDiscountPercentGreaterThanAndDiscountPercentLessThanOrderByDiscountPercentDesc(
            Double greater,
            Double lesser
    );


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

    List<Variant> findTop10ByVideosIsNotNull();

    @Query(" SELECT v FROM Variant v WHERE v.id IN ( SELECT MIN(v2.id) FROM Variant v2  GROUP BY v2.color ) ")
    List<Variant> findOneVariantPerColor(Pageable pageable);

}