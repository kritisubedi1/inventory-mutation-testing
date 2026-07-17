package com.mun.inventorysystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mun.inventorysystem.model.Product;
import com.mun.inventorysystem.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product("Laptop", 1000.0, 10, 10.0);
        product.setId(1L);
    }

    @Test
    void testGetAllProducts() throws Exception {
        when(productService.getAllProducts()).thenReturn(List.of(product));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Laptop"));
    }

    @Test
    void testGetProductById_found() throws Exception {
        when(productService.getProductById(1L)).thenReturn(Optional.of(product));

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Laptop"));
    }

    // product doesn't exist -> should get a 404, not a 500 or crash
    @Test
    void testGetProductById_notFound() throws Exception {
        when(productService.getProductById(99L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/products/99")).andExpect(status().isNotFound());
    }

    @Test
    void testAddProduct() throws Exception {
        when(productService.addProduct(any(Product.class))).thenReturn(product);

        mockMvc.perform(post("/api/products")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Laptop"));
    }

    @Test
    void testUpdateStock() throws Exception {
        when(productService.updateStock(1L, 5)).thenReturn(product);
        mockMvc.perform(put("/api/products/1/stock").param("quantity", "5"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetFinalPrice_found() throws Exception {
        when(productService.getProductById(1L)).thenReturn(Optional.of(product));
        when(productService.calculateFinalPrice(product)).thenReturn(900.0);

        mockMvc.perform(get("/api/products/1/final-price"))
                .andExpect(status().isOk())
                .andExpect(content().string("900.0"));
    }

    @Test
    void testGetFinalPrice_notFound() throws Exception {
        when(productService.getProductById(99L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/products/99/final-price")).andExpect(status().isNotFound());
    }

    @Test
    void testDeleteProduct() throws Exception {
        mockMvc.perform(delete("/api/products/1")).andExpect(status().isOk());
    }
}