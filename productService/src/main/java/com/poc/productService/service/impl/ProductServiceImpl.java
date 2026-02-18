package com.poc.productService.service.impl;

import com.poc.productService.dto.ProductRequestDTO;
import com.poc.productService.dto.ProductResponseDTO;
import com.poc.productService.entity.Product;
import com.poc.productService.repository.ProductRepository;
import com.poc.productService.service.ProductService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repo;

    @Override
    public ProductResponseDTO addProduct(ProductRequestDTO dto) {

        Product product = Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .stock(dto.getStock())
                .active(true)
                .build();

        Product saved = repo.save(product);

        return map(saved);
    }

    @Override
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO dto) {

        Product product = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());

        return map(repo.save(product));
    }

    @Override
    public void deleteProduct(Long id) {

        if (!repo.existsById(id)) {
            throw new RuntimeException("Product not found with id: " + id);
        }

        repo.deleteById(id);
    }

    @Override
    @Transactional
    public List<ProductResponseDTO> getAllProducts() {
        return repo.findAll()
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    @Transactional
    public List<ProductResponseDTO> getActiveProducts() {
        return repo.findByActiveTrue()
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    public void decreaseStock(Long id, Integer quantity) {

        Product product = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        if (!product.getActive()) {
            throw new RuntimeException("Product is inactive");
        }

        if (product.getStock() < quantity) {
            throw new RuntimeException("Insufficient stock");
        }

        product.setStock(product.getStock() - quantity);

        repo.save(product);
    }

    @Override
    public void activateProduct(Long id) {

        Product product = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        product.setActive(true);
        repo.save(product);
    }

    @Override
    public void deactivateProduct(Long id) {

        Product product = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        product.setActive(false);
        repo.save(product);
    }

    private ProductResponseDTO map(Product product) {
        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .active(product.getActive())
                .build();
    }
}
