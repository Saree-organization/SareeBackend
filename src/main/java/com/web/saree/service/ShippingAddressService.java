package com.web.saree.service;

import com.web.saree.dto.response.ShippingAddressResponse;
import com.web.saree.entity.ShippingAddress;
import com.web.saree.entity.Users;
import com.web.saree.repository.ShippingAddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ShippingAddressService {

    @Autowired
    private ShippingAddressRepository addressRepository;


    public ShippingAddress saveNewAddress(ShippingAddress addressRequest, Users user) {
        ShippingAddress newAddress = new ShippingAddress();
        newAddress.setUser(user);
        newAddress.setFullName(addressRequest.getFullName());
        newAddress.setStreet(addressRequest.getStreet());
        newAddress.setCity(addressRequest.getCity());
        newAddress.setState(addressRequest.getState());
        newAddress.setPincode(addressRequest.getPincode());
        newAddress.setPhone(addressRequest.getPhone());

        // आप यहाँ वैलिडेटर या बिज़नेस लॉजिक जोड़ सकते हैं

        return addressRepository.save(newAddress);
    }

    public List<ShippingAddressResponse> getAddressesByUser(Users user) {

        List<ShippingAddress> addresses = addressRepository.findByUser(user);

        // Entity List को DTO List में बदलें
        return addresses.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Entity से Response DTO में बदलने के लिए निजी (private) विधि।
     */
    private ShippingAddressResponse mapToResponseDto(ShippingAddress address) {
        ShippingAddressResponse response = new ShippingAddressResponse();
        // ध्यान दें: आपको DTO में 'id' फ़ील्ड भी जोड़ना चाहिए ताकि फ्रंटएंड उसे सेलेक्ट कर सके।
        // अभी के लिए, हम सिर्फ़ मौजूदा फ़ील्ड्स का उपयोग कर रहे हैं।
        // यदि DTO में ID नहीं है, तो फ्रंटएंड काम नहीं करेगा।

        // मान लें कि DTO में ID भी है:
        response.setId(address.getId());

        response.setFullName(address.getFullName());
        response.setStreet(address.getStreet());
        response.setCity(address.getCity());
        response.setState(address.getState());
        response.setPincode(address.getPincode());
        response.setPhone(address.getPhone());
        return response;
    }


    public Optional<ShippingAddress> getAddressByIdAndUser(Long id, Users user) {
        return Optional.ofNullable(addressRepository.findByIdAndUser(id, user));
    }
}