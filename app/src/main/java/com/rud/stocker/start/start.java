package com.rud.stocker.start;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import com.rud.stocker.Database.database;
import com.rud.stocker.R;
import com.rud.stocker.auth.Log_In_Page;

public class start extends AppCompatActivity {

    private static final int DISPLAY_DURATION = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.open_animation);
        database db = new database();
        db.Database();
        db.createUserData();
        db.updateTotalAmount();
        db.updateCurrent();
        db.updateProfit();

        // Create a new Handler to post a Runnable after 2 seconds
        new Handler().postDelayed(() -> {
            // Start the next activity
            startActivity(new Intent(start.this, Log_In_Page.class));
            finish(); // Close the splash screen
        }, DISPLAY_DURATION);
    }
}
