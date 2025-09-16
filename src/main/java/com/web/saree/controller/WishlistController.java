package com.web.saree.controller;

import com.web.saree.entity.Saree;
import com.web.saree.entity.Users;
import com.web.saree.entity.Variant;
import com.web.saree.entity.Wishlist;
import com.web.saree.reopository.SareeRepository;
import com.web.saree.reopository.UserRepository;
import com.web.saree.reopository.VariantRepository;
import com.web.saree.reopository.WishlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.web.saree.dto.request.WishlistRequest; // Import the DTO class

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/wishlist")
public class WishlistController {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private UserRepository usersRepository;

    @Autowired
    private SareeRepository sareeRepository;

    @Autowired
    private VariantRepository variantRepository;

    // The DTO class is now in a separate file

    @PostMapping("/add")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> addToWishlist(@RequestBody WishlistRequest request, Authentication authentication) {
        String userEmail = authentication.getName();
        Users user = usersRepository.findByEmail(userEmail).orElse(null);

        if (user == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        Optional<Saree> sareeOptional = sareeRepository.findById(request.getSareeId());
        Optional<Variant> variantOptional = variantRepository.findById(request.getVariantId());

        if (!sareeOptional.isPresent() || !variantOptional.isPresent()) {
            return new ResponseEntity<>("Saree or Variant not found", HttpStatus.NOT_FOUND);
        }

        Saree saree = sareeOptional.get();
        Variant variant = variantOptional.get();

        Optional<Wishlist> existingItem = wishlistRepository.findByUserAndVariant(user, variant);
        if (existingItem.isPresent()) {
            return new ResponseEntity<>("Item already in wishlist", HttpStatus.CONFLICT);
        }

        Wishlist newWishlistItem = new Wishlist();
        newWishlistItem.setUser(user);
        newWishlistItem.setSaree(saree);
        newWishlistItem.setVariant(variant);

        wishlistRepository.save(newWishlistItem);

        return new ResponseEntity<>("Item added to wishlist successfully", HttpStatus.CREATED);
    }
}