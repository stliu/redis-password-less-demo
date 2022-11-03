package com.example.redispasswordlessdemo;

import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public void createProduct(Product product) {
        productRepository.save(product);
    }

    public Product getProduct(String id) {
        return productRepository.findById(id).orElse(null);
    }

    // other methods
}
