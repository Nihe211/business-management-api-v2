package com.business.business_management_api_v2.service;

import com.business.business_management_api_v2.dto.request.ProductRequest;
import com.business.business_management_api_v2.dto.response.ProductResponse;
import com.business.business_management_api_v2.entity.Product;
import com.business.business_management_api_v2.exception.ResourceNotFoundException;
import com.business.business_management_api_v2.repository.ProductRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepo productRepo;

    public ProductResponse create(ProductRequest request){
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        Product saved = productRepo.save(product);
        return toResponse(saved);
    }

    public List<ProductResponse> getAll(){
        return productRepo.findAllByActiveTrue().stream().map(this::toResponse).toList();
    }

    public ProductResponse getById(Long id){
        return toResponse(findActiveOrThrow(id));
    }

    public ProductResponse update(Long id, ProductRequest request){
        Product product = findActiveOrThrow(id);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        Product saved = productRepo.save(product);
        return toResponse(saved);
    }

    public void delete(Long id){
        Product product = findActiveOrThrow(id);
        product.setActive(false);
        Product saved = productRepo.save(product);
    }

    private ProductResponse toResponse(Product p){
        return new ProductResponse(
                p.getId(), p.getName(), p.getDescription(),
                p.getPrice(), p.getStockQuantity(), p.isActive());
    }

    private Product findActiveOrThrow(Long id){
        return productRepo.findByIdAndActiveTrue(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm có ID: " + id));
    }


}
