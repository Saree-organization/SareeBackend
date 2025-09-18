package com.web.saree.service;

import com.web.saree.entity.Saree;
import com.web.saree.entity.Users;
import com.web.saree.entity.Wishlist;
import com.web.saree.repository.SareeRepository;
import com.web.saree.repository.UserRepository;
import com.web.saree.repository.WishlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SareeRepository sareeRepository;

    public void addToWishlist(String userEmail, Long sareeId) {
        Users user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Saree saree = sareeRepository.findById(sareeId)
                .orElseThrow(() -> new RuntimeException("Saree not found"));

        if (wishlistRepository.findByUserEmailAndSareeId(userEmail, sareeId).isPresent()) {
            throw new RuntimeException("Saree is already in the wishlist");
        }

        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        wishlist.setSaree(saree);
        wishlistRepository.save(wishlist);
    }

    public void removeFromWishlist(String userEmail, Long sareeId) {
        Users user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Wishlist wishlist = wishlistRepository.findByUserIdAndSareeId(user.getId(), sareeId)
                .orElseThrow(() -> new RuntimeException("Saree not found in wishlist"));

        wishlistRepository.delete(wishlist);
    }

    // Is method mein badlav kiya gaya hai
    public List<Wishlist> getWishlistItems(String userEmail) {
        Users user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return wishlistRepository.findByUserId(user.getId());
    }

    public boolean isSareeInWishlist(String userEmail, Long sareeId) {
        return wishlistRepository.findByUserEmailAndSareeId(userEmail, sareeId).isPresent();
    }
}