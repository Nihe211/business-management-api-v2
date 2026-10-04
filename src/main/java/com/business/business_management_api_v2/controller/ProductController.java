package com.business.business_management_api_v2.controller;

import com.business.business_management_api_v2.entity.Product;
import com.business.business_management_api_v2.repository.ProductRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductRepo productRepo;

    @GetMapping
    public List<Product> getAll(){
        return productRepo.findAllByActiveTrue();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable Long id){
        return productRepo.findByIdAndActiveTrue(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Product> create(@RequestBody Product product){
        Product saved = productRepo.save(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> update(@PathVariable Long id, @RequestBody Product updateProduct){
        Optional<Product> optionalProduct = productRepo.findByIdAndActiveTrue(id);
        if(optionalProduct.isPresent()){
            Product product = optionalProduct.get();
            product.setName(updateProduct.getName());
            product.setDescription(updateProduct.getDescription());
            product.setPrice(updateProduct.getPrice());
            product.setStockQuantity(updateProduct.getStockQuantity());
            Product saved = productRepo.save(product);
            return ResponseEntity.status(HttpStatus.OK).body(saved);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteByID(@PathVariable Long id){
        Optional<Product> optionalProduct = productRepo.findById(id);
        if (optionalProduct.isPresent()){
            Product product = optionalProduct.get();
            product.setActive(false);
            Product saved = productRepo.save(product);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }


}
