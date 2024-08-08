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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rud.stocker.api.ApiDataRetrieval;
import com.rud.stocker.api.ApiResponseItem;

import android.util.Log;

public class TopGainer extends ApiDataRetrieval {
    private FirebaseDatabase database;
    private LinkedHashMap<String, Double> currentPriceMap;
    private LinkedHashMap<String, Double> previousPriceMap;
    private LinkedHashMap<String, StockChange> topGainerMap;
    private LinkedHashMap<String, StockChange> topLoserMap;

    public TopGainer() {
        database = FirebaseDatabase.getInstance();
    }

    public void start() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                saveClosingPriceList();
            }
        }, getClosingTime());

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                calculateTopGainersAndLosers();
            }
            }, getOpeningTime());

        Timer minuteTimer = new Timer();
        minuteTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateCurrentPrices();
            }
        }, 0, 60000);
    }

    public void updateCurrentPrices() {
        ApiDataRetrieval apiDataRetrieval = new ApiDataRetrieval();
        apiDataRetrieval.startRealtimeUpdates(new ApiDataCallback() {

            @Override
            public void onDataFetched(List<ApiResponseItem> apiDataModels) {

            }

            public void onDataRetrieved(LinkedHashMap<String, Double> data) {
                currentPriceMap = data;
                database.getReference("currentPriceList").setValue(currentPriceMap);
            }
        });
    }

    private void saveClosingPriceList() {
        ApiDataRetrieval apiDataRetrieval = new ApiDataRetrieval();
        apiDataRetrieval.startRealtimeUpdates(new ApiDataCallback() {
            @Override
            public void onDataFetched(List<ApiResponseItem> apiDataModels) {

            }

            public void onDataFetched(LinkedHashMap<String, Double> data) {
                currentPriceMap = data;
                database.getReference("closingPriceList").setValue(currentPriceMap);
            }
        });
    }

    private void calculateTopGainersAndLosers() {
        database.getReference("closingPriceList").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                previousPriceMap = (LinkedHashMap<String, Double>) dataSnapshot.getValue();
                if (previousPriceMap != null && currentPriceMap != null) {
                    LinkedHashMap<String, StockChange> priceChangeMap = calculatePriceChangeMap(currentPriceMap, previousPriceMap);
                    topGainerMap = sortPriceChangeMap(priceChangeMap, true);
                    topLoserMap = sortPriceChangeMap(priceChangeMap, false);
                    database.getReference("topGainerList").setValue(topGainerMap);
                    database.getReference("topLoserList").setValue(topLoserMap);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("TopGainer", "Error retrieving closing price list: " + databaseError.getMessage());
            }
        });
    }

    private LinkedHashMap<String, StockChange> calculatePriceChangeMap(LinkedHashMap<String, Double> currentPriceMap, LinkedHashMap<String, Double> previousPriceMap) {
        LinkedHashMap<String, StockChange> priceChangeMap = new LinkedHashMap<>();

        for (Map.Entry<String, Double> currentPriceEntry : currentPriceMap.entrySet()) {
            String symbol = currentPriceEntry.getKey();
            Double currentPrice = currentPriceEntry.getValue();
            Double previousPrice = previousPriceMap.get(symbol);
            if (previousPrice != null) {
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
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    private long getClosingTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 16);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTimeInMillis();
    }

    private long getOpeningTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 15);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTimeInMillis();
    }


//    public void onDataUpdated(List<StockChange> stockChanges) {
//        // Handle data update
//    }

    public static class StockChange implements Serializable {
        private String symbol;
        private double currentPrice;
        private double percentageChange;

        public StockChange(String symbol, double currentPrice, double percentageChange) {
            this.symbol = symbol;
            this.currentPrice = currentPrice;
            this.percentageChange = percentageChange;
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
