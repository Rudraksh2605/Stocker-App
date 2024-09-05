package com.rud.stocker.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rud.stocker.Order.Order_Layout;
import com.rud.stocker.Portfolio.Portfolio_Layout;
import com.rud.stocker.R;
import com.rud.stocker.Settings.Setting_Layout;
import com.rud.stocker.api.ApiDataRetrieval;
import com.rud.stocker.api.ApiResponseItem;
import com.rud.stocker.market.MarketLayoutActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

public class Home_Layout extends AppCompatActivity {

    private ImageButton marketButton;
    private ImageButton portfolioButton;
    private ImageButton orderButton;
    private ImageButton settingButton;
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
        settingButton = findViewById(R.id.setting_button);

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
                startActivity(new Intent(getApplicationContext(), Portfolio_Layout.class));
            }
        });

        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the Order activity here
                startActivity(new Intent(getApplicationContext(), Order_Layout.class));
            }
        });
        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Setting_Layout.class));
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
                    if (item.getCurrentPrice() != null && !item.getCurrentPrice().equals("#N/A")) {
                        try {
                            data.put(item.getSymbol(), Double.parseDouble(item.getCurrentPrice()));
                        } catch (NumberFormatException e) {
                            Log.e("Error", "Failed to parse current price: " + e.getMessage());
                        }
                    } else {
                        Log.d("Info", "Current price is not available for " + item.getSymbol());
                    }
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
        topGainerAdapter.setTopGainers(stockChanges);
        List<TopGainer.StockChange> reversedList = new ArrayList<>(stockChanges);
        Collections.reverse(reversedList);
        topLoserAdapter.setStockChanges(reversedList);
    }
}