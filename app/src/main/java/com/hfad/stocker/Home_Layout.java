package com.hfad.stocker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class Home_Layout extends AppCompatActivity implements TopGainer.DataUpdateListener {

    private Button marketButton;
    private Button portfolioButton;
    private Button orderButton;
    private RecyclerView topGainerRecyclerView;
    private StockAdapter stockAdapter;
    private TopGainer topGainer;

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

        // Setup RecyclerView
        topGainerRecyclerView = findViewById(R.id.TopGainer);
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        topGainerRecyclerView.setLayoutManager(horizontalLayoutManager);
        stockAdapter = new StockAdapter(); // Initialize without passing a list
        topGainerRecyclerView.setAdapter(stockAdapter);

        // Initialize TopGainer and start fetching data
        topGainer = new TopGainer();
        topGainer.setDataUpdateListener(this);

        ApiDataRetrieval apiDataRetrieval = new ApiDataRetrieval();
        apiDataRetrieval.startRealtimeUpdates(topGainer);
    }

    @Override
    public void onDataUpdated(List<TopGainer.StockChange> stockChanges) {
        stockAdapter.setStockChanges(stockChanges);
    }
}
