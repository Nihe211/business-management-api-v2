package com.business.business_management_api_v2.mapper;

import com.business.business_management_api_v2.dto.response.OrderItemResponse;
import com.business.business_management_api_v2.dto.response.OrderResponse;
import com.business.business_management_api_v2.entity.Order;
import com.business.business_management_api_v2.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    OrderResponse toResponse(Order order);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    OrderItemResponse toItemResponse(OrderItem item);
}
