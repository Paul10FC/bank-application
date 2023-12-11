package com.paymentchain.billing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(name = "InvoiceResponse", description = "Model represent a invoice on database")
@Data
public class InvoiceResponse {
    @Schema(name = "invoiceId", required = true, example = "2", defaultValue = "1", description = "Unique Id of invoice")
    private long invoiceId;
    @Schema(name = "customer", required = true, example = "2", defaultValue = "1", description = "Unique Id of customer that represent the owner of invoice")
    private long customer;
    private String number;
    private String detail;
    private double amount;
}
