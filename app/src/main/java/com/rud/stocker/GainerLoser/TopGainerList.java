package com.rud.stocker.GainerLoser;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rud.stocker.R;
import java.util.ArrayList;
import java.util.List;

public class TopGainerList extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TopGainerAdapter adapter;
    private List<Stock> stockList;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_gainer);

        recyclerView = findViewById(R.id.stock_list_gainer);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        stockList = new ArrayList<>();
        adapter = new TopGainerAdapter(stockList);
        recyclerView.setAdapter(adapter);


        databaseReference = FirebaseDatabase.getInstance().getReference("TopGainers");


        fetchTopGainers();
    }

    private void fetchTopGainers() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                stockList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    String stockName = snapshot.getKey();
                    Double percentChange = snapshot.getValue(Double.class);

                    Log.d("StockData", "StockName: " + stockName + ", PercentChange: " + percentChange);

                    if (stockName != null && percentChange != null) {
                        Stock stock = new Stock(stockName, String.valueOf(percentChange));
                        stockList.add(stock);
                    }
                }
                adapter.notifyDataSetChanged();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
