package com.jve.repsup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jve.repsup.activities.LoginActivity;
import com.jve.repsup.database.ConnectionManager;
import com.jve.repsup.fragments.CalendarFragment;
import com.jve.repsup.fragments.HomeFragment;
import com.jve.repsup.fragments.MeFragment;
import com.jve.repsup.fragments.WorkoutFragment;
import com.jve.repsup.models.User;
import com.jve.repsup.repositories.UserRepository;
import com.jve.repsup.utils.SessionManager;

/**
 * MainActivity hosts the main navigation shell of the Reps Up app.
 * Contains a persistent top bar (avatar, level, streak, coins),
 * a fragment container managed via show/hide pattern, and a BottomNavigationView.
 *
 * On launch, checks for an active session. If none exists, redirects to LoginActivity.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // Top bar views
    private ImageView ivAvatarThumb;
    private TextView tvLevel;
    private TextView tvStreak;
    private TextView tvCoins;

    // Navigation
    private BottomNavigationView bottomNav;

    // Fragments
    private Fragment activeFragment;
    private HomeFragment homeFragment;
    private CalendarFragment calendarFragment;
    private WorkoutFragment workoutFragment;
    private MeFragment meFragment;

    // Data
    private SessionManager sessionManager;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Session check: redirect to login if no active session
        sessionManager = new SessionManager(this);
        if (!sessionManager.hasActiveSession()) {
            navigateToLogin();
            return;
        }

        setContentView(R.layout.activity_main);

        userRepository = new UserRepository();

        initTopBar();
        setupNavigation();
        loadTopBarData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh top bar data when returning from other activities
        if (sessionManager != null && sessionManager.hasActiveSession()) {
            loadTopBarData();
        }
    }

    /**
     * Initializes the persistent top bar view references.
     */
    private void initTopBar() {
        ivAvatarThumb = findViewById(R.id.ivAvatarThumb);
        tvLevel = findViewById(R.id.tvLevel);
        tvStreak = findViewById(R.id.tvStreak);
        tvCoins = findViewById(R.id.tvCoins);
    }

    /**
     * Sets up the BottomNavigationView and fragment management.
     * Uses show/hide pattern to preserve fragment state across tab switches.
     */
    private void setupNavigation() {
        bottomNav = findViewById(R.id.bottomNav);

        homeFragment = new HomeFragment();
        calendarFragment = new CalendarFragment();
        workoutFragment = new WorkoutFragment();
        meFragment = new MeFragment();

        // Add all fragments, hide all except home (which is the default tab)
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, meFragment, "me").hide(meFragment)
                .add(R.id.fragment_container, workoutFragment, "workout").hide(workoutFragment)
                .add(R.id.fragment_container, calendarFragment, "calendar").hide(calendarFragment)
                .add(R.id.fragment_container, homeFragment, "home")
                .commit();

        activeFragment = homeFragment;

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selected = homeFragment;
            } else if (itemId == R.id.nav_calendar) {
                selected = calendarFragment;
            } else if (itemId == R.id.nav_workout) {
                selected = workoutFragment;
            } else if (itemId == R.id.nav_me) {
                selected = meFragment;
            }

            if (selected != null && selected != activeFragment) {
                getSupportFragmentManager().beginTransaction()
                        .hide(activeFragment)
                        .show(selected)
                        .commit();
                activeFragment = selected;
            }
            return true;
        });
    }

    /**
     * Loads user data from the database to populate the persistent top bar.
     */
    private void loadTopBarData() {
        int userId = sessionManager.getUserId();
        if (userId == -1) return;

        userRepository.getUserById(userId, new ConnectionManager.DatabaseCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user == null) return;
                runOnUiThread(() -> {
                    tvLevel.setText("Lv. " + user.getLevel());
                    tvStreak.setText(String.valueOf(user.getCurrentStreak()));
                    tvCoins.setText(String.valueOf(user.getCoins()));
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Failed to load top bar data", e);
            }
        });
    }

    /**
     * Redirects to LoginActivity and finishes this activity.
     */
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
