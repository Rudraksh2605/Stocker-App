package com.rud.stocker.home;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rud.stocker.Order.Order_Layout;
import com.rud.stocker.Portfolio.Portfolio_Layout;
import com.rud.stocker.R;
import com.rud.stocker.Settings.Setting_Layout;
import com.rud.stocker.market.MarketLayoutActivity;

import java.util.HashMap;
import java.util.Map;

public class Home_Layout extends AppCompatActivity {

    private ImageButton marketButton;
    private ImageButton portfolioButton;
    private ImageButton orderButton;
    private ImageButton settingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_layout);

        marketButton = findViewById(R.id.market_button);
        portfolioButton = findViewById(R.id.portfolio_button);
        orderButton = findViewById(R.id.order_button);
        settingButton = findViewById(R.id.setting_button);

        marketButton.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), MarketLayoutActivity.class)));
        portfolioButton.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), Portfolio_Layout.class)));
        orderButton.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), Order_Layout.class)));
        settingButton.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), Setting_Layout.class)));

        fetchAndLogTopGainersAndLosers();
    }

    private void fetchAndLogTopGainersAndLosers() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference closingPriceRef = database.getReference("ClosingPrice");
        DatabaseReference currentPriceRef = database.getReference("CurrentPrice");

        closingPriceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot closingSnapshot) {
                Map<String, String> closingPrices = new HashMap<>();
                for (DataSnapshot snapshot : closingSnapshot.getChildren()) {
                    closingPrices.put(snapshot.getKey(), snapshot.getValue(String.class));
                }

                currentPriceRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot currentSnapshot) {
                        Map<String, String> currentPrices = new HashMap<>();
                        for (DataSnapshot snapshot : currentSnapshot.getChildren()) {
                            currentPrices.put(snapshot.getKey(), snapshot.getValue(String.class));
                        }

                        logTopGainersAndLosers(closingPrices, currentPrices);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e(TAG, "Failed to read Current Prices from Firebase: " + error.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to read Closing Prices from Firebase: " + error.getMessage());
            }
        });
    }

    private void logTopGainersAndLosers(Map<String, String> closingPrices, Map<String, String> currentPrices) {
        if (closingPrices.isEmpty() || currentPrices.isEmpty()) {
            Log.e(TAG, "Closing or Current prices data is empty.");
            return;
        }

        for (String stock : closingPrices.keySet()) {
            if (currentPrices.containsKey(stock)) {
                String closingPriceStr = closingPrices.get(stock);
                String currentPriceStr = currentPrices.get(stock);

                // Check for null, empty, or invalid price data
                if (isValidPrice(closingPriceStr) && isValidPrice(currentPriceStr)) {
                    try {
                        double closingPrice = Double.parseDouble(closingPriceStr.trim());
                        double currentPrice = Double.parseDouble(currentPriceStr.trim());

                        // Calculate percentage change
                        if (closingPrice > 0) {
                            double percentageChange = ((currentPrice - closingPrice) / closingPrice) * 100;

                            // Log the stock and its percentage change
                            Log.d(TAG, "Stock: " + stock + ", Percentage Change: " + percentageChange + "%");
                        } else {
                            Log.e(TAG, "Invalid closing price for stock: " + stock);
                        }
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Error parsing price for stock: " + stock, e);
                    }
                } else {
                    Log.e(TAG, "Invalid or missing price data for stock: " + stock);
                }
            } else {
                Log.e(TAG, "Current price data missing for stock: " + stock);
            }
        }
    }

    // Helper method to check if a price is valid
    private boolean isValidPrice(String priceStr) {
        if (priceStr == null || priceStr.trim().isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(priceStr.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
