package com.rud.stocker.GainerLoser;

public class Stock {
    private String name;
    private String percentChange;

    public Stock() {

    }

    public Stock(String name, String percentChange) {
        this.name = name;
        this.percentChange = percentChange;
    }

    public String getName() {
        return name;
    }

    public String getPercentChange() {
        return percentChange;
    }
}
