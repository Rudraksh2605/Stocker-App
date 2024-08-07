package com.hfad.stocker.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hfad.stocker.R;
import com.hfad.stocker.api.ApiDataRetrieval;
import com.hfad.stocker.api.ApiResponseItem;
import com.hfad.stocker.market.MarketLayoutActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

public class Home_Layout extends AppCompatActivity {

    private Button marketButton;
    private Button portfolioButton;
    private Button orderButton;
    private RecyclerView topGainerRecyclerView;
    private RecyclerView topLoserRecyclerView;
    private TopGainerAdapter topGainerAdapter;
    private TopLoserAdapter topLoserAdapter;
    private TopGainer topGainer;
    private TopLoser topLoser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_layout);

        marketButton = findViewById(R.id.market_button);
        portfolioButton = findViewById(R.id.portfolio_button);
        orderButton = findViewById(R.id.order_button);

        marketButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MarketLayoutActivity.class));
            }
        });

        portfolioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the Portfolio activity here
            }
        });

        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the Order activity here
            }
        });

        topGainerRecyclerView = findViewById(R.id.TopGainer);
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        topGainerAdapter = new TopGainerAdapter(new ArrayList<>());
        topGainerRecyclerView.setLayoutManager(horizontalLayoutManager);
        topGainerRecyclerView.setAdapter(topGainerAdapter);

        topLoserRecyclerView = findViewById(R.id.TopLoser);
        LinearLayoutManager horizontalLayoutManager2 = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        topLoserRecyclerView.setLayoutManager(horizontalLayoutManager2);
        topLoserAdapter = new TopLoserAdapter();
        topLoserRecyclerView.setAdapter(topLoserAdapter);

        topGainer = new TopGainer();
        topGainer.start();  // Start data fetching process

        ApiDataRetrieval apiDataRetrieval = new ApiDataRetrieval();
        apiDataRetrieval.startRealtimeUpdates(new ApiDataRetrieval.ApiDataCallback() {
            @Override
            public void onDataFetched(List<ApiResponseItem> apiDataModels) {
                // Convert ApiResponseItem to LinkedHashMap<String, Double>
                LinkedHashMap<String, Double> data = new LinkedHashMap<>();
                for (ApiResponseItem item : apiDataModels) {
                    data.put(item.getSymbol(), Double.parseDouble(item.getCurrentPrice()));
                }
                topGainer.updateCurrentPrices();
            }

//            @Override
//            public void onDataRetrieved(LinkedHashMap<String, Double> data) {
//                topGainer.updateCurrentPrices(data);
//            }
        });

        topLoser = new TopLoser();
    }

    public void onDataUpdated(List<TopGainer.StockChange> stockChanges) {
        topGainerAdapter.setStockChanges(stockChanges);
        List<TopGainer.StockChange> reversedList = new ArrayList<>(stockChanges);
        Collections.reverse(reversedList);
        topLoserAdapter.setStockChanges(reversedList);
    }
}