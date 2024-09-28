package com.rud.stocker.Order;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rud.stocker.Portfolio.Portfolio_Layout;
import com.rud.stocker.R;
import com.rud.stocker.Settings.Setting_Layout;
import com.rud.stocker.home.Home_Layout;
import com.rud.stocker.market.MarketLayoutActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Order_Layout extends AppCompatActivity {

    private ImageButton marketButton, orderButton, settingButton, homebtn, backbtn, portfoliobtn;
    private RecyclerView recyclerView;
    private order_adapter orderAdapter;
    private List<order_item> orderItems;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_layout);

        marketButton = findViewById(R.id.market_button);
        orderButton = findViewById(R.id.order_button);
        settingButton = findViewById(R.id.setting_button);
        homebtn = findViewById(R.id.home_button);
        backbtn = findViewById(R.id.back_button);
        portfoliobtn = findViewById(R.id.portfolio_button);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        setupButtons();
        fetchUserData();

        recyclerView = findViewById(R.id.stock_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderItems = new ArrayList<>();
        orderAdapter = new order_adapter(orderItems);
        recyclerView.setAdapter(orderAdapter);
    }

    private void setupButtons() {
        marketButton.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), MarketLayoutActivity.class)));
        orderButton.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), Order_Layout.class)));
        settingButton.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), Setting_Layout.class)));
        homebtn.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), Home_Layout.class)));
        backbtn.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), Home_Layout.class)));
        portfoliobtn.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), Portfolio_Layout.class)));
    }

    private void fetchUserData() {
        String emailId = auth.getCurrentUser().getEmail();
        Log.d("Email", emailId);

        db.collection("users").document(emailId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> orders = (List<Map<String, Object>>) documentSnapshot.get("Orders");
                        if (orders != null) {
                            for (Map<String, Object> orderData : orders) {
                                String symbol = (String) orderData.get("symbol");

                                // Handle quantity
                                Object quantityObj = orderData.get("quantity");
                                String quantity;
                                if (quantityObj instanceof Long) {
                                    quantity = String.valueOf(quantityObj);
                                } else if (quantityObj instanceof String) {
                                    quantity = (String) quantityObj;
                                } else {
                                    quantity = "0"; // Fallback in case of unexpected type
                                }

                                // Handle totalAmount
                                Object totalAmountObj = orderData.get("totalAmount");
                                String totalAmount;
                                if (totalAmountObj instanceof Long) {
                                    totalAmount = String.valueOf(totalAmountObj);
                                } else if (totalAmountObj instanceof String) {
                                    totalAmount = (String) totalAmountObj;
                                } else {
                                    totalAmount = "0"; // Fallback in case of unexpected type
                                }

                                String additionalParam = (String) orderData.get("action");

                                order_item orderItem = new order_item(symbol, quantity, totalAmount, additionalParam);
                                orderItems.add(orderItem);
                            }

                            orderAdapter.notifyDataSetChanged();  // Refresh the adapter
                        }
                    } else {
                        Log.d("Firebase", "No data found for user: " + emailId);
                    }
                })
                .addOnFailureListener(e -> Log.d("Fetch", "Error fetching data", e));
    }
}
