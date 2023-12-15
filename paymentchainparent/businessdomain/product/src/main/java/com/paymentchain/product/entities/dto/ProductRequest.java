package com.paymentchain.product.entities.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(name = "ProductRequest", description = "Model represent a product on database")
@Data
public class ProductRequest {
    private String code;
    private String name;
}
