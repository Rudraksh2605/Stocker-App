package com.rud.stocker.auth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.rud.stocker.R;
import com.rud.stocker.home.Home_Layout;

public class Email_Registration extends AppCompatActivity {

    private EditText mEmail;
    private EditText mPass;
    private Button RegButton;
    private TextView mSignin;
    private ProgressDialog mBar;
    private UserPreferences userPreferences;

    //Firebase

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mAuth = FirebaseAuth.getInstance();
        mBar = new ProgressDialog(this);
        userPreferences = new UserPreferences(this);
        registration();
    }

    private void registration(){
        mEmail = findViewById(R.id.email_registration);
        mPass = findViewById(R.id.password_registration);
        RegButton = findViewById(R.id.RegistrationButton);
        mSignin = findViewById(R.id.already_registered);

        RegButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = mEmail.getText().toString().trim();
                String Pass = mPass.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    mEmail.setError("Email Required Field");
                    return;
                }
                if(TextUtils.isEmpty(Pass)) {
                    mPass.setError("Password Required Field");
                }
                mBar.show();

                mAuth.createUserWithEmailAndPassword(email,Pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            mBar.dismiss();
                            Toast.makeText(getApplicationContext(), "Registration Complete", Toast.LENGTH_SHORT).show();
                            userPreferences.saveUserCredentials(email, Pass);

                            // Sign in the user automatically after registration
                            mAuth.signInWithEmailAndPassword(email, Pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        startActivity(new Intent(getApplicationContext(), Home_Layout.class));
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Sign In Failed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        } else {
                            mBar.dismiss();
                            Toast.makeText(getApplicationContext(), "Registration Failed" , Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

       mSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Log_In_Page.class));
            }
        });




    }
}