package com.rud.stocker.Portfolio;

import android.app.Dialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rud.stocker.Database.database;
import com.rud.stocker.Order.order_item;
import com.rud.stocker.R;
import com.rud.stocker.api.ApiDataRetrieval;
import com.rud.stocker.api.ApiResponseItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PortfolioAdapter extends RecyclerView.Adapter<PortfolioAdapter.ViewHolder> {

    private List<PortfolioItem> portfolioItems;
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private List<order_item> ordersList = new ArrayList<>(); // To store orders




    public PortfolioAdapter(List<PortfolioItem> portfolioItems) {
        this.portfolioItems = portfolioItems;
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.portfolio_item, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PortfolioItem item = portfolioItems.get(position);
        holder.companyName.setText(item.getCompanyName());
        double buyPrice = Double.parseDouble(item.getBuyPrice());
        holder.buyPrice.setText("Buy Price: ₹" + " " + String.format("%.2f", buyPrice));

        int quantity = Integer.parseInt(item.getQuantity());
        holder.quantity.setText("Quantity: " + quantity);

        double profitPerStock = Double.parseDouble(item.getProfitPerStock());
        holder.profitPerStock.setText("Profit: ₹" + " " + String.format("%.2f", profitPerStock));

        double totalAmount = Double.parseDouble(item.getTotalAmount());
        holder.totalAmount.setText("Invested: ₹" + " " + String.format("%.2f", totalAmount));

        holder.itemView.setOnClickListener(v -> {
            // Create a dialog to show stock details as a popup
            Dialog dialog = new Dialog(v.getContext());
            LayoutInflater inflater = LayoutInflater.from(v.getContext());

            // Inflate the custom layout for the dialog
            View dialogView = inflater.inflate(R.layout.activity_stock_details, null);

            // Set stock details into the dialog
            TextView dialogSymbol = dialogView.findViewById(R.id.symbolTextView);
            TextView dialogName = dialogView.findViewById(R.id.nameTextView);
            TextView dialogPrice = dialogView.findViewById(R.id.priceTextView); // Price field
            EditText dialogQuantity = dialogView.findViewById(R.id.quantityEditText);
            TextView dialogTotalAmount = dialogView.findViewById(R.id.total_amount);
            Button buyButton = dialogView.findViewById(R.id.buyButton);
            Button sellButton = dialogView.findViewById(R.id.sellButton);

            // Set values in the dialog
            dialogSymbol.setText(item.getSymbol());
            dialogName.setText(item.getCompanyName());

            // Fetch current stock price from ApiDataRetrieval and display in dialogPrice
            ApiDataRetrieval apiDataRetrieval = new ApiDataRetrieval();
            apiDataRetrieval.startRealtimeUpdates(new ApiDataRetrieval.ApiDataCallback() {
                @Override
                public void onDataFetched(List<ApiResponseItem> apiDataModels) {
                    // Loop through API response to find the stock with the correct symbol
                    for (ApiResponseItem apiResponseItem : apiDataModels) {
                        if (apiResponseItem.getSymbol().equalsIgnoreCase(item.getSymbol())) {
                            String currentPrice = String.format("%.2f", apiResponseItem.getCurrentPrice());
                            dialogPrice.setText("Current Price: ₹" + currentPrice);
                            break;
                        }
                    }
                }
            });

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
            buyButton.setOnClickListener(v1 -> {
                performStockAction(v1, "buy", item.getCompanyName(), dialogPrice.getText().toString(), dialogQuantity.getText().toString(), dialogTotalAmount.getText().toString());
                dialog.dismiss(); // Close the dialog after action
            });

            // Sell button logic
            sellButton.setOnClickListener(v12 -> {
                performStockAction(v12, "sell", item.getCompanyName(), dialogPrice.getText().toString(), dialogQuantity.getText().toString(), dialogTotalAmount.getText().toString());
                dialog.dismiss(); // Close the dialog after action
            });


            dialog.setContentView(dialogView);
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_card_background);
            dialog.show();
        });
    }

    private String calculateTotalAmount(EditText quantityEditText, TextView priceTextView, TextView totalAmountTextView) {
        String quantityStr = quantityEditText.getText().toString();
        String totalAmountStr = "0";
        if (!quantityStr.isEmpty()) {
            int quantityValue = Integer.parseInt(quantityStr);
            double priceValue = Double.parseDouble(priceTextView.getText().toString());
            double totalAmount = priceValue * quantityValue;
            totalAmountTextView.setText(String.format("%.2f", totalAmount));
            totalAmountStr = String.format("%.2f", totalAmount);
        } else {
            totalAmountTextView.setText("");
        }

        return totalAmountStr;
    }

    private void performStockAction(View view, String action, String companyName, String buyPrice, String quantity, String totalAmount) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            int newQuantity = Integer.parseInt(quantity);
            double newBuyPrice = Double.parseDouble(buyPrice);
            double newTotalAmount = Double.parseDouble(totalAmount);

            ApiDataRetrieval apiDataRetrieval = new ApiDataRetrieval();
            apiDataRetrieval.startRealtimeUpdates(new ApiDataRetrieval.ApiDataCallback() {
                @Override
                public void onDataFetched(List<ApiResponseItem> apiDataModels) {
                    for (ApiResponseItem apiResponseItem : apiDataModels) {
                        if (apiResponseItem.getNameOfCompany().equalsIgnoreCase(companyName)) {
                            String symbol = apiResponseItem.getSymbol();

                            executeStockAction(view, action, symbol, companyName, newBuyPrice, newQuantity, newTotalAmount, email);
                            ordersList.add(new order_item(symbol, String.valueOf(newQuantity), String.valueOf(newTotalAmount), action));

                            Log.d("Executed","ufff");

                            break;
                        }
                    }
                    Log.d("Out","Out of loop");
                }
            });
        }
    }

    private void executeStockAction(View view, String action, String symbol, String companyName, double newBuyPrice, int newQuantity, double newTotalAmount, String email) {
        DocumentReference userDocRef = firebaseFirestore.collection("users").document(email);

        userDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Double balance = documentSnapshot.getDouble("Balance");
                if (balance == null) {
                    Log.e("Firestore", "Balance field is missing");
                    return;
                }

                if ("buy".equals(action)) {
                    if (newTotalAmount > balance) {
                        Toast.makeText(view.getContext(), "Insufficient balance to buy the stock", Toast.LENGTH_SHORT).show();
                        return;
                    }
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
                        int existingQuantity = Integer.parseInt((String) existingStock.get("quantity"));
                        double existingBuyPrice = Double.parseDouble((String) existingStock.get("BuyPrice"));
                        double existingTotalAmount = Double.parseDouble((String) existingStock.get("totalAmount"));

                        int updatedQuantity = existingQuantity + newQuantity;
                        double updatedBuyPrice = ((existingBuyPrice * existingQuantity) + (newBuyPrice * newQuantity)) / updatedQuantity;
                        double updatedTotalAmount = existingTotalAmount + newTotalAmount;

                        existingStock.put("quantity", String.valueOf(updatedQuantity));
                        existingStock.put("BuyPrice", String.valueOf(updatedBuyPrice));
                        existingStock.put("totalAmount", String.valueOf(updatedTotalAmount));

                        userDocRef.update("Portfolio", portfolio)
                                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Stock quantity and buy price updated"))
                                .addOnFailureListener(e -> Log.e("Firestore", "Error updating stock", e));

                        userDocRef.update("Orders", ordersList)
                                .addOnSuccessListener(aVoid -> Log.d("Order","Order Updated"))
                                .addOnFailureListener(e -> Log.e("Order","Failed Oder"));




                    } else {
                        Map<String, Object> newStock = new HashMap<>();
                        newStock.put("symbol", symbol);
                        newStock.put("companyName", companyName);
                        newStock.put("quantity", String.valueOf(newQuantity));
                        newStock.put("BuyPrice", String.valueOf(newBuyPrice));
                        newStock.put("totalAmount", String.valueOf(newTotalAmount));

                        userDocRef.update("Portfolio", FieldValue.arrayUnion(newStock))
                                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Stock bought and added to portfolio"))
                                .addOnFailureListener(e -> Log.e("Firestore", "Error adding stock", e));
                    }

                    userDocRef.update("Balance", balance)
                            .addOnSuccessListener(aVoid -> Log.d("Firestore", "Balance updated"))
                            .addOnFailureListener(e -> Log.e("Firestore", "Error updating balance", e));

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

                        int enteredQuantity = newQuantity;
                        double currentPrice = existingTotalAmount / existingQuantity;
                        int updatedQuantity = existingQuantity - enteredQuantity;
                        double updatedTotalAmount = updatedQuantity * currentPrice;

                        double newBalance = balance + enteredQuantity * currentPrice;

                        if (updatedQuantity > 0) {
                            existingStock.put("quantity", String.valueOf(updatedQuantity));
                            existingStock.put("totalAmount", String.valueOf(updatedTotalAmount));

                            userDocRef.update("Portfolio", portfolio)
                                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "Stock sold and updated"))
                                    .addOnFailureListener(e -> Log.e("Firestore", "Error updating stock", e));
                        } else {
                            userDocRef.update("Portfolio", FieldValue.arrayRemove(existingStock))
                                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "Stock fully sold and removed"))
                                    .addOnFailureListener(e -> Log.e("Firestore", "Error removing stock", e));
                        }

                        userDocRef.update("Balance", newBalance)
                                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Balance updated after selling"))
                                .addOnFailureListener(e -> Log.e("Firestore", "Error updating balance after selling", e));
                    }
                }
            }
        }).addOnFailureListener(e -> Log.e("Firestore", "Error fetching user document", e));
    }

    @Override
    public int getItemCount() {
        return portfolioItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView companyName, buyPrice, quantity, totalAmount, profitPerStock;

        ViewHolder(View itemView) {
            super(itemView);
            companyName = itemView.findViewById(R.id.company_name);
            buyPrice = itemView.findViewById(R.id.buy_price);
            quantity = itemView.findViewById(R.id.quantity);
            totalAmount = itemView.findViewById(R.id.total_amount);
            profitPerStock = itemView.findViewById(R.id.profit_per_stock);
        }
    }
}
