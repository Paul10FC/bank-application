package com.paymentchain.customer.entities.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.persistence.Transient;

@Data
@Schema(name = "ProductsResponse", description = "Is a response with a products in database")
public class ProductsResponse {
    private long productId;
    private String productName;
}
