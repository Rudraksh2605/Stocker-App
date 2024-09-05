package com.rud.stocker.market;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rud.stocker.Order.Order_Layout;
import com.rud.stocker.Portfolio.Portfolio_Layout;
import com.rud.stocker.R;
import com.rud.stocker.Settings.Setting_Layout;
import com.rud.stocker.api.ApiDataRetrieval;
import com.rud.stocker.api.ApiResponseItem;
import com.rud.stocker.home.Home_Layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarketLayoutActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ApiDataRetrieval apiDataRetrieval;
    private StockAdapter stockAdapter;
    private CountDownTimer timer;
    private SearchStock searchStock;
    private List<ApiResponseItem> originalStockList;
    private Map<String, ApiResponseItem> stockMap;
    private boolean isSearching = false;
    private ImageButton homebtn;
    private ImageButton portbtn;
    private ImageButton settingbtn;
    private ImageButton backbtn;
    private ImageButton orderButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.market_layout);

        homebtn = findViewById(R.id.home_button);
        portbtn = findViewById(R.id.portfolio_button);
        settingbtn = findViewById(R.id.setting_button);
        backbtn = findViewById(R.id.back_button);
        orderButton = findViewById(R.id.order_button);

        recyclerView = findViewById(R.id.stock_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        apiDataRetrieval = new ApiDataRetrieval();
        stockAdapter = new StockAdapter();
        recyclerView.setAdapter(stockAdapter);

        searchStock = new SearchStock();

        stockMap = new HashMap<>();

        //Buttons
        homebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Home_Layout.class));
            }
        });

        portbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Portfolio_Layout.class));
            }
        });

        settingbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Setting_Layout.class));
            }
        });

        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the Order activity here
                startActivity(new Intent(getApplicationContext(), Order_Layout.class));
            }
        });



        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Home_Layout.class));
            }
        });






        // Set up the search bar
        EditText searchBar = findViewById(R.id.search_edit_text);
        searchBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = searchBar.getText().toString();
                searchByName(query);
                return true;
            }
            return false;
        });

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    searchByName(s.toString());
                } else {
                    resetSearch();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Blank Intentionally
            }
        });

        // Updating Stock Price
        timer = new CountDownTimer(Long.MAX_VALUE, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (!isSearching) {
                    apiDataRetrieval.startRealtimeUpdates(new ApiDataRetrieval.ApiDataCallback() {
                        @Override
                        public void onDataFetched(List<ApiResponseItem> apiDataModels) {
                            // Original List --> UI Thread
                            runOnUiThread(() -> {
                                if (!isSearching) {
                                    originalStockList = apiDataModels;
                                    stockAdapter.updateStockList(apiDataModels);
                                    updateStockMap(apiDataModels);
                                    Log.e("Updated UI", "No error");
                                }
                            });
                        }


                    });
                }
            }

            @Override
            public void onFinish() {
            }
        }.start();

        // Original List --> Current List
        apiDataRetrieval.startRealtimeUpdates(new ApiDataRetrieval.ApiDataCallback() {
            @Override
            public void onDataFetched(List<ApiResponseItem> apiDataModels) {
                originalStockList = apiDataModels;
                stockAdapter.updateStockList(apiDataModels);
                updateStockMap(apiDataModels);
            }


        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }

    // Search Methods
    private void searchByName(String companyName) {
        isSearching = true;
        List<ApiResponseItem> filteredStocks = searchStock.searchStockByName(stockMap, companyName);

        if (!filteredStocks.isEmpty()) {
            stockAdapter.updateStockList(filteredStocks);
        } else {
            stockAdapter.updateStockList(new ArrayList<>());
            Toast.makeText(this, "No stocks found with the name '" + companyName + "'", Toast.LENGTH_SHORT).show();
        }
    }

    // Reset search
    private void resetSearch() {
        isSearching = false;
        stockAdapter.updateStockList(originalStockList);
    }

    @Override
    public void onBackPressed() {
        if (isSearching) {
            resetSearch();
        } else {
            super.onBackPressed();
        }
    }

    private void updateStockMap(List<ApiResponseItem> apiDataModels) {
        stockMap.clear();
        for (ApiResponseItem stock : apiDataModels) {
            stockMap.put(stock.getNameOfCompany().toLowerCase(), stock);
        }
    }

    // RecyclerView
    private class StockAdapter extends RecyclerView.Adapter<StockAdapter.ViewHolder> {
        private List<ApiResponseItem> stockList;

        public StockAdapter() {
            this.stockList = new ArrayList<>();
        }

        public void updateStockList(List<ApiResponseItem> stockList) {
            this.stockList = stockList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.stock_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ApiResponseItem stock = stockList.get(position);
            holder.symbolTextView.setText(stock.getSymbol());
            holder.nameTextView.setText(stock.getNameOfCompany());
            holder.priceTextView.setText(stock.getCurrentPrice());
        }

        @Override
        public int getItemCount() {
            return stockList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView symbolTextView;
            TextView nameTextView;
            TextView priceTextView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                symbolTextView = itemView.findViewById(R.id.stock_symbol);
                nameTextView = itemView.findViewById(R.id.stock_name);
                priceTextView = itemView.findViewById(R.id.stock_price);
            }
        }
    }
}
