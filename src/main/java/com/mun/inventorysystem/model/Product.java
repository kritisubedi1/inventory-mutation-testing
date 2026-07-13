package com.mun.inventorysystem.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name is required")
    private String name;

    // price has to be positive, otherwise the discount math breaks down later
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private double price;

    @Min(value = 0, message = "Stock cannot be negative")
    private int stockQuantity;

    private double discountPercentage; // 0 = no discount, 10 = 10% off, etc.

    public Product() {
        // needed by JPA, don't remove
    }

    public Product(String name, double price, int stockQuantity, double discountPercentage) {
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.discountPercentage = discountPercentage;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int qty) {
        stockQuantity = qty;
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(double discount) {
        discountPercentage = discount;
    }
}