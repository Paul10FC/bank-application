package com.paymentchain.transactions.entities.dto;

import com.paymentchain.transactions.entities.Channel;
import com.paymentchain.transactions.entities.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(name = "TransactionResponse", description = "Model used to response to petitions")
@Data
public class TransactionResponse {
    private String reference;
    private String accountIban;
    private LocalDateTime date;
    private double amount;
    private double fee;
    private String description;
    private Status status;
    private Channel channel;
}
