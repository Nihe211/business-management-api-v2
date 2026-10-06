package com.business.business_management_api_v2.controller;

import com.business.business_management_api_v2.dto.request.OrderRequest;
import com.business.business_management_api_v2.dto.response.OrderResponse;
import com.business.business_management_api_v2.entity.RefreshToken;
import com.business.business_management_api_v2.enums.OrderStatus;
import com.business.business_management_api_v2.security.CustomUserDetails;
import com.business.business_management_api_v2.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderRequest request, @AuthenticationPrincipal CustomUserDetails principal){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.create(principal.getId(),request));
    }

    @GetMapping("/my-orders")
    public Page<OrderResponse> myOrders (@RequestParam(required = false)OrderStatus status,
                                         @AuthenticationPrincipal CustomUserDetails principal,
                                         @PageableDefault(size = 10,sort = "createdAt",direction = Sort.Direction.DESC)Pageable pageable){
        return orderService.getMyOrders(principal.getId(),status,pageable);
    }

    // TODO 1: GET /{id} -> Lấy chi tiết một đơn hàng của mình
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        OrderResponse response = orderService.getMyOrder(principal.getId(), id);
        return ResponseEntity.ok(response);
    }

    // TODO 2: PATCH /{id}/cancel -> Huỷ đơn hàng
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        OrderResponse response = orderService.cancel(principal.getId(), id);
        return ResponseEntity.ok(response);
    }


}
