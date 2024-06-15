package org.example.diamondshopsystem.services.Map;

import org.example.diamondshopsystem.dto.ProductDTO;
import org.example.diamondshopsystem.entities.Diamond;
import org.example.diamondshopsystem.entities.Products;
import org.example.diamondshopsystem.entities.Shell;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class ProductMapper {
    public ProductDTO mapProductToDTO(Products products) {
        if (products == null) {
            return null;
        }
        ProductDTO productDTO = new ProductDTO();
        productDTO.setProductId(products.getProductId());
        productDTO.setProductName(products.getProductName());
        productDTO.setPrice(products.getPrice());
        productDTO.setStockQuantity(products.getStockQuantity());
        productDTO.setCollection(products.getCollection());
        productDTO.setDescription(products.getDescription());
        productDTO.setImage1(products.getImage1());
        productDTO.setImage2(products.getImage2());
        productDTO.setImage3(products.getImage3());
        productDTO.setImage4(products.getImage4());
        productDTO.setCategoryId(products.getCategory().getCategoryId());
//        productDTO.setDiamonds(products.getDiamonds());
        productDTO.setShellId(products.getShell().getShellId());
        return productDTO;
    }

    public Products mapProductDTOToProduct(ProductDTO productDTO) {
        if (productDTO == null) {
            return null;
        }
        Products product = new Products();
        product.setProductId(productDTO.getProductId());
        product.setProductName(productDTO.getProductName());
        product.setPrice(productDTO.getPrice());
        product.setStockQuantity(productDTO.getStockQuantity());
        product.setCollection(productDTO.getCollection());
        product.setDescription(productDTO.getDescription());
        product.setImage1(productDTO.getImage1());
        product.setImage2(productDTO.getImage2());
        product.setImage3(productDTO.getImage3());
        product.setImage4(productDTO.getImage4());

        // Assuming 'Diamond' and 'Shell' properties are already set in 'productDTO'
        Set<Diamond> diamonds = new HashSet<>();
        product.setDiamonds(diamonds);

        Shell shell = new Shell();
        shell.setShellId(productDTO.getShellId());
        product.setShell(shell);

        return product;
    }

    public void mapProductDTOToProduct(ProductDTO productDTO, Products product) {
        if (productDTO == null || product == null) {
            return;
        }
        product.setProductId(productDTO.getProductId());
        product.setProductName(productDTO.getProductName());
        product.setPrice(productDTO.getPrice());
        product.setStockQuantity(productDTO.getStockQuantity());
        product.setCollection(productDTO.getCollection());
        product.setDescription(productDTO.getDescription());
        product.setImage1(productDTO.getImage1());
        product.setImage2(productDTO.getImage2());
        product.setImage3(productDTO.getImage3());
        product.setImage4(productDTO.getImage4());
        product.setDiamonds(productDTO.getDiamonds());
        product.getShell().setShellId(productDTO.getShellId());
    }
}
