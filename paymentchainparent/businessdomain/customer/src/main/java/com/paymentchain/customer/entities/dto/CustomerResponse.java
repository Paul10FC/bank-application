package com.paymentchain.customer.entities.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(name = "CustomerResponse", description = "Model to response with a customer in the database")
@Data
public class CustomerResponse {
    private String code;
    private String name;
    private String phone;
    private String iban;
    private String surname;
    private String address;
    private Double accountMount;
    private List<ProductsResponse> productsResponseList;
    private List<?> transactionsResponseList;
}
