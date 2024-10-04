package com.rud.stocker.api;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rud.stocker.GainerLoser.FirebaseUpdater;
import com.rud.stocker.GainerLoser.TopGainerCalculator;
import com.rud.stocker.GainerLoser.FirebaseStockUpdater;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiDataRetrieval {
    private Retrofit retrofit;
    private ApiInterface apiInterface;
    private Timer currentPriceTimer;
    private Timer closingPriceTimer;
    private ApiDataCallback callback;
    private FirebaseUpdater firebaseUpdater;
    private FirebaseStockUpdater firebaseStockUpdater;
    private TopGainerCalculator topGainerCalculator;
    private DatabaseReference dbRef;

    public ApiDataRetrieval() {
        String baseUrl = "https://script.googleusercontent.com/";

        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiInterface = retrofit.create(ApiInterface.class);
        firebaseUpdater = new FirebaseUpdater();
        firebaseStockUpdater = new FirebaseStockUpdater();
        topGainerCalculator = new TopGainerCalculator();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        dbRef = database.getReference();
    }


    public void startRealtimeUpdates(ApiDataCallback callback) {
        this.callback = callback;


        startCurrentPriceUpdates();


        scheduleClosingPriceUpdate();
    }


    private void startCurrentPriceUpdates() {
        currentPriceTimer = new Timer();
        currentPriceTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                fetchDataForCurrentPrices();
            }
        }, 0, 60000);
    }


    private void fetchDataForCurrentPrices() {
        Call<List<ApiResponseItem>> call = apiInterface.getData();
        call.enqueue(new Callback<List<ApiResponseItem>>() {
            @Override
            public void onResponse(Call<List<ApiResponseItem>> call, Response<List<ApiResponseItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ApiResponseItem> data = response.body();
                    Map<String, String> currentPrices = new HashMap<>();

                    for (ApiResponseItem item : data) {
                        currentPrices.put(item.getNameOfCompany(), item.getCurrentPrice());
                    }


                    firebaseUpdater.updateCurrentPrices(currentPrices);


                    if (callback != null) {
                        callback.onDataFetched(data);
                    }


                    calculateTopGainers(currentPrices);
                } else {
                    Log.e("ApiDataRetrieval Failed", "Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<ApiResponseItem>> call, Throwable t) {
                Log.e("ApiDataRetrieval", "Error: " + t.getMessage());
            }
        });
    }


    private void scheduleClosingPriceUpdate() {
        closingPriceTimer = new Timer();


        closingPriceTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d("ClosingPriceUpdate", "Updating Closing Prices...");
                fetchDataForClosingPrices();
            }
        }, getNextClosingPriceUpdateTime(), 24 * 60 * 60 * 1000);
    }



    private void fetchDataForClosingPrices() {
        Call<List<ApiResponseItem>> call = apiInterface.getData();
        call.enqueue(new Callback<List<ApiResponseItem>>() {
            @Override
            public void onResponse(Call<List<ApiResponseItem>> call, Response<List<ApiResponseItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ApiResponseItem> data = response.body();
                    Map<String, String> closingPrices = new HashMap<>();

                    for (ApiResponseItem item : data) {
                        closingPrices.put(item.getNameOfCompany(), item.getCurrentPrice());
                    }


                    firebaseStockUpdater.updateClosingPrices(closingPrices);
                } else {
                    Log.e("ApiDataRetrieval", "Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<ApiResponseItem>> call, Throwable t) {
                Log.e("ApiDataRetrieval", "Error: " + t.getMessage());
            }
        });
    }


    private void calculateTopGainers(Map<String, String> currentPrices) {
        dbRef.child("ClosingPrice").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                Map<String, String> closingPrices = (Map<String, String>) task.getResult().getValue();
                if (closingPrices != null) {

                    Map<String, Double> topGainers = topGainerCalculator.calculateTopGainers(closingPrices, currentPrices);


                    dbRef.child("TopGainers").setValue(topGainers).addOnCompleteListener(updateTask -> {
                        if (updateTask.isSuccessful()) {
                            Log.d("TopGainersUpdate", "Top Gainers updated successfully.");
                        } else {
                            Log.e("TopGainersUpdate", "Failed to update Top Gainers.");
                        }
                    });
                } else {
                    Log.e("TopGainerCalculator", "Closing Prices not found.");
                }
            } else {
                Log.e("TopGainerCalculator", "Error fetching Closing Prices: " + task.getException());
            }
        });
    }

    private long getNextClosingPriceUpdateTime() {
        Calendar now = Calendar.getInstance();
        Calendar tweleveAM = (Calendar) now.clone();
        tweleveAM.set(Calendar.HOUR_OF_DAY, 16);
        tweleveAM.set(Calendar.MINUTE, 00);
        tweleveAM.set(Calendar.SECOND, 0);

        if (now.after(tweleveAM)) {
            return now.getTimeInMillis();
        } else {
            return tweleveAM.getTimeInMillis() - now.getTimeInMillis();
        }
    }



    public interface ApiDataCallback {
        void onDataFetched(List<ApiResponseItem> apiDataModels);
    }
}
