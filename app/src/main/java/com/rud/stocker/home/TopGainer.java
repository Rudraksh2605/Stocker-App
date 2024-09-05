package com.rud.stocker.home;

import java.io.Serializable;
import java.sql.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.rud.stocker.MySqlDatabase.DatabaseUtil;
import com.rud.stocker.api.ApiDataRetrieval;
import com.rud.stocker.api.ApiResponseItem;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.util.Log;

public class TopGainer extends ApiDataRetrieval {
    private LinkedHashMap<String, Double> currentPriceMap;
    private LinkedHashMap<String, Double> previousPriceMap;
    private LinkedHashMap<String, StockChange> topGainerMap;
    private LinkedHashMap<String, StockChange> topLoserMap;
    private ExecutorService executorService;

    public TopGainer() {
        executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> createDatabaseAndTables()); // Create database and tables if they don't exist
    }

    // Method to create database and tables
    private void createDatabaseAndTables() {
        try (Connection connection = DatabaseUtil.getConnection();
             Statement statement = connection.createStatement()) {

            String createCurrentPriceTable = "CREATE TABLE IF NOT EXISTS currentPriceList (symbol VARCHAR(10) PRIMARY KEY, price DOUBLE)";
            statement.execute(createCurrentPriceTable);

            String createClosingPriceTable = "CREATE TABLE IF NOT EXISTS closingPriceList (symbol VARCHAR(10) PRIMARY KEY, price DOUBLE)";
            statement.execute(createClosingPriceTable);

            String createTopGainerTable = "CREATE TABLE IF NOT EXISTS topGainerList (symbol VARCHAR(10) PRIMARY KEY, currentPrice DOUBLE, percentageChange DOUBLE)";
            statement.execute(createTopGainerTable);

            String createTopLoserTable = "CREATE TABLE IF NOT EXISTS topLoserList (symbol VARCHAR(10) PRIMARY KEY, currentPrice DOUBLE, percentageChange DOUBLE)";
            statement.execute(createTopLoserTable);

        } catch (SQLException e) {
            Log.e("TopGainer", "Error creating database or tables", e);
        }
    }

    public void start() {
        Log.d("TopGainer", "Starting the process...");

        // Update prices and calculate top gainers/losers every minute
        Timer minuteTimer = new Timer();
        minuteTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Log.d("TopGainer", "Updating current prices and calculating top gainers/losers...");
                updateCurrentPrices();
            }
        }, 0, 60000); // Update every minute
    }

    public void updateCurrentPrices() {
        Log.d("TopGainer", "Fetching current prices from API...");
        ApiDataRetrieval apiDataRetrieval = new ApiDataRetrieval();
        apiDataRetrieval.startRealtimeUpdates(new ApiDataCallback() {
            @Override
            public void onDataFetched(List<ApiResponseItem> apiDataModels) {
                Log.d("TopGainer", "API data fetched. Size: " + apiDataModels.size());
            }

            public void onDataRetrieved(LinkedHashMap<String, Double> data) {
                Log.d("TopGainer", "Current prices retrieved. Size: " + data.size());
                currentPriceMap = data;
                updateCurrentPricesInDatabase(currentPriceMap);
                calculateTopGainersAndLosers();
            }
        });
    }

    private void updateCurrentPricesInDatabase(LinkedHashMap<String, Double> prices) {
        String query = "REPLACE INTO currentPriceList (symbol, price) VALUES (?, ?)";

        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            for (Map.Entry<String, Double> entry : prices.entrySet()) {
                preparedStatement.setString(1, entry.getKey());
                preparedStatement.setDouble(2, entry.getValue());
                preparedStatement.addBatch();
            }
            int[] affectedRows = preparedStatement.executeBatch();
            Log.d("TopGainer", "Current prices updated in MySQL. Affected rows: " + affectedRows.length);
        } catch (SQLException e) {
            Log.e("TopGainer", "Error updating current prices in MySQL", e);
        }
    }


    private void calculateTopGainersAndLosers() {
        Log.d("TopGainer", "Retrieving closing price list from MySQL...");
        String query = "SELECT symbol, price FROM closingPriceList";

        try (Connection connection = DatabaseUtil.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            previousPriceMap = new LinkedHashMap<>();
            while (resultSet.next()) {
                previousPriceMap.put(resultSet.getString("symbol"), resultSet.getDouble("price"));
            }
            if (previousPriceMap != null && currentPriceMap != null) {
                Log.d("TopGainer", "Calculating top gainers and losers...");
                LinkedHashMap<String, StockChange> priceChangeMap = calculatePriceChangeMap(currentPriceMap, previousPriceMap);
                topGainerMap = sortPriceChangeMap(priceChangeMap, true);
                topLoserMap = sortPriceChangeMap(priceChangeMap, false);
                saveTopListsToDatabase("topGainerList", topGainerMap);
                saveTopListsToDatabase("topLoserList", topLoserMap);
            }
        } catch (SQLException e) {
            Log.e("TopGainer", "Error retrieving closing price list from MySQL", e);
        }
    }

    private void saveTopListsToDatabase(String tableName, LinkedHashMap<String, StockChange> map) {
        String query = "REPLACE INTO " + tableName + " (symbol, currentPrice, percentageChange) VALUES (?, ?, ?)";

        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            for (Map.Entry<String, StockChange> entry : map.entrySet()) {
                preparedStatement.setString(1, entry.getKey());
                preparedStatement.setDouble(2, entry.getValue().getCurrentPrice());
                preparedStatement.setDouble(3, entry.getValue().getPercentageChange());
                preparedStatement.addBatch();
            }
            int[] affectedRows = preparedStatement.executeBatch();
            Log.d("TopGainer", "Top list updated in MySQL: " + tableName + ". Affected rows: " + affectedRows.length);
        } catch (SQLException e) {
            Log.e("TopGainer", "Error saving to MySQL: " + tableName, e);
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
