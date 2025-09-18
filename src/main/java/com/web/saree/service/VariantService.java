package com.web.saree.service;

import com.web.saree.dto.response.sareeResponse.AllSareeResponse;
import com.web.saree.entity.Saree;
import com.web.saree.entity.Variant;
import com.web.saree.repository.VariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class VariantService {
    private final VariantRepository variantRepository;


}
