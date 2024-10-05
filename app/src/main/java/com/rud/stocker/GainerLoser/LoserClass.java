package com.rud.stocker.GainerLoser;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class LoserClass {

    private DatabaseReference databaseReference;
    private TopLoserCalculator topLoserCalculator;

    public LoserClass() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        topLoserCalculator = new TopLoserCalculator();
    }

    public void fetchAndSaveLosers() {

        Log.d("Loss", "Losssss");

        DatabaseReference closingPriceRef = databaseReference.child("ClosingPrice");
        DatabaseReference currentPriceRef = databaseReference.child("CurrentPrice");

        closingPriceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot closingPriceSnapshot) {
                if (closingPriceSnapshot.exists()) {

                    Map<String, String> closingPrices = (Map<String, String>) closingPriceSnapshot.getValue();
                    Log.d("Mapped", closingPrices.toString());


                    currentPriceRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot currentPriceSnapshot) {
                            if (currentPriceSnapshot.exists()) {
                                Map<String, String> currentPrices = (Map<String, String>) currentPriceSnapshot.getValue();
                                Log.d("Mapped Curr", currentPrices.toString());


                                calculateAndSaveTopLosers(closingPrices, currentPrices);
                            } else {
                                Log.e("LoserClass", "No current price data found");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e("LoserClass", "Error fetching current prices: " + databaseError.getMessage());
                        }
                    });

                } else {
                    Log.e("LoserClass", "No closing price data found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("LoserClass", "Error fetching closing prices: " + databaseError.getMessage());
            }
        });
    }

    private void calculateAndSaveTopLosers(Map<String, String> closingPrices, Map<String, String> currentPrices) {
        Map<String, Double> topLosers = topLoserCalculator.calculateTopLosers(closingPrices, currentPrices);

        if (topLosers.isEmpty()) {
            System.out.println("No top losers to update.");
            return;
        }

        DatabaseReference topLoserRef = databaseReference.child("TopLosers");
        topLoserRef.setValue(topLosers).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("LoserClass", "TopLosers updated successfully");
            } else {
                Log.d("LoserClass", "Failed to update TopLosers");
            }
        });
    }
}
