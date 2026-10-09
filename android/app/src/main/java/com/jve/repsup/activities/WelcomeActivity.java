package com.jve.repsup.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.jve.repsup.MainActivity;
import com.jve.repsup.R;
import com.jve.repsup.utils.InsetsHelper;
import com.jve.repsup.utils.SessionManager;

/**
 * Launcher activity displaying "Welcome to REPS UP." branding
 * with full-screen gym background, Sign Up button, and Login link.
 * Contains a hidden admin entry point via long-press on the title.
 */
public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Draw the full-screen gym background behind the status and nav bars.
        InsetsHelper.enableEdgeToEdge(getWindow());

        // If user already has an active session, skip to MainActivity
        SessionManager sessionManager = new SessionManager(this);
        if (sessionManager.hasActiveSession()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_welcome);

        Button btnSignUp = findViewById(R.id.btnSignUp);
        TextView tvRepsUp = findViewById(R.id.tvRepsUp);
        TextView tvHaveAccount = findViewById(R.id.tvHaveAccount);

        // Sign Up button navigates to RegisterActivity
        btnSignUp.setOnClickListener(v ->
                startActivity(new Intent(WelcomeActivity.this, RegisterActivity.class))
        );

        // Set up "Have an account already? Login" with clickable "Login" portion
        setupLoginLink(tvHaveAccount);

        // Hidden admin entry point: long-press on "REPS UP." title navigates to admin login
        tvRepsUp.setOnLongClickListener(v -> {
            startActivity(new Intent(WelcomeActivity.this, AdminLoginActivity.class));
            return true;
        });
    }

    /**
     * Makes the "Login" portion of the "Have an account already? Login" text
     * clickable and styled in the accent blue color.
     */
    private void setupLoginLink(TextView tvHaveAccount) {
        String fullText = getString(R.string.welcome_have_account);
        String loginWord = getString(R.string.btn_login);
        int loginStart = fullText.indexOf(loginWord);

        if (loginStart == -1) {
            // Fallback: make the whole text clickable
            tvHaveAccount.setOnClickListener(v ->
                    startActivity(new Intent(WelcomeActivity.this, LoginActivity.class))
            );
            return;
        }

        SpannableString spannable = new SpannableString(fullText);
        int loginEnd = loginStart + loginWord.length();
        int blueColor = ContextCompat.getColor(this, R.color.light_blue);

        ClickableSpan loginSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setColor(blueColor);
                ds.setUnderlineText(false);
            }
        };

        spannable.setSpan(loginSpan, loginStart, loginEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvHaveAccount.setText(spannable);
        tvHaveAccount.setMovementMethod(LinkMovementMethod.getInstance());
        tvHaveAccount.setHighlightColor(Color.TRANSPARENT);
    }
}
