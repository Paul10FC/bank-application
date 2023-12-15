package com.paymentchain.product.common.mappers;

import com.paymentchain.product.entities.dto.ProductResponse;
import com.paymentchain.product.entities.Product;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductResponseMapper {
    ProductResponse productToProductResponse(Product product);
    List<ProductResponse> productListToProductResponseList(List<Product> products);
}
