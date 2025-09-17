package com.web.saree.repository;

import com.web.saree.entity.Variant;
import org.springframework.data.jpa.repository.JpaRepository;
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
}