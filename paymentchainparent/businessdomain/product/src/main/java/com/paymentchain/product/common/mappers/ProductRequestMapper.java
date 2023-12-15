package com.paymentchain.product.common.mappers;

import com.paymentchain.product.entities.dto.ProductRequest;
import com.paymentchain.product.entities.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductRequestMapper {
    Product productRequestMapperToProduct(ProductRequest product);
}
