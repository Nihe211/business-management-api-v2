package com.business.business_management_api_v2.service;

import com.business.business_management_api_v2.dto.request.ProductRequest;
import com.business.business_management_api_v2.dto.response.ProductResponse;
import com.business.business_management_api_v2.entity.Product;
import com.business.business_management_api_v2.exception.ConflictException;
import com.business.business_management_api_v2.exception.ResourceNotFoundException;
import com.business.business_management_api_v2.mapper.ProductMapper;
import com.business.business_management_api_v2.repository.ProductRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepo productRepo;
    private final ProductMapper productMapper;
    public ProductResponse create(ProductRequest request){
        if(productRepo.existsByNameAndActiveTrue(request.getName())){
            throw new ConflictException("Tên sản phẩm đã tồn tại");
        }
        Product product = productMapper.toEntity(request);
        Product saved = productRepo.save(product);
        return toResponse(saved);
    }

    public Page<ProductResponse> getAll(Pageable pageable) {
        return productRepo.findAllByActiveTrue(pageable).map(this::toResponse);
    }

    public ProductResponse getById(Long id){
        return toResponse(findActiveOrThrow(id));
    }

    public ProductResponse update(Long id, ProductRequest request){
        if(productRepo.existsByNameAndActiveTrueAndIdNot(request.getName(),id)){
            throw new ConflictException("Tên sản phẩm đã tồn tại");
        }
        Product product = findActiveOrThrow(id);
        productMapper.updatedFromRequest(request,product);
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
