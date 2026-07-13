package com.mun.inventorysystem.service;

import com.mun.inventorysystem.model.Product;
import com.mun.inventorysystem.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product addProduct(Product product) {
        return productRepository.save(product);
    }

    // adds/removes stock. quantity can be negative for removing stock
    public Product updateStock(Long id, int quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        int newStock = product.getStockQuantity() + quantity;
        if (newStock < 0) {
            throw new IllegalArgumentException("Not enough stock available");
        }

        product.setStockQuantity(newStock);
        return productRepository.save(product);
    }

    public double calculateFinalPrice(Product product) {
        double discount = product.getDiscountPercentage();

        // discount should always be a valid percentage
        if (discount < 0 || discount > 100) {
            throw new IllegalArgumentException("Discount must be between 0 and 100");
        }

        double discountAmount = product.getPrice() * (discount / 100);
        double finalPrice = product.getPrice() - discountAmount;
        return finalPrice;
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found");
        }
        productRepository.deleteById(id);
    }
}