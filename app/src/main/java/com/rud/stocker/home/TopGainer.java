package com.rud.stocker.home;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.rud.stocker.api.ApiDataRetrieval;
import com.rud.stocker.api.ApiResponseItem;

import android.util.Log;

public class TopGainer extends ApiDataRetrieval {
    private FirebaseFirestore firestore;
    private LinkedHashMap<String, Double> currentPriceMap;
    private LinkedHashMap<String, Double> previousPriceMap;
    private LinkedHashMap<String, StockChange> topGainerMap;
    private LinkedHashMap<String, StockChange> topLoserMap;

    public TopGainer() {
        firestore = FirebaseFirestore.getInstance();
    }

    public void start() {
        Log.d("TopGainer", "Starting the process...");

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d("TopGainer", "Saving closing prices...");
                saveClosingPriceList();
            }
        }, getClosingTime());

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d("TopGainer", "Calculating top gainers and losers...");
                calculateTopGainersAndLosers();
            }
        }, getOpeningTime());

        Timer minuteTimer = new Timer();
        minuteTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Log.d("TopGainer", "Updating current prices...");
                updateCurrentPrices();
            }
        }, 0, 60000);
    }


    public void updateCurrentPrices() {
        Log.d("TopGainer", "Fetching current prices from API...");
        ApiDataRetrieval apiDataRetrieval = new ApiDataRetrieval();
        apiDataRetrieval.startRealtimeUpdates(new ApiDataCallback() {
            @Override
            public void onDataFetched(List<ApiResponseItem> apiDataModels) {
                Log.d("TopGainer", "API data fetched.");
            }

            public void onDataRetrieved(LinkedHashMap<String, Double> data) {
                Log.d("TopGainer", "Current prices retrieved.");
                currentPriceMap = data;
                firestore.collection("currentPriceList").document("prices").set(currentPriceMap)
                        .addOnSuccessListener(aVoid -> Log.d("TopGainer", "Current prices updated in Firestore"))
                        .addOnFailureListener(e -> Log.e("TopGainer", "Error updating current prices", e));
            }
        });
    }


    private void saveClosingPriceList() {
        Log.d("TopGainer", "Fetching closing prices from API...");
        ApiDataRetrieval apiDataRetrieval = new ApiDataRetrieval();
        apiDataRetrieval.startRealtimeUpdates(new ApiDataCallback() {
            @Override
            public void onDataFetched(List<ApiResponseItem> apiDataModels) {
                Log.d("TopGainer", "API data fetched.");
            }

            public void onDataFetched(LinkedHashMap<String, Double> data) {
                Log.d("TopGainer", "Closing prices retrieved.");
                currentPriceMap = data;
                firestore.collection("closingPriceList").document("prices").set(currentPriceMap)
                        .addOnSuccessListener(avoid -> Log.d("TopGainer", "Closing prices saved in Firestore"))
                        .addOnFailureListener(e -> Log.e("TopGainer", "Error saving closing prices", e));
            }
        });
    }


    private void calculateTopGainersAndLosers() {
        Log.d("TopGainer", "Retrieving closing price list from Firestore...");
        firestore.collection("closingPriceList").document("prices").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d("TopGainer", "Closing price list retrieved.");
                            Map<String, Object> data = document.getData();
                            previousPriceMap = new LinkedHashMap<>();

                            if (data != null) {
                                for (Map.Entry<String, Object> entry : data.entrySet()) {
                                    if (entry.getValue() instanceof Number) {
                                        previousPriceMap.put(entry.getKey(), ((Number) entry.getValue()).doubleValue());
                                    } else {
                                        Log.e("TopGainer", "Invalid data type for " + entry.getKey());
                                    }
                                }
                            }

                            if (previousPriceMap != null && currentPriceMap != null) {
                                Log.d("TopGainer", "Calculating top gainers and losers...");
                                LinkedHashMap<String, StockChange> priceChangeMap = calculatePriceChangeMap(currentPriceMap, previousPriceMap);
                                topGainerMap = sortPriceChangeMap(priceChangeMap, true);
                                topLoserMap = sortPriceChangeMap(priceChangeMap, false);
                                saveTopListsToFirestore("topGainerList", topGainerMap);
                                saveTopListsToFirestore("topLoserList", topLoserMap);
                            }
                        }
                    } else {
                        Log.e("TopGainer", "Error retrieving closing price list", task.getException());
                    }
                });
    }



    private void saveTopListsToFirestore(String collectionName, LinkedHashMap<String, StockChange> map) {
        Log.d("TopGainer", "Saving " + collectionName + " to Firestore...");
        CollectionReference collectionRef = firestore.collection(collectionName);
        for (Map.Entry<String, StockChange> entry : map.entrySet()) {
            Log.d("TopGainer", "Saving " + entry.getKey() + " to " + collectionName);
            collectionRef.document(entry.getKey()).set(entry.getValue())
                    .addOnSuccessListener(aVoid -> Log.d("TopGainer", entry.getKey() + " saved in " + collectionName))
                    .addOnFailureListener(e -> Log.e("TopGainer", "Error saving " + entry.getKey() + " in " + collectionName, e));
        }
    }


    private LinkedHashMap<String, StockChange> calculatePriceChangeMap(LinkedHashMap<String, Double> currentPriceMap, LinkedHashMap<String, Double> previousPriceMap) {
        LinkedHashMap<String, StockChange> priceChangeMap = new LinkedHashMap<>();

        for (Map.Entry<String, Double> currentPriceEntry : currentPriceMap.entrySet()) {
            String symbol = currentPriceEntry.getKey();
            Double currentPrice = currentPriceEntry.getValue();
            Double previousPrice = previousPriceMap.get(symbol);
            if (previousPrice != null) {
                Log.d("TopGainer", "Calculating price change for " + symbol);
                double percentageChange = calculatePercentageChange(currentPrice, previousPrice);
                StockChange stockChange = new StockChange(symbol, currentPrice, percentageChange);
                priceChangeMap.put(symbol, stockChange);
            }
        }

        return priceChangeMap;
    }


    private double calculatePercentageChange(double currentPrice, double previousPrice) {
        return ((currentPrice - previousPrice) / previousPrice) * 100;
    }

    private LinkedHashMap<String, StockChange> sortPriceChangeMap(LinkedHashMap<String, StockChange> priceChangeMap, boolean isAscending) {
        Log.d("TopGainer", "Sorting price change map " + (isAscending ? "ascending" : "descending") + "...");
        List<Map.Entry<String, StockChange>> list = new LinkedList<>(priceChangeMap.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, StockChange>>() {
            @Override
            public int compare(Map.Entry<String, StockChange> o1, Map.Entry<String, StockChange> o2) {
                if (isAscending) {
                    return Double.compare(o1.getValue().getPercentageChange(), o2.getValue().getPercentageChange());
                } else {
                    return Double.compare(o2.getValue().getPercentageChange(), o1.getValue().getPercentageChange());
                }
            }
        });

        LinkedHashMap<String, StockChange> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, StockChange> entry : list) {
            Log.d("TopGainer", "Sorted: " + entry.getKey() + " with change: " + entry.getValue().getPercentageChange());
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        Log.d("TopGainer", "Sorting completed.");
        return sortedMap;
    }


    private long getClosingTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 16);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long closingTime = calendar.getTimeInMillis();
        Log.d("TopGainer", "Calculated closing time: " + closingTime);
        return closingTime;
    }


    private long getOpeningTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 15);
        calendar.set(Calendar.SECOND, 0);
        long openingTime = calendar.getTimeInMillis();
        Log.d("TopGainer", "Calculated opening time: " + openingTime);
        return openingTime;
    }


    public static class StockChange implements Serializable {
        private String symbol;
        private double currentPrice;
        private double percentageChange;

        public StockChange(String symbol, double currentPrice, double percentageChange) {
            this.symbol = symbol;
            this.currentPrice = currentPrice;
            this.percentageChange = percentageChange;
            Log.d("TopGainer", "Created StockChange: Symbol=" + symbol + ", CurrentPrice=" + currentPrice + ", PercentageChange=" + percentageChange);
        }

        public String getSymbol() {
            return symbol;
        }

        public double getCurrentPrice() {
            return currentPrice;
        }

        public double getPercentageChange() {
            return percentageChange;
        }
    }

}
