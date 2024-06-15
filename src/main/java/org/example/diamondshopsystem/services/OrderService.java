package org.example.diamondshopsystem.services;

import org.example.diamondshopsystem.dto.OrderDTO;
import org.example.diamondshopsystem.dto.PaymentDTO;
import org.example.diamondshopsystem.entities.Order;
import org.example.diamondshopsystem.entities.OrderDetails;
import org.example.diamondshopsystem.entities.OrderStatus;
import org.example.diamondshopsystem.entities.Payments;
import org.example.diamondshopsystem.repositories.OrderDetailRepository;
import org.example.diamondshopsystem.repositories.OrderRepository;
import org.example.diamondshopsystem.services.Map.OrderMapper;
import org.example.diamondshopsystem.services.imp.OrderServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService implements OrderServiceImp {


    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    OrderDetailRepository orderDetailRepository;

    public Page<Order> getOrdersByStatus(OrderStatus status, int page, int size) {
        return orderRepository.findByStatus(status, PageRequest.of(page, size));
    }

    @Override
    public void receiveOrder(OrderDTO orderDTO) {
        Order order = orderRepository.findById(orderDTO.getOrderId()).orElseThrow(() ->
                new IllegalArgumentException("Order does not exist in the database."));
        order.setStatus(OrderStatus.RECEIVED);
        orderRepository.save(order);
    }

    @Override
    public void confirmOrder(OrderDTO orderDTO, PaymentDTO paymentDTO) {
        Order order = orderRepository.findById(orderDTO.getOrderId()).orElseThrow(() ->
                new IllegalArgumentException("Order does not exist in the database."));

        Payments payments = new Payments();
        payments.setPaymentsId(paymentDTO.getPaymentsId());
        payments.setDescription(paymentDTO.getDescription());
        payments.setPaymentAmount(paymentDTO.getPaymentAmount());
        payments.setPaymentCode(paymentDTO.getPaymentCode());
        payments.setPaymentMode(paymentDTO.getPaymentMode());
        payments.setPaymentTime(paymentDTO.getPaymentTime());

        List<Payments> paymentsList = new ArrayList<>();
        paymentsList.add(payments);

        order.setPayments(paymentsList);
        order.setStatus(OrderStatus.CONFIRMED);

        List<OrderDetails> orderDetailsList = orderDetailRepository.findByOrder(order);
        for (OrderDetails orderDetail : orderDetailsList) {
            orderDetail.setSize(orderDetail.getSize());
            orderDetailRepository.save(orderDetail);
        }
        orderRepository.save(order);
    }
    public BigDecimal findPriceByOrderId(Integer orderId) {
        BigDecimal totalPrice = orderRepository.findTotalPriceByOrderId(orderId);
        return totalPrice;
    }

    @Override
    public void setOrderStatus(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if (isStatusTransitionAllowed(order.getStatus(), OrderStatus.PAYMENT)) {
            order.setStatus(OrderStatus.PAYMENT);
            try {
                orderRepository.save(order);
            } catch (DataIntegrityViolationException e) {
                // This catches constraint violation exceptions
                throw new IllegalStateException("Cannot update order status. The new status is not allowed.", e);
            }
        } else {
            throw new IllegalStateException("Status transition from " + order.getStatus() + " to " + OrderStatus.PAYMENT + " is not allowed.");
        }
    }

    private boolean isStatusTransitionAllowed(OrderStatus currentStatus, OrderStatus newStatus) {
        switch (currentStatus) {
            case PENDING:
                return newStatus == OrderStatus.PAYMENT || newStatus == OrderStatus.CANCELED;
            case PAYMENT:
                return newStatus == OrderStatus.CONFIRMED || newStatus == OrderStatus.CANCELED;
            case CONFIRMED:
                return newStatus == OrderStatus.DELIVERED || newStatus == OrderStatus.CANCELED;
            case DELIVERED:
                return newStatus == OrderStatus.RECEIVED || newStatus == OrderStatus.CANCELED;
            case CANCELED:
                return false; // Không cho phép thay đổi trạng thái nếu đã hủy
            case RECEIVED:
                return false; // Đơn hàng đã nhận, không cho phép thay đổi trạng thái
            default:
                return false;
        }
    }
}