package com.rud.stocker.GainerLoser;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class FirebaseStockUpdater {
    private DatabaseReference dbRef;

    public FirebaseStockUpdater() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        dbRef = database.getReference();
    }

    // Helper method to sanitize keys
    private String sanitizeKey(String key) {
        return key.replace(".", "_")    // Replace period
                .replace("/", "_")    // Replace forward slash
                .replace("#", "_")    // Replace hash
                .replace("$", "_")    // Replace dollar sign
                .replace("[", "_")    // Replace open square bracket
                .replace("]", "_");   // Replace close square bracket
    }

    public void updateClosingPrices(Map<String, String> currentPrices) {
        // Create a new map with sanitized keys
        Map<String, String> sanitizedPrices = new HashMap<>();
        for (Map.Entry<String, String> entry : currentPrices.entrySet()) {
            String sanitizedKey = sanitizeKey(entry.getKey());
            sanitizedPrices.put(sanitizedKey, entry.getValue());
        }

        // Check if "ClosingPrice" exists, then update or create it
        dbRef.child("ClosingPrice").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // If "ClosingPrice" doesn't exist, this will create the field
                if (!task.getResult().exists()) {
                    System.out.println("ClosingPrice does not exist. Creating...");
                }

                // Update "ClosingPrice" with the sanitized map
                dbRef.child("ClosingPrice").setValue(sanitizedPrices)
                        .addOnCompleteListener(setTask -> {
                            if (setTask.isSuccessful()) {
                                System.out.println("Closing Prices updated successfully");
                            } else {
                                System.out.println("Failed to update Closing Prices");
                            }
                        });
            } else {
                System.out.println("Error checking for ClosingPrice existence: " + task.getException());
            }
        });
    }
}
