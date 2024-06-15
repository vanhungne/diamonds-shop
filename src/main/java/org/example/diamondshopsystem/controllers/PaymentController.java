package org.example.diamondshopsystem.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.diamondshopsystem.dto.OrderDTO;
import org.example.diamondshopsystem.dto.PaymentDTO;
import org.example.diamondshopsystem.payload.ResponseObject;
import org.example.diamondshopsystem.payload.requests.PaymentRequest;
import org.example.diamondshopsystem.services.OrderService;
import org.example.diamondshopsystem.services.PaymentService;
import org.example.diamondshopsystem.services.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/payment")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PaymentController {

    @Autowired
    PaymentService paymentService;

    @Autowired
    ShoppingCartService shoppingCartService;

    @Autowired
    OrderService orderService;

    @PostMapping
    public ResponseEntity<?> payment(@RequestBody PaymentRequest paymentRequest,
                                     HttpServletRequest request) {
        String bankCode = paymentRequest.getBankCode();
        if (bankCode == null || bankCode.isEmpty()) {
            return ResponseEntity.badRequest().body("Bank code is required.");
        }
        Integer orderId = paymentRequest.getOrderId();
        BigDecimal totalPrice = orderService.findPriceByOrderId(orderId);
        PaymentDTO.VNPayResponse vnPayResponse = paymentService.createVnPayPayment(totalPrice, bankCode, paymentRequest.getOrderId(), request);
        return ResponseEntity.ok(vnPayResponse);
    }

    @GetMapping("/VNPayBack")
    public ResponseObject<PaymentDTO.VNPayResponse> payCallbackHandler(HttpServletRequest request) {
        try {

            Map<String, String[]> parameterMap = request.getParameterMap();
            boolean isValid = paymentService.verifyVNPayCallback(parameterMap);
            if (isValid) {
                PaymentDTO paymentDTO = paymentService.convertRequestToPaymentDTO(parameterMap);

                orderService.setOrderStatus(Integer.parseInt(parameterMap.get("vnp_OrderInfo")[0].split("Order ID: ")[1]));

                paymentService.savePayment(paymentDTO);
                return new ResponseObject<>(HttpStatus.OK, "Success", new PaymentDTO.VNPayResponse("00", "Payment verified and saved successfully.", "https://www.facebook.com/hungpitit/"));
            } else {
                return new ResponseObject<>(HttpStatus.BAD_REQUEST, "Failed", new PaymentDTO.VNPayResponse("01", "Invalid payment signature.", "https://www.facebook.com/myduyenpogo"));
            }
        } catch (Exception e) {
            return new ResponseObject<>(HttpStatus.INTERNAL_SERVER_ERROR, "Error", new PaymentDTO.VNPayResponse("02", "An error occurred: " + e.getMessage(), "https://www.facebook.com/nguyen.t.tin.7311"));
        }
    }
}
