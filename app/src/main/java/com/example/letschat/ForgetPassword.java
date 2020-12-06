package com.example.letschat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgetPassword extends AppCompatActivity {

    private EditText email;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        email = findViewById(R.id.email);
        mAuth = FirebaseAuth.getInstance();
    }

    public void openCancel(View view) {
        Intent intent = new Intent(this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void submit(View view) {
        String fEmail = email.getText().toString().trim();
        if (fEmail.isEmpty()) {
            email.setError("PLEASE ENTER AN EMAIL");
            email.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(fEmail).matches()) {
            email.setError("PLEASE ENTER A VALID EMAIL");
            email.requestFocus();
            return;
        }

        mAuth.sendPasswordResetEmail(fEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    final Reset restPasswordEmail = new Reset(ForgetPassword.this);
                    restPasswordEmail.startLodingDialog();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            restPasswordEmail.dissmissDialog();

                        }
                    }, 3000);
                    Handler handler1 = new Handler();
                    handler1.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(ForgetPassword.this, Login.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                    }, 3000);
                } else {
                    email.setError("ACCOUNT CANNOT FOUND! PLEASE REGISTER AN ACCOUNT.");
                    email.requestFocus();
                }
            }
        });
    }
}
