package org.example.diamondshopsystem.controllers;

import org.example.diamondshopsystem.dto.ProductDTO;
import org.example.diamondshopsystem.payload.ResponseData;
import org.example.diamondshopsystem.services.FileService;
import org.example.diamondshopsystem.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;

@RestController
@RequestMapping("/product")
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired
    ProductService productService;

    @Autowired
    FileService fileService;

    @PostMapping("/savefile")
    public ResponseEntity<ResponseData> uploadFile(@RequestParam MultipartFile file) {
        ResponseData responseData = new ResponseData();
        boolean isSuccess = fileService.saveFile(file);
        responseData.setData(isSuccess);
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }
    @GetMapping("/load/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {

        Resource fileResource = fileService.loadFile(filename);

        try {

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileResource.getFilename() + "\"")
                    .body(fileResource);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/load-image/{filename}")
    public ResponseEntity<Resource> loadImage(@PathVariable String filename) {
        Resource fileResource = fileService.loadFile(filename);

        if (fileResource != null && fileResource.exists() && fileResource.isReadable()) {
            try {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, Files.probeContentType(fileResource.getFile().toPath()))
                        .body(fileResource);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable int id) {
        ProductDTO product = productService.getProductDTOById(id);
        if (product != null) {
            return ResponseEntity.ok(product);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/add")
    public ResponseEntity<ProductDTO> addProduct(@RequestBody ProductDTO product) {
        ProductDTO savedProduct = productService.addProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable int id, @RequestBody ProductDTO product) {
        product.setProductId(id);
        ProductDTO updatedProduct = productService.updateProduct(product);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable int id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/setprice")
    public void updateProductPricesOnStartup() {
        System.out.println("Hello controller");
        productService.updateProductPricesOnStartup();
    }
    //sort
    @GetMapping("/sort-by-name")
    public ResponseEntity<Page<ProductDTO>> getProductsSortedByName(
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.getProductsSortedByName(direction, pageable));
    }

    @GetMapping("/sort-by-stock")
    public ResponseEntity<Page<ProductDTO>> getProductsSortedByStockQuantity(
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.getProductsSortedByStockQuantity(direction, pageable));
    }

    @GetMapping("/search-by-name")
    public ResponseEntity<Page<ProductDTO>> getProductsByNameKeyword(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.getProductsByNameKeyword(keyword, pageable));
    }

    @GetMapping("/by-price-range")
    public ResponseEntity<Page<ProductDTO>> getProductsByPriceRange(
            @RequestParam double minPrice,
            @RequestParam double maxPrice,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.getProductsByPriceRange(minPrice, maxPrice, direction, pageable));
    }

    @GetMapping("/by-category-sorted-by-price")
    public ResponseEntity<Page<ProductDTO>> getProductsByCategorySortedByPrice(
            @RequestParam String categoryName,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.getProductsByCategorySortedByPrice(categoryName, direction, pageable));
    }

    @GetMapping("/by-multiple-criteria")
    public ResponseEntity<Page<ProductDTO>> getProductsByMultipleCriteria(
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) String collection,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.getProductsByMultipleCriteria(categoryName, collection, minPrice, maxPrice, pageable));
    }
}

