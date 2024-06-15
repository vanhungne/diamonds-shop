package org.example.diamondshopsystem.services.imp;

import org.example.diamondshopsystem.dto.*;
import org.example.diamondshopsystem.entities.Products;
import org.example.diamondshopsystem.entities.User;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ShoppingCartServiceImp {
    void addProduct(Products product,int quantity,Integer sizeID);
    void removeProduct(Products product);
    void updateProductQuantity(Products product, int newQuantity);
//    Map<Products, Integer> getProductsInCart();
    void selectProductSize(Products product, SizeDTO size);
    void checkout(OrderDTO orderDTO, UserDTO user, String address);
    BigDecimal getTotalPrice();
    List<CartDTO> getProductsInCart();

}
