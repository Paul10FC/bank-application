package com.paymentchain.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "Customer Simple Response", description = "A response without products and transactions")
public class CustomerSimpleResponse {
    private String code;
    private String name;
    private String phone;
    private String iban;
    private String surname;
    private String address;
    private Double balance;
}
