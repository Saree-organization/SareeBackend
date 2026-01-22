    package com.web.saree.dto.response.sareeResponse;

    import com.web.saree.entity.Saree;
    import com.web.saree.entity.Variant;
    import lombok.Data;

    import java.util.ArrayList;
    import java.util.List;

    @Data
    public class AllSareeResponse {

        private Long id;
        private String fabrics;
        private String design;
        private Double length;
        private String description;
        private String border;
        private String category;
        private Double weight;

        private List<VariantResponse> variants = new ArrayList<> ();
        public void setSarees(Saree saree) {
            this.id = saree.getId();
            this.fabrics = saree.getFabrics();
            this.design = saree.getDesign();
            this.length = saree.getLength();
            this.description = saree.getDescription();
            this.border = saree.getBorder();
            this.category = saree.getCategory();
            this.weight = saree.getWeight();

            List<Variant> variantList = saree.getVariants();

            if (variantList != null && !variantList.isEmpty()) {
                Variant variant = variantList.get(0);

                VariantResponse variantResponse = new VariantResponse();
                variantResponse.setId(variant.getId());
                variantResponse.setSalesPrice(variant.getSalesPrice());
                variantResponse.setCostPrice(variant.getCostPrice());
                variantResponse.setDiscountPercent(variant.getDiscountPercent());
                variantResponse.setPriceAfterDiscount(variant.getPriceAfterDiscount());
                variantResponse.setStock(variant.getStock());

                List<String> images = new ArrayList<>();
                if (variant.getImages() != null) {
                    images.addAll(variant.getImages());
                }
                variantResponse.setImages(images);

                this.variants.add(variantResponse);
            }
        }

    }
