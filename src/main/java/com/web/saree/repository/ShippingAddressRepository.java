package com.web.saree.repository;

import com.web.saree.entity.ShippingAddress;
import com.web.saree.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShippingAddressRepository extends JpaRepository<ShippingAddress, Long> {

    // JWT से प्राप्त User के सभी एड्रेस खोजता है
    List<ShippingAddress> findByUser(Users user);

    // सुरक्षा जांच के लिए: एड्रेस ID और User दोनों से एड्रेस खोजता है
    ShippingAddress findByIdAndUser(Long id, Users user);
}