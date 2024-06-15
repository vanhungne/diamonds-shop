package org.example.diamondshopsystem.utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.diamondshopsystem.dto.CartDTO;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Component
public class CookieUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String CART_COOKIE_NAME = "cart";


    public static void saveCartToCookies(HttpServletResponse response, List<CartDTO> cartItems){

        try{
            String json = objectMapper.writeValueAsString(cartItems);
            String encodedCart = Base64.getEncoder().encodeToString(json.getBytes());
            Cookie cartCookie = new Cookie(CART_COOKIE_NAME, encodedCart);
            cartCookie.setPath("/");
            cartCookie.setHttpOnly(true);
            cartCookie.setMaxAge(7 * 24 * 60 * 60); // 1 week
            response.addCookie(cartCookie);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static List<CartDTO> getCartFromCookies(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        if(cookies != null){
            for (Cookie cookie : cookies) {
                if(cookie.getName().equals(CART_COOKIE_NAME)){
                    try {
                        String decodedCart = new String(Base64.getDecoder().decode(cookie.getValue()));
                        return objectMapper.readValue(decodedCart,
                                objectMapper.getTypeFactory()
                                        .constructCollectionType(List.class, CartDTO.class));
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
        }
        return new ArrayList<>();
    }
    public static void clearCartFromCookies(HttpServletResponse response){
        Cookie cartCookie = new Cookie(CART_COOKIE_NAME, "");
        cartCookie.setPath("/");
        cartCookie.setHttpOnly(true);
        cartCookie.setMaxAge(0);
        response.addCookie(cartCookie);
    }
}
