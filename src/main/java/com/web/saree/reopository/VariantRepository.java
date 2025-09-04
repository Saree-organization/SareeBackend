package com.web.saree.reopository;

import com.web.saree.entity.Variant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VariantRepository extends JpaRepository<Variant, Long> {
}
