package com.poc.productService.controller;

import com.poc.productService.dto.ProductRequestDTO;
import com.poc.productService.dto.ProductResponseDTO;
import com.poc.productService.entity.Product;
import com.poc.productService.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;

    // ADMIN
    @PostMapping
    public ProductResponseDTO add(@RequestBody ProductRequestDTO dto) {
        return service.addProduct(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponseDTO update(@PathVariable("id") Long id,
                                     @RequestBody ProductRequestDTO dto) {
        System.out.println("UPDATE HIT");

        return service.updateProduct(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable("id") Long id) {
        System.out.println("delete HIT");
        service.deleteProduct(id);
    }

    @PutMapping("/activate/{id}")
    public void activate(@PathVariable("id") Long id) {
        System.out.println("activate HIT");

        service.activateProduct(id);
    }

    @PutMapping("/deactivate/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deactivate(@PathVariable("id") Long id) {
        System.out.println("deactivate HIT");

        service.deactivateProduct(id);
    }

    // USER
    @GetMapping("/active")
    public List<ProductResponseDTO> getActive() {
        System.out.println("active HIT");

        return service.getActiveProducts();
    }

    @GetMapping("/all")
    public List<ProductResponseDTO> getAll() {
        System.out.println("all HIT");

        return service.getAllProducts();
    }
}



