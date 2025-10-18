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
            this.id = saree.getId ();
            this.fabrics = saree.getFabrics ();
            this.design = saree.getDesign ();
            this.length = saree.getLength ();
            this.description = saree.getDescription ();
            this.border = saree.getBorder ();
            this.category = saree.getCategory ();
            this.weight = saree.getWeight ();
            for (int i = 0; i < 1; i++){
                Variant variant = saree.getVariants ().get (i);
                System.out.println (variant);
                VariantResponse variantResponse = new VariantResponse ();
                variantResponse.setId (variant.getId ());
                variantResponse.setSalesPrice (variant.getSalesPrice ());
                variantResponse.setCostPrice (variant.getCostPrice ());
                variantResponse.setDiscountPercent (variant.getDiscountPercent ());
                variantResponse.setPriceAfterDiscount (variant.getPriceAfterDiscount ());
                variantResponse.setStock (variant.getStock ());

                // Safely get images
                List<String> images = new ArrayList<>();
                if (variant.getImages() != null) {
                    for (String img : variant.getImages()) {
                        images.add(img);
                    }
                }
                variantResponse.setImages (images);
                this.variants.add (variantResponse);
            }

        }
    }
