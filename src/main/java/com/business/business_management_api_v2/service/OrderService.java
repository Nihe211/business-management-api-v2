package com.business.business_management_api_v2.service;

import com.business.business_management_api_v2.dto.request.OrderRequest;
import com.business.business_management_api_v2.dto.response.OrderResponse;
import com.business.business_management_api_v2.entity.Order;
import com.business.business_management_api_v2.entity.OrderItem;
import com.business.business_management_api_v2.entity.Product;
import com.business.business_management_api_v2.enums.OrderStatus;
import com.business.business_management_api_v2.exception.ConflictException;
import com.business.business_management_api_v2.exception.ResourceNotFoundException;
import com.business.business_management_api_v2.mapper.OrderMapper;
import com.business.business_management_api_v2.repository.OrderRepo;
import com.business.business_management_api_v2.repository.ProductRepo;
import com.business.business_management_api_v2.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepo orderRepo;
    private final ProductRepo productRepo;
    private final UserRepo userRepo;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderResponse create(Long userId, OrderRequest request) {
        // TODO 1: gộp các dòng trùng productId:
        //   Map<Long, Integer> quantities = new TreeMap<>();
        Map<Long,Integer> quantities = new TreeMap<>();
        //   với mỗi item: quantities.merge(item.getProductId(), item.getQuantity(), Integer::sum);
        request.getItems().forEach(item -> quantities.merge(item.getProductId(), item.getQuantity(), Integer::sum));
        // TODO 2: khoá và lấy sản phẩm:
        //   List<Product> products = productRepo.findAllByIdInOrderByIdAsc(quantities.keySet());
        List<Product> products = productRepo.findAllByIdInOrderByIdAsc(quantities.keySet());
        // TODO 3: products.size() != quantities.size() -> ném ResourceNotFoundException("Có sản phẩm không tồn tại")
        if (products.size() != quantities.size()) {
            throw new ResourceNotFoundException("Có sản phẩm không tồn tại trong hệ thống");
        }
        // TODO 4: tạo Order: setUser(userRepo.getReferenceById(userId)), setShippingAddress, setNote
        Order order = new Order();
        order.setUser(userRepo.getReferenceById(userId));
        order.setShippingAddress(request.getShippingAddress());
        order.setNote(request.getNote());
        // TODO 5: BigDecimal total = BigDecimal.ZERO; với mỗi product trong products:
        //     - !product.isActive() -> ResourceNotFoundException("Sản phẩm '...' đã ngừng kinh doanh")
        //     - int qty = quantities.get(product.getId());
        //     - product.getStockQuantity() < qty -> ConflictException("Sản phẩm '...' không đủ tồn kho (còn X)")
        //     - product.setStockQuantity(product.getStockQuantity() - qty);
        //     - tạo OrderItem: setProduct, setQuantity(qty), setUnitPrice(product.getPrice()); order.addItem(item);
        //     - total = total.add(product.getPrice().multiply(BigDecimal.valueOf(qty)));
        BigDecimal total = BigDecimal.ZERO;
        for (Product product: products){
            if (!product.isActive()) {
                throw new ResourceNotFoundException("Sản phẩm '" + product.getName() + "' đã ngừng kinh doanh");
            }
            int qty = quantities.get(product.getId());
            if (product.getStockQuantity() < qty) {
                throw new ConflictException("Sản phẩm '" + product.getName() +
                        "' không đủ tồn kho (còn " + product.getStockQuantity() + ")");
            }

            product.setStockQuantity(product.getStockQuantity() - qty);
            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(qty);
            item.setUnitPrice(product.getPrice());
            order.addItem(item);
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(qty)));
        }
        // TODO 6: order.setTotalAmount(total); Order saved = orderRepo.save(order);
        //         return orderMapper.toResponse(saved);
        order.setTotalAmount(total);
        Order saved = orderRepo.save(order);
        return orderMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrders(Long userId, OrderStatus status, Pageable pageable){
        Page<Order> page = (status==null)
                ?orderRepo.findByUserId(userId,pageable)
                :orderRepo.findByUserIdAndStatus(userId,status,pageable);
        return page.map(orderMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public OrderResponse getMyOrder(Long userId, Long orderId) {
        // TODO: orderRepo.findByIdAndUserId(orderId, userId) -> không có thì ResourceNotFoundException
        //       rồi orderMapper.toResponse(...)
        return orderRepo.findByIdAndUserId(orderId,userId)
                .map(orderMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng hoặc bạn không có quyền truy cập"));
    }

    @Transactional
    public OrderResponse cancel(Long userId, Long orderId) {
        // TODO 1: tìm đơn bằng findByIdAndUserId(orderId, userId) -> không có thì ResourceNotFoundException
        Order order = orderRepo.findByIdAndUserId(orderId,userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng hoặc bạn không có quyền truy cập"));

        // TODO 2: status khác PENDING -> ConflictException("Chỉ có thể huỷ đơn ở trạng thái PENDING")
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new ConflictException("Chỉ có thể huỷ đơn ở trạng thái PENDING");
        }
        // TODO 3: gom id sản phẩm của các dòng hàng vào một TreeSet, gọi productRepo.findAllByIdInOrderByIdAsc(ids)
        //         (mục đích: khoá các sản phẩm đó trước khi cộng lại kho)
        Set<Long> productIds = order.getItems().stream()
                .map(item ->item.getProduct().getId())
                .collect(Collectors.toCollection(TreeSet::new));
        List<Product> lockedProducts = productRepo.findAllByIdInOrderByIdAsc(productIds);
        // TODO 4: với mỗi item: product.setStockQuantity(product.getStockQuantity() + item.getQuantity())
        Map<Long,Integer> refundQuantities = order.getItems().stream()
                .collect(Collectors.groupingBy(item -> item.getProduct().getId(),Collectors.summingInt(OrderItem::getQuantity)));
        for (Product product:lockedProducts){
            int qtyToRefund = refundQuantities.getOrDefault(product.getId(),0);
            product.setStockQuantity(product.getStockQuantity()+qtyToRefund);
        }
        // TODO 5: order.setStatus(OrderStatus.CANCELLED); return orderMapper.toResponse(order);
        order.setStatus(OrderStatus.CANCELLED);
        return orderMapper.toResponse(order);
    }
}
