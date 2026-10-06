package com.business.business_management_api_v2.repository;

import com.business.business_management_api_v2.entity.Order;
import com.business.business_management_api_v2.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepo extends JpaRepository<Order,Long> {
    Page<Order> findByUserId(Long userId, Pageable pageable);
    Page<Order> findByUserIdAndStatus(Long userId, OrderStatus status, Pageable pageable);
    Optional<Order> findByIdAndUserId(Long id, Long userId);

}
