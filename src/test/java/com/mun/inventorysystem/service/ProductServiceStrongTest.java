package com.mun.inventorysystem.service;

import com.mun.inventorysystem.model.Product;
import com.mun.inventorysystem.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

// edges + invalid inputs for updateStock / calculateFinalPrice
@ExtendWith(MockitoExtension.class)
public class ProductServiceStrongTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product("Laptop", 1000.0, 10, 10.0);
        product.setId(1L);
    }

    @Test
    void testUpdateStock_validAddition() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        Product updated = productService.updateStock(1L, 5);
        assertEquals(15, updated.getStockQuantity());
    }

    // take stock all the way to 0 - should still be allowed
    @Test
    void testUpdateStock_boundaryExactlyZero() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        Product updated = productService.updateStock(1L, -10);
        assertEquals(0, updated.getStockQuantity());
    }

    // one past zero should fail
    @Test
    void testUpdateStock_boundaryJustBelowZero_throws() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(IllegalArgumentException.class, () -> {
            productService.updateStock(1L, -11);
        });
    }

    @Test
    void testUpdateStock_productNotFound_throws() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            productService.updateStock(99L, 5);
        });

        // need the message too - type alone wasn't enough for PIT
        assertEquals("Product not found", ex.getMessage());
    }

    @Test
    void testCalculateFinalPrice_discountZero() {
        product.setDiscountPercentage(0.0);
        double finalPrice = productService.calculateFinalPrice(product);
        assertEquals(1000.0, finalPrice);
    }

    @Test
    void testCalculateFinalPrice_discountFull() {
        product.setDiscountPercentage(100.0);
        double finalPrice = productService.calculateFinalPrice(product);
        assertEquals(0.0, finalPrice);
    }

    @Test
    void testCalculateFinalPrice_discountBelowZero_throws() {
        product.setDiscountPercentage(-1.0);
        assertThrows(IllegalArgumentException.class,
                () -> productService.calculateFinalPrice(product));
    }

    @Test
    void testCalculateFinalPrice_discountAboveHundred_throws() {
        product.setDiscountPercentage(101.0);
        assertThrows(IllegalArgumentException.class,
                () -> productService.calculateFinalPrice(product));
    }
}
