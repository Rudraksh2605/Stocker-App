package com.rud.stocker.Order;

public class order_item {
    private String symbol;
    private String quantity;
    private String totalAmount;
    private String action;

    public order_item(String symbol, String quantity, String totalAmount, String action) {
        this.symbol = symbol;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.action = action;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public String getAction() {
        return action;
    }
}
