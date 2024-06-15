package org.example.diamondshopsystem.controllers;

import org.example.diamondshopsystem.dto.ProductDTO;
import org.example.diamondshopsystem.payload.ResponseData;
import org.example.diamondshopsystem.services.FileService;
import org.example.diamondshopsystem.services.ProductService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/home")
@CrossOrigin(origins = "*")
public class HomeController {

    @Autowired
    FileService fileService;

    @Autowired
    ProductService productService;

    @GetMapping("/collection")
    public ResponseEntity<Page<ProductDTO>> getProductByCollection(@RequestParam String collection,
                                                                   @RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTO> products = productService.getProductByCollection(collection, pageable);
        if (!products.isEmpty()) {
            return ResponseEntity.ok(products);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/featured")
    public ResponseEntity<List<ProductDTO>> getFeaturedProducts() {
        ResponseData responseData = new ResponseData();
        List<ProductDTO> featuredProducts = productService.getFeaturedProduct();
        responseData.setData(featuredProducts);
        return ResponseEntity.ok(featuredProducts);
    }

    @PostMapping("/getProductByCategory")
    public ResponseEntity<?> getProductByCategory(@RequestParam String categoryName,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
       Page<ProductDTO> productCate = productService.getProductByCategory(categoryName,pageable);
        return new ResponseEntity<>(productCate, HttpStatus.OK);
    }

    @GetMapping("/products")
    public ResponseEntity<Page<ProductDTO>> getAllProducts(@RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTO> productsPage = productService.getAllProduct(pageable);
        return ResponseEntity.ok(productsPage);
    }

    @GetMapping("/sortByPrice")
    public ResponseEntity<Page<ProductDTO>> sortByPrice(@RequestParam String order,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTO> sortedProducts = productService.getProductStoredByPrice(order, pageable);
        if (!sortedProducts.isEmpty()) {
            return ResponseEntity.ok(sortedProducts);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
