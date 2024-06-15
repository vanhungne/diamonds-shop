package org.example.diamondshopsystem.services.imp;

import org.example.diamondshopsystem.dto.OrderDTO;
import org.example.diamondshopsystem.dto.PaymentDTO;
import org.example.diamondshopsystem.entities.Order;
import org.example.diamondshopsystem.entities.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public interface OrderServiceImp {
Page<Order> getOrdersByStatus(OrderStatus status, int page,int size);
    void receiveOrder(OrderDTO orderDTO);
    void confirmOrder(OrderDTO orderDTO, PaymentDTO paymentDTO);
//    void dispatchOrder(OrderDTO orderDTO);
    BigDecimal findPriceByOrderId(Integer orderId);
    void setOrderStatus(Integer orderId);
}
