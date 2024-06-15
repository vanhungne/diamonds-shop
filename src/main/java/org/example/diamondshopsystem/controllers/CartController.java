package org.example.diamondshopsystem.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.diamondshopsystem.dto.*;
import org.example.diamondshopsystem.entities.Products;
import org.example.diamondshopsystem.payload.requests.*;
import org.example.diamondshopsystem.repositories.ProductRepository;
import org.example.diamondshopsystem.repositories.UserRepository;
import org.example.diamondshopsystem.services.PaymentService;
import org.example.diamondshopsystem.services.ShoppingCartService;
import org.example.diamondshopsystem.services.UserService;
import org.example.diamondshopsystem.utils.CookieUtil;
import org.example.diamondshopsystem.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cart")
@CrossOrigin(origins = "*")
public class CartController {

    @Autowired
    ShoppingCartService shoppingCartService;

    @Autowired
    UserService userService;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PaymentService paymentService;

    @Autowired
    ProductRepository productRepository;

    @PostMapping("/add")
    public ResponseEntity<?> addProductToCart(@RequestBody AddProductRequest addProductRequest,
                                              HttpServletRequest request,
                                              HttpServletResponse response) {
        //product
        Products product = productRepository.findById(addProductRequest.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        //size
        Integer sizeId = addProductRequest.getSizeId();
        if (sizeId == null) {
            return ResponseEntity.badRequest().body("Size must be selected for the product.");
        }

        try {
            shoppingCartService.addProduct(product, addProductRequest.getQuantity(), sizeId);
            //them no vao gio
            List<CartDTO> cartItem = shoppingCartService.getProductsInCart();
            //save vao cookie
            CookieUtil.saveCartToCookies(response,cartItem);//luu y

            return ResponseEntity.ok("Product added to cart successfully.");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while adding product to cart.");
        }
    }
    @DeleteMapping("/remove")
    public ResponseEntity<?> removeProductFromCart(@RequestBody RemoveProductRequest removeProductRequest,
                                                   HttpServletResponse response) {
        Products product = new Products();
        product.setProductId(removeProductRequest.getProductId());

        try {
            shoppingCartService.removeProduct(product);
            List<CartDTO> cartItems = shoppingCartService.getProductsInCart();
            CookieUtil.saveCartToCookies(response, cartItems);

            return ResponseEntity.ok("Product removed from cart successfully.");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while removing product from cart.");
        }
    }
    @PutMapping("/update")
    public ResponseEntity<?> updateProductInCart(@RequestBody UpdateProductRequest updateProductRequest,
                                                 HttpServletResponse response) {
        Products product = new Products();
        product.setProductId(updateProductRequest.getProductId());
        try {
            shoppingCartService.updateProductQuantity(product, updateProductRequest.getQuantity());
            List<CartDTO> cartItems = shoppingCartService.getProductsInCart();
            CookieUtil.saveCartToCookies(response, cartItems);
            return ResponseEntity.ok("Product quantity updated successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while updating product quantity.");
        }
    }

    @GetMapping()
    public List<CartDTO> getCartDetails(HttpServletRequest request) {
        // Load cart items from cookies
        List<CartDTO> cart = CookieUtil.getCartFromCookies(request);
        return cart;
    }
    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody CheckoutRequest checkoutRequest,
                                      @RequestHeader("Authorization") String authHeader,
                                      HttpServletResponse response){
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtUtil.verifyToken(token)) {

                String email = jwtUtil.getUsernameFromToken(token);
                UserDTO userDTO = userService.getUserByEmail(email);

                if (userDTO != null) {
                    OrderDTO orderDTO = new OrderDTO();
                    try {
                        shoppingCartService.checkout(orderDTO, userDTO, checkoutRequest.getDeliveryAddress());
                        CookieUtil.clearCartFromCookies(response);
                        return ResponseEntity.ok("Checkout successful.");
                    } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during checkout.");
                    }
                }
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing token.");
    }

    @GetMapping("/total")
    public ResponseEntity<BigDecimal> getTotalPrice(){
        return ResponseEntity.ok(shoppingCartService.getTotalPrice());
    }

}