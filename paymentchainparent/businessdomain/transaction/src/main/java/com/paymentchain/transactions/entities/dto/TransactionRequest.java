package com.paymentchain.transactions.dto;

import com.paymentchain.transactions.entities.Channel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(name = "TransactionRequest", description = "Model represent a transaction on database")
@Data
public class TransactionRequest {
    private String accountIban;
    private LocalDateTime date;
    private double amount;
    private String description;
    private Channel channel;
}
