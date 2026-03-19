package com.ecommerce.product.service;

import com.ecommerce.product.entity.Product;
import java.util.List;

public interface ProductService {

    Product createProduct(Product product);

    Product getProductById(Long productId);

    List<Product> getAllProducts();

    Product updateProduct(Long productId, Product product);

    void deleteProduct(Long productId);
}
