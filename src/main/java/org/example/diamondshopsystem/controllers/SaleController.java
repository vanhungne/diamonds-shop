package org.example.diamondshopsystem.controllers;

import org.example.diamondshopsystem.dto.OrderDTO;
import org.example.diamondshopsystem.dto.PaymentDTO;
import org.example.diamondshopsystem.dto.SizeDTO;
import org.example.diamondshopsystem.entities.Order;
import org.example.diamondshopsystem.entities.OrderStatus;
import org.example.diamondshopsystem.payload.requests.ConfirmOrderRequest;
import org.example.diamondshopsystem.repositories.OrderRepository;
import org.example.diamondshopsystem.services.Map.OrderMapper;
import org.example.diamondshopsystem.services.ShoppingCartService;
import org.example.diamondshopsystem.services.imp.OrderServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("sale-dasboard")
@CrossOrigin(origins = "*")
public class SaleController {

    @Autowired
    ShoppingCartService shoppingCartService;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderServiceImp orderServiceImp;
    @Autowired
    private OrderMapper orderMapper;

    @GetMapping("/new-orders")
    public ResponseEntity<List<OrderDTO>> getNewOrders(@RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size) {
        Page<Order> newOrdersPage = orderServiceImp.getOrdersByStatus(OrderStatus.PENDING, page, size);
        List<OrderDTO> newOrders = newOrdersPage.getContent().stream()
                .map(orderMapper::mapOrderToOrderDTO) // Sử dụng OrderMapper để chuyển đổi Order thành OrderDTO
                .collect(Collectors.toList());
        return ResponseEntity.ok(newOrders);
    }
    @PutMapping("/confirmOrder")
    public ResponseEntity<?> confirmOrder(@RequestBody ConfirmOrderRequest confirmOrderRequest) {
        OrderDTO orderDTO = confirmOrderRequest.getOrderDTO();
        PaymentDTO paymentDTO = confirmOrderRequest.getPaymentDTO();
        orderServiceImp.confirmOrder(orderDTO, paymentDTO);
        return ResponseEntity.ok("Order confirmed and payment processed successfully.");
    }
}
