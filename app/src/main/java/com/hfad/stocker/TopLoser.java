package com.hfad.stocker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TopLoser implements TopGainer.DataUpdateListener {
    private List<TopGainer.StockChange> topLoserList;

    public TopLoser() {
        topLoserList = new ArrayList<>();
    }

    public void update(List<TopGainer.StockChange> stockChanges) {
        topLoserList = new ArrayList<>(stockChanges);
        Collections.reverse(topLoserList); // Assuming the list is sorted by gain and you want the opposite
        logTopLosers();
    }

    private void logTopLosers() {
        for (TopGainer.StockChange stockChange : topLoserList) {
            System.out.println("Top Loser: " + stockChange.getStock().getSymbol() + ": " + stockChange.getPercentageChange() + "%");
        }
    }

    public List<TopGainer.StockChange> getTopLoserList() {
        return topLoserList;
    }

    @Override
    public void onDataUpdated(List<TopGainer.StockChange> stockChanges) {
        update(stockChanges);
    }
}
