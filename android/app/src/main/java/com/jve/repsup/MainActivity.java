package com.jve.repsup;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jve.repsup.activities.LoginActivity;
import com.jve.repsup.fragments.CalendarFragment;
import com.jve.repsup.fragments.HomeFragment;
import com.jve.repsup.fragments.MeFragment;
import com.jve.repsup.fragments.WorkoutFragment;
import com.jve.repsup.utils.SessionManager;

/**
 * Main activity hosting the navigation shell with BottomNavigationView
 * and a persistent top bar. Uses fragment hide/show pattern to preserve state.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG_HOME = "fragment_home";
    private static final String TAG_CALENDAR = "fragment_calendar";
    private static final String TAG_WORKOUT = "fragment_workout";
    private static final String TAG_ME = "fragment_me";

    private FragmentManager fragmentManager;
    private Fragment activeFragment;
    private HomeFragment homeFragment;
    private CalendarFragment calendarFragment;
    private WorkoutFragment workoutFragment;
    private MeFragment meFragment;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Session check: no session → LoginActivity
        sessionManager = new SessionManager(this);
        if (!sessionManager.hasActiveSession()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        fragmentManager = getSupportFragmentManager();

        setupFragments(savedInstanceState);
        setupBottomNavigation();
    }

    /**
     * Initializes all four tab fragments using hide/show pattern.
     * On first launch, all fragments are added but only HomeFragment is shown.
     * On configuration change, fragments are restored from FragmentManager.
     */
    private void setupFragments(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            homeFragment = HomeFragment.newInstance();
            calendarFragment = CalendarFragment.newInstance();
            workoutFragment = WorkoutFragment.newInstance();
            meFragment = MeFragment.newInstance();

            fragmentManager.beginTransaction()
                    .add(R.id.flFragmentContainer, homeFragment, TAG_HOME)
                    .add(R.id.flFragmentContainer, calendarFragment, TAG_CALENDAR)
                    .add(R.id.flFragmentContainer, workoutFragment, TAG_WORKOUT)
                    .add(R.id.flFragmentContainer, meFragment, TAG_ME)
                    .hide(calendarFragment)
                    .hide(workoutFragment)
                    .hide(meFragment)
                    .commit();

            activeFragment = homeFragment;
        } else {
            homeFragment = (HomeFragment) fragmentManager.findFragmentByTag(TAG_HOME);
            calendarFragment = (CalendarFragment) fragmentManager.findFragmentByTag(TAG_CALENDAR);
            workoutFragment = (WorkoutFragment) fragmentManager.findFragmentByTag(TAG_WORKOUT);
            meFragment = (MeFragment) fragmentManager.findFragmentByTag(TAG_ME);

            // Determine which fragment is currently visible
            if (homeFragment != null && !homeFragment.isHidden()) {
                activeFragment = homeFragment;
            } else if (calendarFragment != null && !calendarFragment.isHidden()) {
                activeFragment = calendarFragment;
            } else if (workoutFragment != null && !workoutFragment.isHidden()) {
                activeFragment = workoutFragment;
            } else if (meFragment != null && !meFragment.isHidden()) {
                activeFragment = meFragment;
            } else {
                activeFragment = homeFragment;
            }
        }
    }

    /**
     * Sets up BottomNavigationView tab switching using hide/show pattern
     * to avoid reloading previously visited fragments.
     */
    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = homeFragment;
            } else if (itemId == R.id.nav_calendar) {
                selectedFragment = calendarFragment;
            } else if (itemId == R.id.nav_workout) {
                selectedFragment = workoutFragment;
            } else if (itemId == R.id.nav_me) {
                selectedFragment = meFragment;
            }

            if (selectedFragment != null && selectedFragment != activeFragment) {
                fragmentManager.beginTransaction()
                        .hide(activeFragment)
                        .show(selectedFragment)
                        .commit();
                activeFragment = selectedFragment;
            }

            return true;
        });
    }
}
