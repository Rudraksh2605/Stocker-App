package com.rud.stocker.GainerLoser;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class FirebaseUpdater {
    private DatabaseReference dbRef;

    public FirebaseUpdater() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        dbRef = database.getReference();
    }

    // Sanitize the stock name to remove invalid Firebase characters
    private String sanitizeKey(String key) {
        return key.replace(".", "_")  // Replace periods
                .replace("/", "_")  // Replace slashes
                .replace("#", "_")  // Replace hash symbols
                .replace("$", "_")  // Replace dollar symbols
                .replace("[", "_")  // Replace left brackets
                .replace("]", "_"); // Replace right brackets
    }

    public void updateCurrentPrices(Map<String, String> currentPrices) {
        Map<String, String> sanitizedPrices = new HashMap<>();

        // Sanitize all keys
        for (Map.Entry<String, String> entry : currentPrices.entrySet()) {
            String sanitizedKey = sanitizeKey(entry.getKey());
            sanitizedPrices.put(sanitizedKey, entry.getValue());
        }

        dbRef.child("CurrentPrice").setValue(sanitizedPrices)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        System.out.println("Current Prices updated successfully");
                    } else {
                        System.out.println("Failed to update Current Prices");
                    }
                });
    }
}
