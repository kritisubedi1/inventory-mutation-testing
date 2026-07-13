package com.mun.inventorysystem.repository;

import com.mun.inventorysystem.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // JpaRepository already handles save/find/delete for us
    // will add custom finder methods here if the business logic needs them later
}