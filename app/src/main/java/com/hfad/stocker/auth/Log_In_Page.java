package com.hfad.stocker.auth;

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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.hfad.stocker.R;
import com.hfad.stocker.home.Home_Layout;

import java.util.HashMap;

public class Log_In_Page extends AppCompatActivity {

    private Button googleAuth;
    private FirebaseAuth fauth;
    private FirebaseDatabase database;
    private GoogleSignInClient googleSignInClient;
    private EditText mEmail;
    private EditText mPass;
    private Button btnLogin;
    private TextView mForgetPassword;
    private TextView mSignUpHere;
    private ProgressDialog mBar;
    private UserPreferences userPreferences;

    private static final int RC_SIGN_IN = 40;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_in_page);

        FirebaseApp.initializeApp(this);

        fauth = FirebaseAuth.getInstance();
        userPreferences = new UserPreferences(this);

        FirebaseUser user = fauth.getCurrentUser();
        if (user != null) {
            startActivity(new Intent(this, Home_Layout.class));
            finish();
        } else {
            loginDetails();
        }

        googleAuth = findViewById(R.id.sign_in_with_google);
        database = FirebaseDatabase.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("http://955934810329-jqf4265cajl7lhgj1fl4votha8o7d6re.apps.googleusercontent.com")
                .requestEmail().build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleSignIn();
            }
        });

        mBar = new ProgressDialog(this);
    }

    private void loginDetails() {
        mEmail = findViewById(R.id.login);
        mPass = findViewById(R.id.password);
        btnLogin = findViewById(R.id.LogInButton);
        mSignUpHere = findViewById(R.id.sign_up);
        mForgetPassword = findViewById(R.id.forget_password);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                        if (task.isSuccessful()) {
                            mBar.dismiss();
                            Toast.makeText(getApplicationContext(), "LogIn Successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), Home_Layout.class));
                            userPreferences.saveUserCredentials(email, pass);
                        } else {
                            mBar.dismiss();
                            Toast.makeText(getApplicationContext(), "LogIn Failed!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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
    }

    private void googleSignIn() {
        Intent intent = googleSignInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    if (account != null) {
                        firebaseAuth(account.getIdToken());
                    } else {
                        Toast.makeText(this, "Google sign-in failed", Toast.LENGTH_SHORT).show();
                    }
                } catch (ApiException e) {
                    Toast.makeText(this, "Google sign-in failed: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void firebaseAuth(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        fauth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = fauth.getCurrentUser();
                            userPreferences.saveUserCredentials(user.getEmail(), user.getUid());

                            HashMap<String, Object> map = new HashMap<>();
                            map.put("id", user.getUid());
                            map.put("name", user.getDisplayName());
                            if (user.getPhotoUrl() != null) {
                                map.put("profile", user.getPhotoUrl().toString());
                            }

                            database.getReference().child("user").child(user.getUid()).setValue(map);

                            Intent intent = new Intent(Log_In_Page.this, Home_Layout.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(Log_In_Page.this, "something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
