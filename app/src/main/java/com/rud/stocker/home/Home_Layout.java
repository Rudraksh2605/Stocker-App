package com.rud.stocker.home;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rud.stocker.GainerLoser.LoserClass;
import com.rud.stocker.GainerLoser.LossList;
import com.rud.stocker.GainerLoser.TopGainerList;
import com.rud.stocker.Order.Order_Layout;
import com.rud.stocker.Portfolio.Portfolio_Layout;
import com.rud.stocker.R;
import com.rud.stocker.Settings.Setting_Layout;
import com.rud.stocker.market.MarketLayoutActivity;

import java.util.ArrayList;
import java.util.List;

public class Home_Layout extends AppCompatActivity {

    private ImageButton marketButton;
    private ImageButton portfolioButton;
    private ImageButton orderButton;
    private ImageButton settingButton;
    private RecyclerView topGainerRecyclerView;
    private RecyclerView topLoserRecyclerView;
    private FirebaseDatabase db;
    private List<Stock> topGainerList;
    private List<Stock> topLoserList;
    private StockAdapter stockAdapter;
    private LossStockAdapter lossStockAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_layout);

        View belowGainer2 = findViewById(R.id.below_gainer_2);
        View belowLoser2 = findViewById(R.id.below_loser_2);

        View belowLoser3 = findViewById(R.id.below_loser_1);
        TextView stockName3Loser = findViewById(R.id.stock_name_3);
        TextView percentageLoss3 = findViewById(R.id.percentage_gain_3);

        belowLoser2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Home_Layout.this, LossList.class);
                startActivity(intent);
            }
        });


        belowGainer2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Home_Layout.this, TopGainerList.class);
                startActivity(intent);
            }
        });

        marketButton = findViewById(R.id.market_button);
        portfolioButton = findViewById(R.id.portfolio_button);
        orderButton = findViewById(R.id.order_button);
        settingButton = findViewById(R.id.setting_button);

        marketButton.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), MarketLayoutActivity.class)));
        portfolioButton.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), Portfolio_Layout.class)));
        orderButton.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), Order_Layout.class)));
        settingButton.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), Setting_Layout.class)));

        // Top Gainers
        topGainerRecyclerView = findViewById(R.id.TopGainer);
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        topGainerRecyclerView.setLayoutManager(horizontalLayoutManager);

        topGainerList = new ArrayList<>();
        stockAdapter = new StockAdapter(topGainerList);
        topGainerRecyclerView.setAdapter(stockAdapter);

        // Top Losers
        topLoserRecyclerView = findViewById(R.id.TopLoser);
        LinearLayoutManager horizontalLayoutManager2 = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        topLoserRecyclerView.setLayoutManager(horizontalLayoutManager2);

        topLoserList = new ArrayList<>();
        lossStockAdapter = new LossStockAdapter(topLoserList);
        topLoserRecyclerView.setAdapter(lossStockAdapter);

        db = FirebaseDatabase.getInstance();
        DatabaseReference topGainersRef = db.getReference("TopGainers");
        DatabaseReference topLosersRef = db.getReference("TopLosers");

        LoserClass loss = new LoserClass();
        loss.fetchAndSaveLosers();

        // Top Gainers Data Fetch
        topGainersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                topGainerList.clear();
                int count = 0;
                String thirdStockName = "";
                String thirdStockGain = "";

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Long percentageGain = dataSnapshot.getValue(Long.class);
                    String stockName = dataSnapshot.getKey();

                    if (percentageGain != null) {
                        if (count < 2) {
                            topGainerList.add(new Stock(stockName, percentageGain.toString() + "%"));
                        } else if (count == 2) {
                            thirdStockName = stockName;
                            thirdStockGain = percentageGain.toString() + "%";
                            break;
                        }
                        count++;
                    }
                }

                TextView stockName3 = findViewById(R.id.stock_name_1);
                TextView percentageGain3 = findViewById(R.id.percentage_gain_1);

                stockName3.setText(thirdStockName);
                percentageGain3.setText(thirdStockGain);

                stockAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to read top gainers", error.toException());
            }
        });


        topLosersRef.addValueEventListener(new ValueEventListener() {

            String thirdStockNameLoser = "";
            String thirdStockLoss = "";
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                topLoserList.clear();
                int count = 0;

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Long percentageLoss = dataSnapshot.getValue(Long.class);
                    String stockName = dataSnapshot.getKey();

                    if (percentageLoss != null && count < 2) {
                        topLoserList.add(new Stock(stockName, percentageLoss.toString() + "%"));
                        count++;
                    } else if (count == 2) {
                        thirdStockNameLoser = stockName;
                        thirdStockLoss = percentageLoss.toString() + "%";
                        break;

                    }
                }

                stockName3Loser.setText(thirdStockNameLoser);
                percentageLoss3.setText(thirdStockLoss);

                lossStockAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to read top losers", error.toException());
            }
        });
    }

}
