package com.paymentchain.product.service;

import com.paymentchain.product.entities.Product;
import com.paymentchain.product.respository.ProductRepository;
import com.paymentchain.product.web.exception.BusinessRuleException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Optional<List<Product>> getAllProducts(){
        List<Product> productList = productRepository.findAll();
        if (productList.isEmpty()){
            return Optional.empty();
        } else {
            return Optional.of(productList);
        }
    }

    public Optional<Product> getOneProduct(long id){
        return this.productRepository.findById(id);
    }

    public Optional<Product> updateProduct(Long id, Product product){
        Optional<Product> find = this.productRepository.findById(id);

        if (find.isPresent()){
            find.get().setCode(product.getCode());
            find.get().setName(product.getName());
            Product save = productRepository.save(find.get());

            return Optional.of(save);
        } else {
            return Optional.empty();
        }
    }

    public Optional<Product> newProduct(Product product) throws BusinessRuleException {
        if (product.getName().isEmpty() || product.getCode().isEmpty()) {
            throw new BusinessRuleException("0001", "Name and code can't be empty", HttpStatus.PRECONDITION_FAILED);
        } else {
            Product save = productRepository.save(product);
            Optional<Product> find = this.productRepository.findById(save.getId());
            if (find.isPresent()) {
                return Optional.of(save);
            } else {
                return Optional.empty();
            }
        }
    }

    public Optional<Product> deleteProduct(Long id){
        Optional<Product> find = this.productRepository.findById(id);
        this.productRepository.delete(find.get());

        return this.productRepository.findById(id);
    }
}
