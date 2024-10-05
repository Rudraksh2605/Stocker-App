package com.rud.stocker.home;

public class Stock {
    private String stockName;
    private String percentageGain;

    // Required empty constructor for Firebase
    public Stock(String stockName, Double percentageChange) {}

    public Stock(String stockName, String percentageGain) {
        this.stockName = stockName;
        this.percentageGain = percentageGain;
    }

    public String getStockName() {
        return stockName;
    }

    public String getPercentageGain() {
        return percentageGain;
    }
}
