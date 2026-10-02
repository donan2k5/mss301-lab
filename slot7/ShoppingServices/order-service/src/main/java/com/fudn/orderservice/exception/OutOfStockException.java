package com.fudn.orderservice.exception;

public class OutOfStockException extends RuntimeException {

    public OutOfStockException(String skuCode) {
        super("Product with Skucode " + skuCode + " is not in stock");
    }
}
