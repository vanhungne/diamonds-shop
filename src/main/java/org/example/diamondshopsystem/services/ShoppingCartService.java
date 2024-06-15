package org.example.diamondshopsystem.services;

import org.example.diamondshopsystem.dto.*;
import org.example.diamondshopsystem.entities.*;
import org.example.diamondshopsystem.repositories.*;
import org.example.diamondshopsystem.services.Map.OrderMapper;
import org.example.diamondshopsystem.services.Map.UserMapper;
import org.example.diamondshopsystem.services.exeptions.NotEnoughProductsInStockException;
import org.example.diamondshopsystem.services.imp.ShoppingCartServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@Transactional
public class ShoppingCartService implements ShoppingCartServiceImp {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    SizeRepository sizeRepository;

    @Autowired
    UserMapper userMapper;

    @Autowired
    OrderMapper orderMapper;

    private final Map<Products, Integer> cart = new HashMap<>();
    private final Map<Products, Double> productSizes = new HashMap<>();
    private BigDecimal totalPrice = BigDecimal.ZERO;
    @Autowired
    private DiamondsRepository diamondsRepository;

    @Override
    public synchronized void addProduct(Products product, int quantity, Integer sizeId) {
        Products managedProduct = productRepository.findById(product.getProductId()).orElseThrow(() ->
                new IllegalArgumentException("Product does not exist in the inventory."));

        int stockQuantity = managedProduct.getStockQuantity();
        if (stockQuantity == 0) {
            throw new IllegalArgumentException("Product does not exist in the inventory.");
        } else if (stockQuantity < quantity) {
            throw new NotEnoughProductsInStockException("Not enough products in stock.");
        }

        // Fetch size from the database using sizeId
        Size size = sizeRepository.findById(sizeId)
                .orElseThrow(() -> new IllegalArgumentException("Size not found"));

        cart.merge(managedProduct, quantity, Integer::sum);
        productSizes.put(managedProduct, size.getValueSize()); // Storing the size value as a Double
        totalPrice = totalPrice.add(BigDecimal.valueOf(managedProduct.getPrice()).multiply(BigDecimal.valueOf(quantity)));

        System.out.println("Added product: " + product.getProductId()
                + ", Quantity: " + quantity + ", Size: " + size.getValueSize()
                + ", New Total Price: " + totalPrice);
    }

