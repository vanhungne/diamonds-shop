package org.example.diamondshopsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {
    private int productId;
    private String productName;
    private double price;
    private String image1;
    private BigDecimal totalPrice;
    private int quantity;
    private double size;
}
