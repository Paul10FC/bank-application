package com.paymentchain.transactions.entities;


public enum Status {
    PENDING(1, "Pending"),
    LIQUIDATED(2, "Liquidated"),
    REJECTED(3, "Rejected"),
    CANCELLED(4, "Cancelled");

    final int code;
    final String status;

    Status(int code, String status){
        this.code = code;
        this.status = status;
    }
}
