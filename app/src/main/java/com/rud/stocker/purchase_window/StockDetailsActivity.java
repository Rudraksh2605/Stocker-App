package com.rud.stocker.purchase_window;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.rud.stocker.R;

public class StockDetailsActivity extends AppCompatActivity {

    private TextView symbolTextView;
    private TextView nameTextView;
    private     TextView priceTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_details);

        // Initialize your views
        symbolTextView = findViewById(R.id.symbolTextView);
        nameTextView = findViewById(R.id.nameTextView);
        priceTextView = findViewById(R.id.priceTextView);

        // Retrieve the data passed from the previous activity
        Intent intent = getIntent();
        String symbol = intent.getStringExtra("symbol");
        String companyName = intent.getStringExtra("companyName");
        String currentPrice = intent.getStringExtra("currentPrice");

        // Set the data to the views
        symbolTextView.setText(symbol);
        nameTextView.setText(companyName);
        priceTextView.setText(currentPrice);
    }
}
