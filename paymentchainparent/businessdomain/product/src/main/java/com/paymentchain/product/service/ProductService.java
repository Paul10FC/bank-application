package com.paymentchain.product.service;

import com.paymentchain.product.common.mappers.ProductRequestMapper;
import com.paymentchain.product.common.mappers.ProductResponseMapper;
import com.paymentchain.product.entities.dto.ProductRequest;
import com.paymentchain.product.entities.dto.ProductResponse;
import com.paymentchain.product.entities.Product;
import com.paymentchain.product.respository.ProductRepository;
import com.paymentchain.product.common.exception.BusinessRuleException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    ProductRepository productRepository;
    ProductRequestMapper productRequestMapper;
    ProductResponseMapper productResponseMapper;

    @Autowired
    public ProductService(ProductRepository productRepository, ProductRequestMapper productRequestMapper, ProductResponseMapper productResponseMapper) {
        this.productRepository = productRepository;
        this.productRequestMapper = productRequestMapper;
        this.productResponseMapper = productResponseMapper;

    }

    public Optional<List<ProductResponse>> getAllProducts(){
        List<Product> productList = productRepository.findAll();
        if (productList.isEmpty()){
            return Optional.empty();
        } else {
            List<ProductResponse> responses = this.productResponseMapper.productListToProductResponseList(productList);
            return Optional.of(responses);
        }
    }

    public Optional<ProductResponse> getOneProduct(long id){
        Optional<Product> product = this.productRepository.findById(id);

        return product.map(value -> this.productResponseMapper.productToProductResponse(value));
    }

    public Optional<ProductResponse> updateProduct(Long id, ProductRequest request){
        Optional<Product> find = this.productRepository.findById(id);

        if (find.isPresent()){
            if (request.getCode() != null) find.get().setCode(request.getCode());
            if (request.getName() != null) find.get().setName(request.getName());
            ProductResponse response = this.productResponseMapper.productToProductResponse(find.get());
            Product save = productRepository.save(find.get());

            return Optional.of(response);
        } else {
            return Optional.empty();
        }
    }

    public Optional<ProductResponse> newProduct(ProductRequest request) throws BusinessRuleException {
        if (request.getName().isEmpty() || request.getCode().isEmpty()) {
            throw new BusinessRuleException("0001", "Name and code can't be empty", HttpStatus.PRECONDITION_FAILED);

        } else {
            Product product = this.productRequestMapper.productRequestMapperToProduct(request);
            Product save = productRepository.save(product);
            Optional<Product> find = this.productRepository.findById(save.getId());

            if (find.isPresent()) {
                ProductResponse response = this.productResponseMapper.productToProductResponse(save);
                System.out.println(response);
                return Optional.of(response);
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
