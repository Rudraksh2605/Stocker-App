package com.rud.stocker.auth;

import static android.content.ContentValues.TAG;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.rud.stocker.home.Home_Layout;
import com.rud.stocker.R;

// FIREBASE GOOGLE AUTH IMPORT
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.HashMap;

public class Log_In_Page extends AppCompatActivity {

    private EditText mEmail;
    private EditText mPass;
    private Button btnLogin;
    private TextView mForgetPassword;
    private TextView mSignUpHere;
    private ProgressDialog mBar;
    private UserPreferences userPreferences;
    private FirebaseAuth fauth;
    private FirebaseDatabase database;
    private static final int REQ_ONE_TAP = 2;
    private boolean showOneTapUI = true;
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;
    private FirebaseAuth mAuth;
    private Button googlebtn;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_ONE_TAP) {
            try {
                SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(data);
                String idToken = credential.getGoogleIdToken();
                if (idToken != null) {

                    AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
                    mAuth.signInWithCredential(firebaseCredential)
                            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {

                                        Log.d(TAG, "signInWithCredential:success");
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        updateUI(user);
                                    } else {

                                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                                        updateUI(null);
                                    }
                                }
                            });
                }
            } catch (ApiException e) {
                Log.e(TAG, "Google sign in failed", e);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_in_page);

        fauth = FirebaseAuth.getInstance();
        userPreferences = new UserPreferences(this);
        database = FirebaseDatabase.getInstance();

        FirebaseUser user = fauth.getCurrentUser();
        if (user != null) {
            navigateToHome();
        } else {
            initializeUI();
        }

        mBar = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();

        oneTapClient = Identity.getSignInClient(this);
        signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(getString(R.string.default_web_client_id))
                        .setFilterByAuthorizedAccounts(true)
                        .build())
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void initializeUI() {
        mEmail = findViewById(R.id.login);
        mPass = findViewById(R.id.password);
        btnLogin = findViewById(R.id.LogInButton);
        mSignUpHere = findViewById(R.id.sign_up);
        mForgetPassword = findViewById(R.id.forget_password);
        googlebtn = findViewById(R.id.sign_in_with_google);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleEmailPasswordLogin();
            }
        });

        mSignUpHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Email_Registration.class));
            }
        });

        mForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ResetActivity.class));
            }
        });

        googlebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });
    }

    private void handleEmailPasswordLogin() {
        String email = mEmail.getText().toString().trim();
        String pass = mPass.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            mEmail.setError("Email Required!");
            return;
        }

        if (TextUtils.isEmpty(pass)) {
            mPass.setError("Password is Required!");
            return;
        }

        mBar.setMessage("Processing");
        mBar.show();

        fauth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                mBar.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "LogIn Successful", Toast.LENGTH_SHORT).show();
                    userPreferences.saveUserCredentials(email, pass);
                    navigateToHome();
                } else {
                    Toast.makeText(getApplicationContext(), "LogIn Failed!", Toast.LENGTH_SHORT).show();
                    Log.e("Manual Log In Failed", "Authentication Failed.");
                }
            }
        });
    }

    private void navigateToHome() {
        startActivity(new Intent(Log_In_Page.this, Home_Layout.class));
        finish();
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            navigateToHome();
        }
    }


    private void signInWithGoogle() {
        oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(this, result -> {
                    try {
                        startIntentSenderForResult(result.getPendingIntent().getIntentSender(), REQ_ONE_TAP, null, 0, 0, 0);
                    } catch (IntentSender.SendIntentException e) {
                        Log.e(TAG, "Google sign in failed", e);
                    }
                })
                .addOnFailureListener(this, e -> {
                    Log.e(TAG, "Google sign in failed", e);
                });
    }


}
