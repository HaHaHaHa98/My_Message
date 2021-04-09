package com.example.my_message;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StartActivity extends AppCompatActivity {
    private Button btnLogin;
    private Button btnRegister;
    private FirebaseUser firebaseUser;

    /* access modifiers changed from: protected */
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        this.firebaseUser = currentUser;
        if (currentUser != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_start);
        this.btnLogin = (Button) findViewById(R.id.btnLogin);
        this.btnRegister = (Button) findViewById(R.id.btnRegister);
        this.btnLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                StartActivity.this.startActivity(new Intent(StartActivity.this, LoginActivity.class));
            }
        });
        this.btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                StartActivity.this.startActivity(new Intent(StartActivity.this, RegisterActivity.class));
            }
        });
    }
}