    @Override
    public synchronized void removeProduct(Products product) {
        Products managedProduct = productRepository.findById(product.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product does not exist in the inventory"));

        if (cart.containsKey(managedProduct)) {
            int currentQuantity = cart.get(managedProduct);
            cart.remove(managedProduct);
            BigDecimal productTotalPrice = BigDecimal.valueOf(managedProduct.getPrice())
                    .multiply(BigDecimal.valueOf(currentQuantity));
            totalPrice = totalPrice.subtract(productTotalPrice);

            productSizes.remove(managedProduct);
            System.out.println("Removed product: " + managedProduct.getProductId()
                    + ", Quantity: " + currentQuantity + ", New Total Price: " + totalPrice);
        } else {
            throw new IllegalArgumentException("Product not in cart.");
        }
    }

    @Override
    public synchronized void updateProductQuantity(Products product, int newQuantity) {
        Products managedProduct = productRepository.findById(product.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product does not exist in the inventory"));
        if (cart.containsKey(managedProduct)) {
            int stockQuantity = productRepository.getStockQuantityById(managedProduct.getProductId());
            if (stockQuantity < newQuantity) {
                throw new NotEnoughProductsInStockException("Not enough products in stock.");
            }

            int currentQuantity = cart.get(managedProduct);
            cart.put(managedProduct, newQuantity);
            totalPrice = totalPrice.subtract(BigDecimal.valueOf(managedProduct.getPrice()).multiply(BigDecimal.valueOf(currentQuantity)))
                    .add(BigDecimal.valueOf(managedProduct.getPrice()).multiply(BigDecimal.valueOf(newQuantity)));

            System.out.println("Updated product: " + managedProduct.getProductId() + ", Quantity: " + newQuantity + ", New Total Price: " + totalPrice);
        } else {
            throw new IllegalArgumentException("Product is not in the cart");
        }
    }

    @Override
    public synchronized List<CartDTO> getProductsInCart() {
        List<CartDTO> productsInCart = new ArrayList<>();
        for (Map.Entry<Products, Integer> entry : cart.entrySet()) {
            Products product = entry.getKey();
            int quantity = entry.getValue();
            BigDecimal totalPrice = BigDecimal.valueOf(product.getPrice()).multiply(BigDecimal.valueOf(quantity));
            Double size = productSizes.get(product);

            CartDTO cartDTO = new CartDTO();
            cartDTO.setProductId(product.getProductId());
            cartDTO.setProductName(product.getProductName());
            cartDTO.setQuantity(quantity);
            cartDTO.setTotalPrice(totalPrice);
            cartDTO.setImage1(product.getImage1());
            cartDTO.setSize(size);

            productsInCart.add(cartDTO);
        }
        return productsInCart;
    }

    @Override
    public void selectProductSize(Products product, SizeDTO sizeDTO) {
        if (cart.containsKey(product)) {
            Size size = sizeRepository.findById(sizeDTO.getSizeId())
                    .orElseThrow(() -> new IllegalArgumentException("Size not found"));
            productSizes.put(product, size.getValueSize());
        } else {
            throw new IllegalArgumentException("Product is not in the cart");
        }
    }

    @Override
    public synchronized BigDecimal getTotalPrice() {
        System.out.println("Getting Total Price: " + totalPrice);
        return totalPrice;
    }

    @Transactional
    @Override
    public synchronized void checkout(OrderDTO orderDTO, UserDTO customer, String address) {
        // Convert UserDTO to User
        User user = userMapper.mapUserDTOToUser(customer);

        // Check if the user exists in the database
        User managedUser = userRepository.findByEmail(user.getEmail());
        if (managedUser == null) {
            throw new IllegalArgumentException("User does not exist in the database.");
        }

        Order order = orderMapper.mapOrderDTOToOrder(orderDTO, managedUser);

        order.setOrderDeliveryAddress(address);

        Date currentDate = new Date();

        order.setOrderDate(currentDate);
        order.setOrderTotalAmount(totalPrice.doubleValue());
        order.setStatus(OrderStatus.PENDING);
        order = orderRepository.save(order);

        // Create OrderDetails from products in the cart
        for (Map.Entry<Products, Integer> entry : cart.entrySet()) {
            Products product = entry.getKey();
            int quantity = entry.getValue();
            Double size = productSizes.get(product);

            // Check if the product exists in the database
            Products managedProduct = productRepository.findById(product.getProductId()).orElseThrow(() ->
                    new IllegalArgumentException("Product does not exist in the inventory."));

            // Check if there is enough stock quantity
            int stockQuantity = managedProduct.getStockQuantity();

            //set lai status diamond
            Diamond diamondOfProduct = diamondsRepository.findFirstByProduct_ProductIdAndStatusIsTrue(managedProduct.getProductId());
                    if (diamondOfProduct == null) {
                        throw new IllegalArgumentException("Diamond does not exist in the inventory.");
                    }else {
                        diamondOfProduct.setStatus(false);
                        diamondsRepository.save(diamondOfProduct);
                    }

            if (stockQuantity < quantity) {
                throw new NotEnoughProductsInStockException("Not enough products in stock.");
            }
            // Create OrderDetailDTO
            OrderDetailDTO orderDetailDTO = new OrderDetailDTO();
            orderDetailDTO.setOrderId(order.getOrderId());
            orderDetailDTO.setProductId(managedProduct.getProductId());
            orderDetailDTO.setQuantity(quantity);
            orderDetailDTO.setPrice(product.getPrice());
            orderDetailDTO.setSize(size); // Updated to use size as double

            OrderDetails orderDetails = orderMapper.mapOrderDetailDTOToOrderDetail(orderDetailDTO, order, managedProduct);
            orderDetailRepository.save(orderDetails);
        }
        cart.clear();
        productSizes.clear();
        totalPrice = BigDecimal.ZERO;
        System.out.println("Checkout completed, cart cleared. New Total Price: " + totalPrice);
    }
}
