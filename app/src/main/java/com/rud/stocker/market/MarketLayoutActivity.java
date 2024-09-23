package com.rud.stocker.market;

import android.app.Dialog;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.rud.stocker.Database.database;
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
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

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

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();


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

            // Set up onClickListener for the item
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Create a dialog to show stock details as a popup
                    Dialog dialog = new Dialog(v.getContext());
                    LayoutInflater inflater = LayoutInflater.from(v.getContext());

                    // Inflate the custom layout for the dialog
                    View dialogView = inflater.inflate(R.layout.activity_stock_details, null);

                    // Set stock details into the dialog
                    TextView dialogSymbol = dialogView.findViewById(R.id.symbolTextView);
                    TextView dialogName = dialogView.findViewById(R.id.nameTextView);
                    TextView dialogPrice = dialogView.findViewById(R.id.priceTextView);
                    EditText dialogQuantity = dialogView.findViewById(R.id.quantityEditText);
                    TextView dialogTotalAmount = dialogView.findViewById(R.id.total_amount);
                    Button buyButton = dialogView.findViewById(R.id.buyButton);
                    Button sellButton = dialogView.findViewById(R.id.sellButton);

                    // Set values in the dialog
                    dialogSymbol.setText(stock.getSymbol());
                    dialogName.setText(stock.getNameOfCompany());
                    dialogPrice.setText(stock.getCurrentPrice());

                    // Set up text watcher to calculate total amount
                    dialogQuantity.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                            // Intentionally left blank
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            calculateTotalAmount(dialogQuantity, dialogPrice, dialogTotalAmount);
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            calculateTotalAmount(dialogQuantity, dialogPrice, dialogTotalAmount);
                        }
                    });

                    // Buy button logic
                    buyButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            performStockAction("buy", stock.getSymbol(), stock.getNameOfCompany(), stock.getCurrentPrice(), dialogQuantity.getText().toString(), dialogTotalAmount.getText().toString());
                            dialog.dismiss(); // Close the dialog after action
                        }
                    });

                    // Sell button logic
                    sellButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            performStockAction("sell", stock.getSymbol(), stock.getNameOfCompany(), stock.getCurrentPrice(), dialogQuantity.getText().toString(), dialogTotalAmount.getText().toString());
                            dialog.dismiss(); // Close the dialog after action
                        }
                    });

                    // Show the dialog
                    dialog.setContentView(dialogView);
                    dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.rounded_card_background));
                    dialog.show();
                }
            });
        }

        private String calculateTotalAmount(EditText quantityEditText, TextView priceTextView, TextView totalAmountTextView) {
            String quantityStr = quantityEditText.getText().toString();
            String strtotalAmount = "0" ;
            if (!quantityStr.isEmpty()) {
                int quantityValue = Integer.parseInt(quantityStr);
                double priceValue = Double.parseDouble(priceTextView.getText().toString());
                double totalAmount = priceValue * quantityValue;
                totalAmountTextView.setText(String.format("%.2f", totalAmount));
                strtotalAmount = String.format("%.2f", totalAmount);;

            } else {
                totalAmountTextView.setText("");
            }

            return strtotalAmount;
        }

        private void performStockAction(String action, String symbol, String companyName, String BuyPrice, String quantity, String totalAmount) {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            database db = new database();

            if (user != null) {
                if (quantity.isEmpty() || Integer.parseInt(quantity) <= 0) {
                    Toast.makeText(MarketLayoutActivity.this, "Quantity must be greater than 0", Toast.LENGTH_SHORT).show();
                    return; // Exit the function without placing the order
                }

                String email = user.getEmail();
                Map<String, Object> stock = new HashMap<>();
                stock.put("symbol", symbol);
                stock.put("companyName", companyName);
                stock.put("BuyPrice", BuyPrice);
                stock.put("quantity", quantity);
                stock.put("totalAmount", totalAmount);

                // Reference to the user's document
                DocumentReference userDocRef = firebaseFirestore.collection("users").document(email);

                // Use set() with merge option to create the document if it doesn't exist
                userDocRef.set(new HashMap<>(), SetOptions.merge())
                        .addOnSuccessListener(aVoid -> {
                            Log.d("Firestore", "User document created or merged successfully");

                            if ("buy".equals(action)) {
                                // Add the stock to the user's portfolio in Firestore
                                userDocRef.update("Portfolio", FieldValue.arrayUnion(stock))
                                        .addOnSuccessListener(aVoid1 -> Log.d("Firestore", "Stock bought and added to portfolio"))
                                        .addOnFailureListener(e -> Log.e("Firestore", "Error adding stock", e));

                            } else if ("sell".equals(action)) {
                                // Remove the stock from the user's portfolio in Firestore
                                userDocRef.update("Portfolio", FieldValue.arrayRemove(stock))
                                        .addOnSuccessListener(aVoid1 -> Log.d("Firestore", "Stock sold and removed from portfolio"))
                                        .addOnFailureListener(e -> Log.e("Firestore", "Error removing stock", e));
                            }
                        })
                        .addOnFailureListener(e -> Log.e("Firestore", "Error creating or merging user document", e));
            }
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
