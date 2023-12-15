package com.paymentchain.product.entities.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(name = "ProductResponse", description = "Model represent a response of products api")
@Data
public class ProductResponse {
    private String code;
    private String name;
}
