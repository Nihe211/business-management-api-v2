package com.business.business_management_api_v2.mapper;

import com.business.business_management_api_v2.dto.request.ProductRequest;
import com.business.business_management_api_v2.dto.response.ProductResponse;
import com.business.business_management_api_v2.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductResponse toResponse(Product product);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    Product toEntity(ProductRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    void updatedFromRequest(ProductRequest request, @MappingTarget Product product);
}
