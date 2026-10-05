package com.business.business_management_api_v2.repository;

import com.business.business_management_api_v2.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepo extends JpaRepository<Product,Long> {
    List<Product> findAllByActiveTrue();
    Optional<Product> findByIdAndActiveTrue(Long id);
    Page<Product> findAllByActiveTrue(Pageable pageable);
    boolean existsByNameAndActiveTrue(String name);
    boolean existsByNameAndActiveTrueAndIdNot(String name, Long id);
}
