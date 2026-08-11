package com.jve.repsup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

<<<<<<< HEAD
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
=======
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
>>>>>>> 581670146b11a91bfebf3cb43d1120e2cf8fcc8f

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jve.repsup.activities.LoginActivity;
import com.jve.repsup.database.ConnectionManager;
import com.jve.repsup.fragments.CalendarFragment;
import com.jve.repsup.fragments.HomeFragment;
import com.jve.repsup.fragments.MeFragment;
import com.jve.repsup.fragments.WorkoutFragment;
<<<<<<< HEAD
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
=======
import com.jve.repsup.repositories.ScheduleRepository;
import com.jve.repsup.repositories.UserRepository;
import com.jve.repsup.utils.SessionManager;
import com.jve.repsup.utils.StreakManager;

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
>>>>>>> 581670146b11a91bfebf3cb43d1120e2cf8fcc8f
    private Fragment activeFragment;
    private HomeFragment homeFragment;
    private CalendarFragment calendarFragment;
    private WorkoutFragment workoutFragment;
    private MeFragment meFragment;

<<<<<<< HEAD
    // Data
    private SessionManager sessionManager;
    private UserRepository userRepository;
=======
    private SessionManager sessionManager;
>>>>>>> 581670146b11a91bfebf3cb43d1120e2cf8fcc8f

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

<<<<<<< HEAD
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
=======
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
>>>>>>> 581670146b11a91bfebf3cb43d1120e2cf8fcc8f
        });

        fragmentManager = getSupportFragmentManager();

        setupFragments(savedInstanceState);
        setupBottomNavigation();
        evaluateYesterdaysStreak();
    }

    /**
<<<<<<< HEAD
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
=======
     * Evaluates yesterday's workout status and updates the user's streak accordingly.
     * Called on app launch to ensure missed workouts reset the streak.
     */
    private void evaluateYesterdaysStreak() {
        int userId = sessionManager.getUserId();
        if (userId == -1) return;

        ScheduleRepository scheduleRepository = new ScheduleRepository();
        UserRepository userRepository = new UserRepository();

        scheduleRepository.getYesterdaysWorkoutStatus(userId, new ConnectionManager.DatabaseCallback<boolean[]>() {
            @Override
            public void onSuccess(boolean[] status) {
                boolean hadScheduledWorkout = status[0];
                boolean completedYesterday = status[1];

                userRepository.getCurrentStreak(userId, new ConnectionManager.DatabaseCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer streak) {
                        int currentStreak = streak != null ? streak : 0;
                        int newStreak = StreakManager.evaluateStreak(currentStreak, hadScheduledWorkout, completedYesterday);

                        if (newStreak != currentStreak) {
                            userRepository.updateStreak(userId, newStreak, new ConnectionManager.DatabaseCallback<Integer>() {
                                @Override
                                public void onSuccess(Integer result) {
                                    // Streak updated
                                }

                                @Override
                                public void onError(Exception e) {
                                    // Log error silently
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        // Log error silently
                    }
>>>>>>> 581670146b11a91bfebf3cb43d1120e2cf8fcc8f
                });
            }

            @Override
            public void onError(Exception e) {
<<<<<<< HEAD
                Log.e(TAG, "Failed to load top bar data", e);
=======
                // Log error silently — don't disrupt app launch
>>>>>>> 581670146b11a91bfebf3cb43d1120e2cf8fcc8f
            }
        });
    }

    /**
<<<<<<< HEAD
     * Redirects to LoginActivity and finishes this activity.
     */
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
=======
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
>>>>>>> 581670146b11a91bfebf3cb43d1120e2cf8fcc8f
    }
}
