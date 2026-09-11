package com.trendai.trendai.ai.service;

import com.trendai.trendai.ai.tool.ProductDetailsTool;
import com.trendai.trendai.ai.tool.ProductSearchTool;
import com.trendai.trendai.ai.tool.dto.ProductDetailsResult;
import com.trendai.trendai.ai.tool.dto.ProductSearchResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
public class AiDemoService {

    private static final String NO_PRODUCT_FOUND =
            "Uygun ürün bulunamadı";

    private final ProductSearchTool productSearchTool;
    private final ProductDetailsTool productDetailsTool;

    public AiDemoService(
            ProductSearchTool productSearchTool,
            ProductDetailsTool productDetailsTool) {

        this.productSearchTool = productSearchTool;
        this.productDetailsTool = productDetailsTool;
    }

    public String chat(String message) {

        try {
            String normalizedMessage = normalize(message);

            if (normalizedMessage.contains("20000")
                    && normalizedMessage.contains("telefon")) {

                return findPhonesUnder20000();
            }

            if (normalizedMessage.contains("elektronik")
                    && normalizedMessage.contains("en ucuz")
                    && normalizedMessage.contains("3")) {

                return findCheapestElectronics();
            }

            if (normalizedMessage.contains("1 numaralı ürün")
                    || normalizedMessage.contains("1 numarali urun")
                    || normalizedMessage.contains("ürün 1")
                    || normalizedMessage.contains("urun 1")) {

                return getProductOneDetails();
            }

            if (normalizedMessage.contains("olmayanmarka")) {

                return findProductsByBrand("OlmayanMarka");
            }

            return "Demo modunda desteklenen sorgulardan birini kullanın.";

        } catch (Exception e) {
            return NO_PRODUCT_FOUND;
        }
    }

    private String findPhonesUnder20000() {

        List<ProductSearchResult> products =
                productSearchTool.searchProducts(
                        "telefon",
                        null,
                        null,
                        null,
                        new BigDecimal("20000"),
                        "price,asc",
                        10
                );

        if (products.isEmpty()) {
            return NO_PRODUCT_FOUND;
        }

        return formatProducts(products);
    }

    private String findCheapestElectronics() {

        List<ProductSearchResult> products =
                productSearchTool.searchProducts(
                        null,
                        "Elektronik",
                        null,
                        null,
                        null,
                        "price,asc",
                        3
                );

        if (products.isEmpty()) {
            return NO_PRODUCT_FOUND;
        }

        return formatProducts(products);
    }

    private String getProductOneDetails() {

        try {
            List<ProductSearchResult> products =
                    productSearchTool.searchProducts(
                            null,
                            null,
                            null,
                            null,
                            null,
                            "id,asc",
                            1
                    );

            if (products.isEmpty()) {
                return NO_PRODUCT_FOUND;
            }

            Long productId = products.get(0).productId();

            ProductDetailsResult product =
                    productDetailsTool.getProductDetails(productId);

            return formatProductDetails(product);

        } catch (Exception e) {
            return NO_PRODUCT_FOUND;
        }
    }

    private String findProductsByBrand(String brand) {

        List<ProductSearchResult> products =
                productSearchTool.searchProducts(
                        null,
                        null,
                        brand,
                        null,
                        null,
                        "id,asc",
                        10
                );

        if (products.isEmpty()) {
            return NO_PRODUCT_FOUND;
        }

        return formatProducts(products);
    }

    private String formatProducts(
            List<ProductSearchResult> products) {

        StringBuilder response = new StringBuilder();

        for (ProductSearchResult product : products) {

            response.append(product.name())
                    .append(" - ")
                    .append(product.price())
                    .append(" TL")
                    .append(" - Stok: ")
                    .append(product.stock())
                    .append("\n");
        }

        return response.toString().trim();
    }

    private String formatProductDetails(
            ProductDetailsResult product) {

        return "Ürün: " + product.name()
                + "\nAçıklama: " + safeValue(product.description())
                + "\nMarka: " + safeValue(product.brand())
                + "\nRenk: " + safeValue(product.color())
                + "\nFiyat: " + product.price() + " TL"
                + "\nStok: " + product.stock()
                + "\nKategori: " + safeValue(product.categoryName());
    }

    private String safeValue(String value) {
        return value == null ? "-" : value;
    }

    private String normalize(String message) {

        if (message == null) {
            return "";
        }

        return message
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}