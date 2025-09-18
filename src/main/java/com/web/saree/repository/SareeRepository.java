package com.web.saree.repository;

import com.web.saree.entity.Saree;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface SareeRepository extends JpaRepository<Saree, Long> {

    List<Saree> findByFabricsAndCategory(String fabrics, String category);
}
