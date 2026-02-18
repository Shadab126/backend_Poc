package com.poc.productService.service;

import com.poc.productService.dto.ProductRequestDTO;
import com.poc.productService.dto.ProductResponseDTO;

import java.util.List;

public interface ProductService {

    ProductResponseDTO addProduct(ProductRequestDTO dto);

    ProductResponseDTO updateProduct(Long id, ProductRequestDTO dto);

    void deleteProduct(Long id);

    List<ProductResponseDTO> getAllProducts();

    List<ProductResponseDTO> getActiveProducts();

    void decreaseStock(Long id, Integer quantity);

    void activateProduct(Long id);

    void deactivateProduct(Long id);
}
