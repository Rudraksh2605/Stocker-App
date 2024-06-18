
package com.hfad.stocker;

import android.util.Log;

import java.util.List;
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
    private Timer timer;
    private ApiDataCallback callback;

    public ApiDataRetrieval() {
        String baseUrl = "https://script.googleusercontent.com/";

        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiInterface = retrofit.create(ApiInterface.class);
    }

    public void startRealtimeUpdates(ApiDataCallback callback) {
        this.callback = callback;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                fetchData();
            }
        }, 0, 1000);
    }

    public void stopRealtimeUpdates() {
        if (timer!= null) {
            timer.cancel();
            timer = null;
        }
    }

    private void fetchData() {
        Call<List<ApiResponseItem>> call = apiInterface.getData();
        call.enqueue(new Callback<List<ApiResponseItem>>() {
            @Override
            public void onResponse(Call<List<ApiResponseItem>> call, Response<List<ApiResponseItem>> response) {
                if (response.isSuccessful()) {
                    List<ApiResponseItem> data = response.body();
                    fetchDataWithCallback(data);
                    Log.e("Updated JSON", "Retrieved Successfully");

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

    private void fetchDataWithCallback(List<ApiResponseItem> data) {
        if (callback!= null) {
            callback.onDataFetched(data);
        }
    }

    public interface ApiDataCallback {
        void onDataFetched(List<ApiResponseItem> apiDataModels);
    }
}