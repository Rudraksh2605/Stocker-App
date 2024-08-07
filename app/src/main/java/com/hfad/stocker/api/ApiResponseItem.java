package com.hfad.stocker.api;

import com.google.gson.annotations.SerializedName;

public class ApiResponseItem {
    @SerializedName("Current Price")
    private String currentPrice;
    @SerializedName("Exchange + Symbol")
    private String exchangeSymbol;
    @SerializedName("FACE VALUE")
    private String faceValue;
    @SerializedName("Market Cap")
    private String marketCap;
    @SerializedName("NAME OF COMPANY")
    private String nameOfCompany;
    @SerializedName("PE ")
    private String pe;
    @SerializedName("SYMBOL")
    private String symbol;
    @SerializedName("52 Week High")
    private String weekHigh;
    @SerializedName("52 Week Low")
    private String weekLow;

    // Getters and setters
    public String getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(String currentPrice) {
        this.currentPrice = currentPrice;
    }

    public String getExchangeSymbol() {
        return exchangeSymbol;
    }

    public void setExchangeSymbol(String exchangeSymbol) {
        this.exchangeSymbol = exchangeSymbol;
    }

    public String getFaceValue() {
        return faceValue;
    }

    public void setFaceValue(String faceValue) {
        this.faceValue = faceValue;
    }

    public String getMarketCap() {
        return marketCap;
    }

    public void setMarketCap(String marketCap) {
        this.marketCap = marketCap;
    }

    public String getNameOfCompany() {
        return nameOfCompany;
    }

    public void setNameOfCompany(String nameOfCompany) {
        this.nameOfCompany = nameOfCompany;
    }

    public String getPe() {
        return pe;
    }

    public void setPe(String pe) {
        this.pe = pe;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getWeekHigh() {
        return weekHigh;
    }

    public void setWeekHigh(String weekHigh) {
        this.weekHigh = weekHigh;
    }

    public String getWeekLow() {
        return weekLow;
    }

    public void setWeekLow(String weekLow) {
        this.weekLow = weekLow;
    }
}
