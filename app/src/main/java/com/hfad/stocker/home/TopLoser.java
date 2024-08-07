package com.hfad.stocker.home;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.text.DecimalFormat;

public class TopLoser extends TopGainer {
    private List<TopGainer.StockChange> topLoserList;

    public TopLoser() {
        topLoserList = new ArrayList<>();
    }

    public void update(List<TopGainer.StockChange> stockChanges) {
        topLoserList = new ArrayList<>(stockChanges);
        Collections.reverse(topLoserList);
        logTopLosers();
    }

    private void logTopLosers() {

        DecimalFormat df = new DecimalFormat("#.##");

        for (TopGainer.StockChange stockChange : topLoserList) {
            System.out.println("Top Loser: " + stockChange.getSymbol() + ": " + stockChange.getPercentageChange() + "%");
        }
    }

    public List<TopGainer.StockChange> getTopLoserList() {
        return topLoserList;
    }


    public void onDataUpdated(List<TopGainer.StockChange> stockChanges) {
        update(stockChanges);
    }
}
