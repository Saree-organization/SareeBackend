package com.web.saree.repository;

import com.web.saree.entity.Saree;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SareeRepository extends JpaRepository<Saree, Long> {
    @Query("""
            select s from Saree s 
            where (:fabrics is null or s.fabrics = :fabrics)
              and (:design is null or s.design = :design)
              and (:weight is null or s.weight = :weight)
              and (:category is null or s.category = :category)
            """)
    List<Saree> filterSarees(@Param("fabrics") String fabrics,
                             @Param("design") String design,
                             @Param("weight") Double weight,
                             @Param("category") String category);

}
