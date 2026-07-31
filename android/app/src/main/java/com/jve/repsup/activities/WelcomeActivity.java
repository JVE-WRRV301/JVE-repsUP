package com.jve.repsup.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.jve.repsup.MainActivity;
import com.jve.repsup.R;
import com.jve.repsup.utils.SessionManager;

/**
 * Launcher activity displaying "Welcome to REPS UP" branding
 * with Sign Up and Login navigation options.
 * Contains a hidden admin entry point via long-press on the title.
 */
public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If user already has an active session, skip to MainActivity
        SessionManager sessionManager = new SessionManager(this);
        if (sessionManager.hasActiveSession()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_welcome);

        Button btnSignUp = findViewById(R.id.btnSignUp);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvTitle = findViewById(R.id.tvTitle);

        btnSignUp.setOnClickListener(v -> {
            startActivity(new Intent(WelcomeActivity.this, RegisterActivity.class));
        });

        btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
        });

        // Hidden admin entry point: long-press on the title navigates to admin login
        tvTitle.setOnLongClickListener(v -> {
            startActivity(new Intent(WelcomeActivity.this, AdminLoginActivity.class));
            return true;
        });
    }
}
