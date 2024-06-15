package org.example.diamondshopsystem.services;

import org.example.diamondshopsystem.dto.ProductDTO;
import org.example.diamondshopsystem.entities.Diamond;
import org.example.diamondshopsystem.entities.Products;
import org.example.diamondshopsystem.entities.Shell;

import org.example.diamondshopsystem.repositories.DiamondsRepository;
import org.example.diamondshopsystem.repositories.ProductRepository;
import org.example.diamondshopsystem.repositories.ShellRepository;
import org.example.diamondshopsystem.services.Map.ProductMapper;
import org.example.diamondshopsystem.services.exeptions.ProductNotFoundException;
import org.example.diamondshopsystem.services.imp.ProductServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProductService implements ProductServiceImp {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    DiamondsRepository diamondsRepository;

    @Autowired
    ShellRepository shellRepository;

    @Autowired
    ProductMapper productMapper;


    @Override
    public ProductDTO getProductDTOById(int id) {
        Optional<Products> ProductOptional = productRepository.findById(id);
        if(ProductOptional.isPresent()) {
            Products products = ProductOptional.get();
            return productMapper.mapProductToDTO(products);
        }
        return null;
    }

    @Override
    public Products getProductById(int id) {
        Products products = productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Can not find product with id: " + id));
        return products;
    }



    @Override
    public ProductDTO addProduct(ProductDTO product) {
        Products products = productMapper.mapProductDTOToProduct(product);
        Products saveProduct = productRepository.save(products);
        return productMapper.mapProductToDTO(saveProduct);
    }

    @Override
    public ProductDTO updateProduct(ProductDTO product) {
        Optional<Products> productOptional = productRepository.findById(product.getProductId());
        if (productOptional.isPresent()) {
            Products existingProduct = productOptional.get();
            productMapper.mapProductDTOToProduct(product, existingProduct);

            //update price
            //get first diamond
            Iterator<Diamond> iterator = product.getDiamonds().iterator();

            // Kiểm tra xem hash set có phần tử không
            Diamond firstDiamond = null;

            if (iterator.hasNext()) {
                // Lấy phần tử đầu tiên
                firstDiamond = iterator.next();
            } else {
                firstDiamond.setPrice(0);
            }

            updateProductPrice(firstDiamond);
            updateProductPrice(existingProduct.getShell());

            Products updatedProduct = productRepository.save(existingProduct);
            return productMapper.mapProductToDTO(updatedProduct);
        } else {
            throw new ProductNotFoundException("Product not found with id: " + product.getProductId());
        }
    }

    @Override
    public void deleteProduct(int id) {
        Optional<Products> productOptional = productRepository.findById(id);
        if (productOptional.isPresent()) {
            productRepository.deleteById(id);
        } else {
            throw new ProductNotFoundException("Product not found with id: " + id);
        }
    }

    @Override
    public Page<ProductDTO> getProductByCollection(String collection, Pageable pageable) {
        Page<Products> productList = productRepository.findByCollection(collection, pageable);
        return productList.map(productMapper::mapProductToDTO);
    }
    @Override
    public List<ProductDTO> getFeaturedProduct() {
        List<Products> topProducts = productRepository.findTop10ByOrderByPriceDesc();
        if (!topProducts.isEmpty()) {
            List<ProductDTO> featuredProducts = new ArrayList<>();
            for (Products product : topProducts) {
                featuredProducts.add(productMapper.mapProductToDTO(product));
            }
            return featuredProducts;
        } else {
            throw new ProductNotFoundException("No featured products found.");
        }
    }

    @Override
    public double calculateTotalPrice(int id) {
        ProductDTO product = getProductDTOById(id);
        if (product == null) {
            throw new NoSuchElementException("Product not found with id: " + id);
        }

        double diamondPrice = 0.0;
        double shellPrice = 0.0;


            diamondPrice= diamondsRepository.findFirstByProduct_ProductIdAndStatusIsTrue(product.getProductId()).getPrice();
            System.out.println("follow Hưng-"+diamondPrice);

        if (product.getShellId() != 0) {
            Shell shell = shellRepository.findById(product.getShellId())
                    .orElseThrow(() -> new NoSuchElementException("Cannot find shell with shell id: " + product.getShellId()));
            shellPrice = shell.getShellPrice();
        }

        return (diamondPrice + shellPrice) * 1.3;
    }

    @Override
    public Page<ProductDTO> getAllProduct(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(productMapper::mapProductToDTO);
    }

    @Override
    public Page<ProductDTO> getProductByCategory(String categoryName,Pageable pageable) {
       Page<Products> productsPage = productRepository.getProductsByCategory(categoryName,pageable);
       return productsPage.map(productMapper::mapProductToDTO);
    }

    @Override
    public Page<ProductDTO> getProductStoredByPrice(String order, Pageable pageable) {
        Page<Products> productsPage;
        if(order.equals("asc")) {
            productsPage = productRepository.findAllByOrderByPriceAsc(pageable);
        } else {
            productsPage = productRepository.findAllByOrderByPriceDesc(pageable);
        }

        return productsPage.map(productMapper::mapProductToDTO);
    }

    @Override
    public void updateProductPrice(Diamond diamond) {
        if (diamond == null || diamond.getDiamondId() == 0) {

        }

        Products product = diamond.getProduct();

        Set<Diamond> diamondSet = productRepository.findSetDiamondByProductId(product.getProductId());

        //Lay phann tu dau tien cua set
        if (diamondSet != null && !diamondSet.isEmpty()) {
            Diamond diamond1 = diamondSet.iterator().next();
            if(diamond1.getDiamondId() == diamond.getDiamondId()) {
                double totalPrice = calculateTotalPrice(product.getProductId());
                product.setPrice(totalPrice);
                productRepository.save(product);
            }
        }
    }

    @Override
    public  void updateProductPrice(Shell shell) {
        List<Products> productList = productRepository.findAllByShellID(shell.getShellId());
        for (Products product : productList) {
            double totalPrice = calculateTotalPrice(product.getProductId());
            product.setPrice(totalPrice);
            productRepository.save(product);
        }
    }

    @Override
    public Page<ProductDTO> getProductsSortedByName(String direction, Pageable pageable) {
        Page<Products> productsPage = direction.equalsIgnoreCase("desc") ?
                productRepository.findAllByOrderByProductNameDesc(pageable) :
                productRepository.findAllByOrderByProductNameAsc(pageable);
        return productsPage.map(productMapper::mapProductToDTO);
    }

    @Override
    public Page<ProductDTO> getProductsSortedByStockQuantity(String direction, Pageable pageable) {
        Page<Products> productsPage = direction.equalsIgnoreCase("desc") ?
                productRepository.findAllByOrderByStockQuantityDesc(pageable) :
                productRepository.findAllByOrderByStockQuantityAsc(pageable);
        return productsPage.map(productMapper::mapProductToDTO);
    }

    @Override
    public Page<ProductDTO> getProductsByNameKeyword(String keyword, Pageable pageable) {
        return productRepository.findByProductNameContainingIgnoreCase(keyword, pageable)
                .map(productMapper::mapProductToDTO);
    }

    @Override
    public Page<ProductDTO> getProductsByPriceRange(double minPrice, double maxPrice, String direction, Pageable pageable) {
        Page<Products> productsPage = direction.equalsIgnoreCase("desc") ?
                productRepository.findByPriceBetweenOrderByPriceDesc(minPrice, maxPrice, pageable) :
                productRepository.findByPriceBetweenOrderByPriceAsc(minPrice, maxPrice, pageable);
        return productsPage.map(productMapper::mapProductToDTO);
    }

    @Override
    public Page<ProductDTO> getProductsByCategorySortedByPrice(String categoryName, String direction, Pageable pageable) {
        Page<Products> productsPage = direction.equalsIgnoreCase("desc") ?
                productRepository.findByCategoryOrderByPriceDesc(categoryName, pageable) :
                productRepository.findByCategoryOrderByPriceAsc(categoryName, pageable);
        return productsPage.map(productMapper::mapProductToDTO);
    }

    @Override
    public Page<ProductDTO> getProductsByMultipleCriteria(String categoryName, String collection, Double minPrice, Double maxPrice, Pageable pageable) {
        return productRepository.findProductsByMultipleCriteria(categoryName, collection, minPrice, maxPrice, pageable)
                .map(productMapper::mapProductToDTO);
    }



    public void updateProductPricesOnStartup() {
        List<Products> products = productRepository.findAll();
        for (Products product : products) {
            double totalPrice = calculateTotalPrice(product.getProductId());
            product.setPrice(totalPrice);
            productRepository.save(product);
        }
    }
}
