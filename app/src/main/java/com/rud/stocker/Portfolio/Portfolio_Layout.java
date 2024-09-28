package com.rud.stocker.Portfolio;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rud.stocker.Database.database;
import com.rud.stocker.Order.Order_Layout;
import com.rud.stocker.R;
import com.rud.stocker.Settings.Setting_Layout;
import com.rud.stocker.home.Home_Layout;
import com.rud.stocker.market.MarketLayoutActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Portfolio_Layout extends AppCompatActivity {

    private static final String TAG = "Portfolio_Layout";

    private ImageButton marketButton, orderButton, settingButton, homebtn, backbtn;
    private TextView Current, Invested, Total_Return, Profit, one_day_return, balanceView, PortfolioProfit;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private Handler handler = new Handler();
    private Runnable periodicUpdate;



    private RecyclerView recyclerView;
    private PortfolioAdapter portfolioAdapter;
    private List<PortfolioItem> portfolioItems;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.portfolio_layout);


        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.portfolio_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        portfolioItems = new ArrayList<>();
        portfolioAdapter = new PortfolioAdapter(portfolioItems);
        recyclerView.setAdapter(portfolioAdapter);

        marketButton = findViewById(R.id.market_button);
        orderButton = findViewById(R.id.order_button);
        settingButton = findViewById(R.id.setting_button);
        homebtn = findViewById(R.id.home_button);
        backbtn = findViewById(R.id.back_button);

        Current = findViewById(R.id.text_value_port);
        Invested = findViewById(R.id.text_invested_amnt);
        Total_Return = findViewById(R.id.text_current_amnt);
        Profit = findViewById(R.id.text_profit_amnt);
        one_day_return = findViewById(R.id.text_1D_return_amnt);
        balanceView = findViewById(R.id.text_balance_amnt);
        PortfolioProfit = findViewById(R.id.text_portfolio_profit_amnt);


        fetchUserData();


        setupButtons();

        periodicUpdate = new Runnable() {
            @Override
            public void run() {
                fetchUserData(); // Fetch and update the data
                handler.postDelayed(this, 30000); // Schedule next update in 30 seconds
            }
        };
        handler.post(periodicUpdate);
    }

    private void fetchUserData() {
        String emailId = auth.getCurrentUser().getEmail();
        Log.d("Email",emailId);

        db.collection("users").document(emailId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {

                        updateTextViews(documentSnapshot);
                    } else {
                        Log.d(TAG, "No data found for user: " + emailId);
                    }
                })
                .addOnFailureListener(e -> Log.d("Fetch", "Error fetching data", e));
    }

    private void updateTextViews(DocumentSnapshot documentSnapshot) {


        long current = Math.round(documentSnapshot.getDouble("Current") != null ? documentSnapshot.getDouble("Current") : 0.0);
        long invested = Math.round(documentSnapshot.getDouble("Invested") != null ? documentSnapshot.getDouble("Invested") : 0.0);
        long totalReturn = Math.round(documentSnapshot.getDouble("Total Return") != null ? documentSnapshot.getDouble("Total Return") : 0.0);
        long profit = Math.round(documentSnapshot.getDouble("Profit") != null ? documentSnapshot.getDouble("Profit") : 0.0);
        long oneDayReturn = Math.round(documentSnapshot.getDouble("1D Return") != null ? documentSnapshot.getDouble("1D Return") : 0.0);
        long balance = Math.round(documentSnapshot.getDouble("Balance") != null ? documentSnapshot.getDouble("Balance") : 0.0);
        String symbol = documentSnapshot.getString("symbol");
        long amt = 1000000;
        long portfolioProfit = (current + balance) - amt ;
        Log.d("Invested", String.valueOf(invested));




        Current.setText("₹" + " " + String.format("%.2f", (double) current));
        Invested.setText("₹" + " " + String.format("%.2f", (double) invested));
        Total_Return.setText(String.format("%.2f", (double) totalReturn) + " " + "%");
        Profit.setText("₹" + " " + String.format("%.2f", (double) profit));
        one_day_return.setText(String.format("%.2f", (double) oneDayReturn) + " " + "%");
        balanceView.setText("₹" + " " + String.format("%.2f", (double) balance));
        PortfolioProfit.setText("₹" + " " + String.format("%.2f", (double) portfolioProfit));




        List<Map<String, Object>> portfolio = (List<Map<String, Object>>) documentSnapshot.get("Portfolio");
        if (portfolio != null) {
            portfolioItems.clear();
            for (Map<String, Object> stock : portfolio) {
                String companyName = (String) stock.get("companyName");
                String buyPrice = (String) stock.get("BuyPrice");
                String previousDayPrice = (String) stock.get("PreviousDayPrice");
                String quantity = (String) stock.get("quantity");
                String totalAmount = (String) stock.get("totalAmount");


                double profitPerStock = stock.get("ProfitPerStock") != null ? (double) stock.get("ProfitPerStock") : 0.0;
                String profitPerStockStr = String.valueOf(profitPerStock);


                portfolioItems.add(new PortfolioItem(companyName, buyPrice, previousDayPrice, quantity, totalAmount, profitPerStockStr,symbol));
            }

            portfolioAdapter.notifyDataSetChanged();
        }
    }

    private void setupButtons() {
        marketButton.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), MarketLayoutActivity.class)));
        orderButton.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), Order_Layout.class)));
        settingButton.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), Setting_Layout.class)));
        homebtn.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), Home_Layout.class)));
        backbtn.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), Home_Layout.class)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop the periodic updates when the activity is destroyed
        handler.removeCallbacks(periodicUpdate);
    }
}
