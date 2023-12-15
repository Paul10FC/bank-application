package com.paymentchain.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(name = "CustomerRequest", description = "Model represent a customer on database")
@Data
public class CustomerRequest {
    @Schema(name = "code", required = true, example = "321", defaultValue = "1", description = "Unique code that represents the customer number")
    private String code;
    private String name;
    private String phone;
    private String iban;
    private String surname;
    private String address;
    private Double accountMount;
    @Schema(name = "products", required = false, example = "[1]", description = "List of customer products")
    List<Long> products;
}
