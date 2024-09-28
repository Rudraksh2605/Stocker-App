package com.rud.stocker.Portfolio;

public class PortfolioItem {
    private String companyName;
    private String buyPrice;
    private String previousDayPrice;
    private String quantity;
    private String totalAmount;
    private String profitPerStock;
    private String symbol;

    public PortfolioItem(String companyName, String buyPrice, String previousDayPrice, String quantity, String totalAmount, String profitPerStock, String symbol) {
        this.companyName = companyName;
        this.buyPrice = buyPrice;
        this.previousDayPrice = previousDayPrice;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.profitPerStock = profitPerStock;
        this.symbol = symbol;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getBuyPrice() {
        return buyPrice;
    }

    public String getPreviousDayPrice() {
        return previousDayPrice;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public String getProfitPerStock() {
        return profitPerStock;
    }

    public String getSymbol() { return symbol; }

}
