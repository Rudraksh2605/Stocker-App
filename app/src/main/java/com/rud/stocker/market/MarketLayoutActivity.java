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
                            runOnUiThread(() -> {
                                originalStockList = apiDataModels;
                                stockAdapter.updateStockList(apiDataModels);
                                updateStockMap(apiDataModels);
                                Log.e("Updated UI", "No error");
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
                            dialog.dismiss();
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

            if (user != null) {
                if (quantity.isEmpty() || Integer.parseInt(quantity) <= 0) {
                    Toast.makeText(MarketLayoutActivity.this, "Quantity must be greater than 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                final String email = user.getEmail();
                final int newQuantity = Integer.parseInt(quantity);
                final double newBuyPrice = Double.parseDouble(BuyPrice);
                final double newTotalAmount = Double.parseDouble(totalAmount);

                final Map<String, Object> orderEntry = new HashMap<>();
                orderEntry.put("action", action);
                orderEntry.put("symbol", symbol);
                orderEntry.put("companyName", companyName);
                orderEntry.put("quantity", quantity);
                orderEntry.put("totalAmount", totalAmount);

                final DocumentReference userDocRef = firebaseFirestore.collection("users").document(email);

                userDocRef.get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Double balance = documentSnapshot.getDouble("Balance");
                        if (balance == null) {
                            Log.e("Firestore", "Balance field is missing");
                            return;
                        }

                        if ("buy".equals(action)) {
                            if (newTotalAmount > balance) {
                                Toast.makeText(MarketLayoutActivity.this, "Insufficient balance to buy the stock", Toast.LENGTH_SHORT).show();
                                return;
                            } else {
                                balance -= newTotalAmount;

                                List<Map<String, Object>> portfolio = (List<Map<String, Object>>) documentSnapshot.get("Portfolio");
                                Map<String, Object> existingStock = null;

                                if (portfolio != null) {
                                    for (Map<String, Object> stockEntry : portfolio) {
                                        if (stockEntry.get("symbol").equals(symbol)) {
                                            existingStock = stockEntry;
                                            break;
                                        }
                                    }
                                }

                                if (existingStock != null) {
                                    int existingQuantity = Integer.parseInt(existingStock.get("quantity").toString());
                                    double existingBuyPrice = Double.parseDouble(existingStock.get("BuyPrice").toString());
                                    double existingTotalAmount = Double.parseDouble(existingStock.get("totalAmount").toString());

                                    int updatedQuantity = existingQuantity + newQuantity;
                                    double updatedBuyPrice = ((existingBuyPrice * existingQuantity) + (newBuyPrice * newQuantity)) / updatedQuantity;
                                    double updatedTotalAmount = existingTotalAmount + newTotalAmount;

                                    existingStock.put("quantity", String.valueOf(updatedQuantity));
                                    existingStock.put("BuyPrice", String.valueOf(updatedBuyPrice));
                                    existingStock.put("totalAmount", String.valueOf(updatedTotalAmount));

                                    userDocRef.update("Portfolio", portfolio)
                                            .addOnSuccessListener(aVoid -> Log.d("Firestore", "Stock quantity and buy price updated"))
                                            .addOnFailureListener(e -> Log.e("Firestore", "Error updating stock", e));
                                } else {
                                    Map<String, Object> newStock = new HashMap<>();
                                    newStock.put("symbol", symbol);
                                    newStock.put("companyName", companyName);
                                    newStock.put("quantity", quantity);
                                    newStock.put("BuyPrice", BuyPrice);
                                    newStock.put("totalAmount", totalAmount);

                                    userDocRef.update("Portfolio", FieldValue.arrayUnion(newStock))
                                            .addOnSuccessListener(aVoid -> Log.d("Firestore", "Stock bought and added to portfolio"))
                                            .addOnFailureListener(e -> Log.e("Firestore", "Error adding stock", e));
                                }

                                userDocRef.update("Balance", balance)
                                        .addOnSuccessListener(aVoid -> Log.d("Firestore", "Balance updated"))
                                        .addOnFailureListener(e -> Log.e("Firestore", "Error updating balance", e));

                                userDocRef.update("Orders", FieldValue.arrayUnion(orderEntry))
                                        .addOnSuccessListener(aVoid -> Log.d("Firestore", "Order added to Orders"))
                                        .addOnFailureListener(e -> Log.e("Firestore", "Error adding order", e));
                            }
                        } else if ("sell".equals(action)) {
                            List<Map<String, Object>> portfolio = (List<Map<String, Object>>) documentSnapshot.get("Portfolio");
                            Map<String, Object> existingStock = null;

                            if (portfolio != null) {
                                for (Map<String, Object> stockEntry : portfolio) {
                                    if (stockEntry.get("symbol").equals(symbol)) {
                                        existingStock = stockEntry;
                                        break;
                                    }
                                }
                            }

                            if (existingStock != null) {
                                int existingQuantity = Integer.parseInt(existingStock.get("quantity").toString());
                                double existingTotalAmount = Double.parseDouble(existingStock.get("totalAmount").toString());

                                int enteredQuantity = Integer.parseInt(quantity);
                                double currentPrice = existingTotalAmount / existingQuantity;
                                int updatedQuantity = existingQuantity - enteredQuantity;
                                double updatedTotalAmount = currentPrice * updatedQuantity;

                                if (existingQuantity >= enteredQuantity) {
                                    if (updatedQuantity == 0) {
                                        userDocRef.update("Portfolio", FieldValue.arrayRemove(existingStock))
                                                .addOnSuccessListener(aVoid -> {
                                                    userDocRef.update("Balance", FieldValue.increment(existingTotalAmount))
                                                            .addOnSuccessListener(balanceVoid -> {
                                                                Log.d("Firestore", "Stock sold, removed from portfolio, and balance updated");
                                                                Intent intent = new Intent(MarketLayoutActivity.this, MarketLayoutActivity.class);
                                                                startActivity(intent);
                                                                finish();
                                                            })
                                                            .addOnFailureListener(e -> Log.e("Firestore", "Error updating balance", e));
                                                })
                                                .addOnFailureListener(e -> Log.e("Firestore", "Error removing stock from portfolio", e));
                                    } else {
                                        existingStock.put("quantity", String.valueOf(updatedQuantity));
                                        existingStock.put("totalAmount", String.valueOf(updatedTotalAmount));

                                        userDocRef.update("Portfolio", portfolio)
                                                .addOnSuccessListener(aVoid -> {
                                                    userDocRef.update("Balance", FieldValue.increment(existingTotalAmount - updatedTotalAmount))
                                                            .addOnSuccessListener(balanceVoid -> {
                                                                Log.d("Firestore", "Balance updated successfully");
                                                                Intent intent = new Intent(MarketLayoutActivity.this, MarketLayoutActivity.class);
                                                                startActivity(intent);
                                                                finish();
                                                            })
                                                            .addOnFailureListener(e -> Log.e("Firestore", "Error updating balance", e));
                                                })
                                                .addOnFailureListener(e -> Log.e("Firestore", "Error updating stock in portfolio", e));
                                    }

                                    userDocRef.update("Orders", FieldValue.arrayUnion(orderEntry))
                                            .addOnSuccessListener(aVoid -> Log.d("Firestore", "Order added to Orders array"))
                                            .addOnFailureListener(e -> Log.e("Firestore", "Error adding order", e));
                                } else {
                                    Log.e("Firestore", "Entered quantity is greater than available quantity");
                                }
                            } else {
                                Log.e("Firestore", "Stock does not exist in portfolio to sell");
                            }
                        }
                    }
                }).addOnFailureListener(e -> Log.e("Firestore", "Error fetching user document", e));
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
