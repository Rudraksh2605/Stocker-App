package com.rud.stocker.GainerLoser;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;

public class TopLoserCalculator {

    public Map<String, Double> calculateTopLosers(Map<String, String> closingPrices, Map<String, String> currentPrices) {
        Map<String, Double> topLosers = new HashMap<>();

        for (String stock : closingPrices.keySet()) {
            String closingPriceStr = closingPrices.get(stock);
            String currentPriceStr = currentPrices.get(stock);

            if (closingPriceStr != null && currentPriceStr != null &&
                    !closingPriceStr.trim().isEmpty() && !currentPriceStr.trim().isEmpty()) {
                try {
                    double closingPrice = Double.parseDouble(closingPriceStr.trim());
                    double currentPrice = Double.parseDouble(currentPriceStr.trim());


                    if (closingPrice > 0) {
                        double percentageChange = ((currentPrice - closingPrice) / closingPrice) * 100;
                        if (percentageChange <= 0) {
                            topLosers.put(stock, percentageChange);
                        }
                    } else {
                        System.out.println("Invalid closing price for stock: " + stock);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid number format for stock: " + stock);
                }
            } else {
                System.out.println("Price data missing or invalid for stock: " + stock);
            }
        }

        return topLosers.entrySet()
                .stream()
                .sorted((entry1, entry2) -> Double.compare(entry1.getValue(), entry2.getValue()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }
}
