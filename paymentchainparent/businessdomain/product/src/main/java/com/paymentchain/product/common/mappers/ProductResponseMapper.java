package com.paymentchain.product.common;

import com.paymentchain.product.dto.ProductResponse;
import com.paymentchain.product.entities.Product;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductResponseMapper {
    ProductResponse productToProductResponse(Product product);
    List<ProductResponse> productListToProductResponseList(List<Product> products);
}
