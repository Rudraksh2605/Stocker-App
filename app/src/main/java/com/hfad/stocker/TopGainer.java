package com.hfad.stocker;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TopGainer implements ApiDataRetrieval.ApiDataCallback {
    private List<ApiResponseItem> stockList;
    private Map<String, Double> previousPrices;
    private List<StockChange> stockChanges;
    private DataUpdateListener dataUpdateListener;

    public TopGainer() {
        stockList = new ArrayList<>();
        previousPrices = new HashMap<>();
        stockChanges = new ArrayList<>();
    }

    public void setDataUpdateListener(DataUpdateListener listener) {
        this.dataUpdateListener = listener;
    }

    @Override
    public void onDataFetched(List<ApiResponseItem> apiDataModels) {
        stockList = apiDataModels;
        calculatePercentageChanges();
        sortStocksByPercentageChange();
        if (dataUpdateListener != null) {
            dataUpdateListener.onDataUpdated(stockChanges);
        }
    }

    private void calculatePercentageChanges() {
        stockChanges.clear();

        for (ApiResponseItem stock : stockList) {
            String symbol = stock.getSymbol();
            String currentPriceString = stock.getCurrentPrice();

            // Remove any leading or trailing whitespace
            currentPriceString = currentPriceString.trim();

            try {
                // Check for null, empty, or invalid values
                if (currentPriceString == null || currentPriceString.isEmpty() || currentPriceString.equalsIgnoreCase("#N/A")) {
                    throw new NumberFormatException("Invalid price format");
                }

                double currentPrice = Double.parseDouble(currentPriceString);
                double previousPrice = previousPrices.getOrDefault(symbol, currentPrice);

                double percentageChange = ((currentPrice - previousPrice) / previousPrice) * 100;
                stockChanges.add(new StockChange(stock, percentageChange));

                previousPrices.put(symbol, currentPrice);
            } catch (NumberFormatException e) {
                // Handle the case where the current price is not a valid number
                System.err.println("Invalid price for stock: " + symbol + " with price: " + currentPriceString);
            }
        }
    }

    private void sortStocksByPercentageChange() {
        Collections.sort(stockChanges, new Comparator<StockChange>() {
            @Override
            public int compare(StockChange o1, StockChange o2) {
                return Double.compare(o2.getPercentageChange(), o1.getPercentageChange());
            }
        });

        // Logging the sorted list
        for (StockChange stockChange : stockChanges) {
            Log.e("Sorted List", stockChange.getStock().getSymbol() + ": " + stockChange.getPercentageChange() + "%");
        }
    }


    public interface DataUpdateListener {
        void onDataUpdated(List<StockChange> stockChanges);
    }

    public class StockChange {
        private ApiResponseItem stock;
        private double percentageChange;

        public StockChange(ApiResponseItem stock, double percentageChange) {
            this.stock = stock;
            this.percentageChange = percentageChange;
        }

        public ApiResponseItem getStock() {
            return stock;
        }

        public double getPercentageChange() {
            return percentageChange;
        }
    }
}
