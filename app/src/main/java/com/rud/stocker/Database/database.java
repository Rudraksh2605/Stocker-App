package com.rud.stocker.Database;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rud.stocker.api.ApiDataRetrieval;
import com.rud.stocker.api.ApiResponseItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class database {
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public void Database() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public void createUserData() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String emailId = currentUser.getEmail();
            Log.d("Email", emailId);


            db.collection("users")
                    .document(emailId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (!documentSnapshot.exists()) {

                            Map<String, Object> userData = new HashMap<>();
                            userData.put("Profit", 0);
                            userData.put("Current", 0);
                            userData.put("Total Return", 0);
                            userData.put("1D Return", 0);
                            userData.put("Invested", 0);
                            userData.put("Balance", 1000000);
                            userData.put("Portfolio", new ArrayList<>());
                            userData.put("PortfolioProfit",0);
                            userData.put("Orders", new ArrayList<>());

                            db.collection("users")
                                    .document(emailId)
                                    .set(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("Database", "User data successfully written!");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("Database", "Error writing user data: " + e.getMessage());
                                    });
                        } else {
                            Log.d("Database", "User data already exists, not creating new data.");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Database", "Error checking user data: " + e.getMessage());
                    });
        }
    }


    public void updateTotalAmount() {
        Log.d("Update", "Update method called");
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String emailId = currentUser.getEmail();

            db.collection("users")
                    .document(emailId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            List<Map<String, Object>> portfolio = (List<Map<String, Object>>) documentSnapshot.get("Portfolio");
                            if (portfolio != null && !portfolio.isEmpty()) {
                                double totalInvested = 0;
                                for (Map<String, Object> item : portfolio) {
                                    if (item.containsKey("totalAmount")) {
                                        String totalAmountStr = (String) item.get("totalAmount");
                                        totalInvested += Double.parseDouble(totalAmountStr);
                                        Log.d("Total Amount", totalAmountStr);
                                    } else {
                                        Log.e("Total Amount", "Item does not contain 'totalAmount' key.");
                                    }
                                }


                                Map<String, Object> updates = new HashMap<>();
                                updates.put("Invested", totalInvested);

                                db.collection("users")
                                        .document(emailId)
                                        .update(updates)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("Database", "Invested amount updated successfully!");
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("Database", "Error updating Invested amount: " + e.getMessage());
                                        });
                            } else {

                                Map<String, Object> updates = new HashMap<>();
                                updates.put("Invested", 0);

                                db.collection("users")
                                        .document(emailId)
                                        .update(updates)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("Database", "Invested amount set to 0 for empty portfolio!");
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("Database", "Error setting Invested amount to 0: " + e.getMessage());
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Database", "Error retrieving user data: " + e.getMessage());
                    });
        }
    }

    public void updateCurrent() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String emailId = currentUser.getEmail();

            db.collection("users")
                    .document(emailId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            List<Map<String, Object>> portfolio = (List<Map<String, Object>>) documentSnapshot.get("Portfolio");
                            if (portfolio != null && !portfolio.isEmpty()) {
                                double totalCurrent = 0;
                                for (Map<String, Object> item : portfolio) {
                                    if (item.containsKey("currentPrice")) {
                                        String currentPriceStr = (String) item.get("currentPrice");
                                        totalCurrent += Double.parseDouble(currentPriceStr);
                                        Log.d("Current Amount", currentPriceStr);
                                    } else {
                                        Log.e("Current Amount", "Item does not contain 'currentPrice' key.");
                                    }
                                }


                                Map<String, Object> updates = new HashMap<>();
                                updates.put("Current", totalCurrent);

                                db.collection("users")
                                        .document(emailId)
                                        .update(updates)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("Database", "Current amount updated successfully!");
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("Database", "Error updating Current amount: " + e.getMessage());
                                        });
                            } else {

                                Map<String, Object> updates = new HashMap<>();
                                updates.put("Current", 0);

                                db.collection("users")
                                        .document(emailId)
                                        .update(updates)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("Database", "Current amount set to 0 for empty portfolio!");
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("Database", "Error setting Current amount to 0: " + e.getMessage());
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Database", "Error retrieving user data: " + e.getMessage());
                    });
        }
    }

    public void updateProfit() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String emailId = currentUser.getEmail();

            db.collection("users")
                    .document(emailId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            List<Map<String, Object>> portfolio = (List<Map<String, Object>>) documentSnapshot.get("Portfolio");
                            if (portfolio != null && !portfolio.isEmpty()) {
                                List<String> stockSymbols = new ArrayList<>();
                                for (Map<String, Object> item : portfolio) {
                                    if (item.containsKey("symbol")) {
                                        stockSymbols.add((String) item.get("symbol"));
                                    }
                                }

                                ApiDataRetrieval api = new ApiDataRetrieval();
                                api.startRealtimeUpdates(new ApiDataRetrieval.ApiDataCallback() {
                                    @Override
                                    public void onDataFetched(List<ApiResponseItem> apiDataModels) {
                                        double totalProfit = 0;
                                        double totalCurrentValue = 0;
                                        double totalInvested = 0;
                                        double total1DReturn = 0;

                                        for (Map<String, Object> stockItem : portfolio) {
                                            String symbol = (String) stockItem.get("symbol");
                                            String quantityStr = (String) stockItem.get("quantity");
                                            double quantity = Double.parseDouble(quantityStr);
                                            String investedStr = (String) stockItem.get("totalAmount");
                                            double invested = Double.parseDouble(investedStr);
                                            totalInvested += invested;

                                            for (ApiResponseItem apiData : apiDataModels) {
                                                if (symbol.equals(apiData.getSymbol())) {
                                                    double currentPrice = Double.parseDouble(apiData.getCurrentPrice());
                                                    double previousDayPrice = 0.0;


                                                    if (stockItem.containsKey("PreviousDayPrice")) {
                                                        previousDayPrice = Double.parseDouble((String) stockItem.get("PreviousDayPrice"));
                                                    } else {
                                                        previousDayPrice = currentPrice;
                                                    }


                                                    stockItem.put("PreviousDayPrice", String.valueOf(currentPrice));


                                                    double currentValue = currentPrice * quantity;
                                                    double profitPerStock = currentValue - invested;
                                                    totalProfit += profitPerStock;


                                                    stockItem.put("Profit", totalProfit);
                                                    stockItem.put("ProfitPerStock", profitPerStock);
                                                    totalCurrentValue += currentValue;


                                                    double oneDayReturn = 0;
                                                    if (previousDayPrice > 0) {
                                                        oneDayReturn = ((currentPrice - previousDayPrice) / previousDayPrice) * 100;

                                                        oneDayReturn = Math.round(oneDayReturn * 100.0) / 100.0;
                                                    }
                                                    stockItem.put("1D", oneDayReturn);


                                                    total1DReturn += oneDayReturn * quantity;

                                                    Log.d("1D Return Stock", String.valueOf(oneDayReturn));
                                                    Log.d("Current Value", String.valueOf(currentValue));
                                                    break;
                                                }
                                            }
                                        }


                                        double totalReturnPercentage = 0;
                                        if (totalInvested > 0) {
                                            totalReturnPercentage = ((totalCurrentValue - totalInvested) / totalInvested) * 100;

                                            totalReturnPercentage = Math.round(totalReturnPercentage * 100.0) / 100.0;
                                        }


                                        double total1DReturnPercentage = 0;
                                        if (totalInvested > 0) {
                                            total1DReturnPercentage = (total1DReturn / totalInvested) * 100;

                                            total1DReturnPercentage = Math.round(total1DReturnPercentage * 100.0) / 100.0;
                                        }


                                        Map<String, Object> updates = new HashMap<>();
                                        updates.put("Profit", totalProfit);
                                        updates.put("Current", totalCurrentValue);
                                        updates.put("Total Return", totalReturnPercentage);
                                        updates.put("1D Return", total1DReturnPercentage);
                                        updates.put("Portfolio", portfolio);
                                        db.collection("users")
                                                .document(emailId)
                                                .update(updates)
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d("Database", "Profit, Current value, Total Return, 1 Day Return, and ProfitPerStock updated successfully!");
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("Database", "Error updating Profit, Current value, Total Return, 1 Day Return, and ProfitPerStock: " + e.getMessage());
                                                });
                                    }
                                });

                            } else {

                                Map<String, Object> updates = new HashMap<>();
                                updates.put("Profit", 0);
                                updates.put("Invested", 0);
                                updates.put("Current", 0);
                                updates.put("Total Return", 0);
                                updates.put("1D Return", 0);

                                db.collection("users")
                                        .document(emailId)
                                        .update(updates)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("Database", "Profit, Invested, Current, Total Return, and 1 Day Return set to 0 for empty portfolio!");
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("Database", "Error setting Profit, Invested, Current, Total Return, and 1 Day Return to 0: " + e.getMessage());
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Database", "Error retrieving user data: " + e.getMessage());
                    });
        }
    }




}
