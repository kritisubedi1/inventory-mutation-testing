package com.mun.inventorysystem.service;

import com.mun.inventorysystem.model.Product;
import com.mun.inventorysystem.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;

    @BeforeEach
    void setUp() {
        // basic laptop product used across most tests
        product = new Product("Laptop", 1000.0, 10, 10.0);
        product.setId(1L);
    }

    @Test
    void testUpdateStock() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        Product updated = productService.updateStock(1L, 5);
        assertEquals(15, updated.getStockQuantity());
    }

    @Test
    void testCalculateFinalPrice() {
        // 1000 with 10% off should be 900
        double finalPrice = productService.calculateFinalPrice(product);
        assertEquals(900.0, finalPrice);
    }

    // PIT flagged this as NO_COVERAGE, added it after checking the report
    @Test
    void testGetAllProducts() {
        when(productRepository.findAll()).thenReturn(List.of(product));

        List<Product> products = productService.getAllProducts();
        assertEquals(1, products.size());
        assertEquals("Laptop", products.get(0).getName());
    }

    @Test
    void testGetProductById() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Optional<Product> found = productService.getProductById(1L);
        assertTrue(found.isPresent());
        assertEquals("Laptop", found.get().getName());
    }

    @Test
    void testAddProduct() {
        when(productRepository.save(product)).thenReturn(product);

        Product added = productService.addProduct(product);
        assertEquals("Laptop", added.getName());
    }

    @Test
    void testDeleteProduct() {
        when(productRepository.existsById(1L)).thenReturn(true);
        productService.deleteProduct(1L);
        verify(productRepository).deleteById(1L);
    }

    // also kills the negated-conditional mutant PIT found on line 57
    @Test
    void testDeleteProduct_notFound_throws() {
        when(productRepository.existsById(99L)).thenReturn(false);
        assertThrows(RuntimeException.class, () -> productService.deleteProduct(99L));
    }
}