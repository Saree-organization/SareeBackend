package com.web.saree.controller;

 // मान लें कि आपके पास यह सर्विस है
import com.web.saree.dto.response.ShippingAddressResponse;
import com.web.saree.entity.ShippingAddress;
import com.web.saree.entity.Users;
import com.web.saree.service.ShippingAddressService;
import com.web.saree.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/addresses")
public class ShippingAddressController {

    @Autowired
    private ShippingAddressService addressService;

    @Autowired
    private UserService userService; // वर्तमान लॉगिन किए गए User को प्राप्त करने के लिए

    /**
     * GET /api/user/addresses
     * React: fetchAddresses()
     */
    @GetMapping
    public ResponseEntity<?> getAllUserAddresses(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "User not authenticated."));
        }

        String email = principal.getName();
        Users currentUser = userService.findByEmail(email);

        // Service से DTOs की लिस्ट प्राप्त करें
        List<ShippingAddressResponse> addresses = addressService.getAddressesByUser(currentUser);
        return ResponseEntity.ok(addresses);
    }

    /**
     * POST /api/user/addresses
     * React: handleSaveNewAddress()
     */
    @PostMapping
    public ResponseEntity<ShippingAddress> saveNewAddress(@RequestBody ShippingAddress addressRequest ,Principal principal) {
        String email=principal.getName();
        Users currentUser=userService.findByEmail(email);
        ShippingAddress savedAddress = addressService.saveNewAddress(addressRequest, currentUser);

        // HTTP 201 Created स्टेटस के साथ सेव किया गया एड्रेस लौटाएं
        return ResponseEntity.status(201).body(savedAddress);
    }
}