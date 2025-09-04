package com.web.saree.reopository;

import com.web.saree.entity.Saree;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SareeRepository extends JpaRepository<Saree, Long > {
}
