package com.paymentchain.product.common;

import com.paymentchain.product.dto.ProductRequest;
import com.paymentchain.product.entities.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface ProductRequestMapper {
    Product productRequestMapperToProduct(ProductRequest product);
}
