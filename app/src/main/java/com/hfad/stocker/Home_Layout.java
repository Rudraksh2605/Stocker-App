package com.hfad.stocker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Home_Layout extends AppCompatActivity implements TopGainer.DataUpdateListener {

    private Button marketButton;
    private Button portfolioButton;
    private Button orderButton;
    private RecyclerView topGainerRecyclerView;
    private RecyclerView topLoserRecyclerView;
    private StockAdapter stockAdapter;
    private TopLoserAdapter topLoserAdapter;
    private TopGainer topGainer;
    private TopLoser topLoser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_layout);

        // Initialize the buttons with their corresponding views
        marketButton = findViewById(R.id.market_button);
        portfolioButton = findViewById(R.id.portfolio_button);
        orderButton = findViewById(R.id.order_button);

        // Set the click listeners for each button
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

        // Setup RecyclerView for Top Gainer
        topGainerRecyclerView = findViewById(R.id.TopGainer);
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        topGainerRecyclerView.setLayoutManager(horizontalLayoutManager);
        stockAdapter = new StockAdapter(); // Initialize without passing a list
        topGainerRecyclerView.setAdapter(stockAdapter);

        // Setup RecyclerView for Top Loser
        topLoserRecyclerView = findViewById(R.id.TopLoser);
        LinearLayoutManager horizontalLayoutManager2 = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        topLoserRecyclerView.setLayoutManager(horizontalLayoutManager2);
        topLoserAdapter = new TopLoserAdapter(); // Initialize without passing a list
        topLoserRecyclerView.setAdapter(topLoserAdapter);

        // Initialize TopGainer and start fetching data
        topGainer = new TopGainer();
        topGainer.setDataUpdateListener(this);

        ApiDataRetrieval apiDataRetrieval = new ApiDataRetrieval();
        apiDataRetrieval.startRealtimeUpdates(topGainer);

        // Initialize TopLoser and start fetching data
        topLoser = new TopLoser();
        topLoserAdapter.setStockChanges(topLoser.getTopLoserList()); // Set initial data for top losers
    }

    @Override
    public void onDataUpdated(List<TopGainer.StockChange> stockChanges) {
        stockAdapter.setStockChanges(stockChanges);
        // Update top loser adapter with reversed list (assuming it's already sorted)
        List<TopGainer.StockChange> reversedList = new ArrayList<>(stockChanges);
        Collections.reverse(reversedList);
        topLoserAdapter.setStockChanges(reversedList);
    }
}
