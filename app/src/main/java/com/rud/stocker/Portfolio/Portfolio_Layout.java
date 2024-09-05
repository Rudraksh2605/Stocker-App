package com.rud.stocker.Portfolio;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.rud.stocker.Order.Order_Layout;
import com.rud.stocker.R;
import com.rud.stocker.Settings.Setting_Layout;
import com.rud.stocker.home.Home_Layout;
import com.rud.stocker.market.MarketLayoutActivity;

public class Portfolio_Layout extends AppCompatActivity {

    private ImageButton marketButton;
    private ImageButton orderButton;
    private ImageButton settingButton;
    private ImageButton homebtn;
    private ImageButton backbtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.portfolio_layout);

        marketButton = findViewById(R.id.market_button);
        orderButton = findViewById(R.id.order_button);
        settingButton = findViewById(R.id.setting_button);
        backbtn = findViewById(R.id.back_button);
        homebtn = findViewById(R.id.home_button);

        marketButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MarketLayoutActivity.class));
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

        homebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Home_Layout.class));
            }
        });

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Home_Layout.class));
            }
        });


    }
}
