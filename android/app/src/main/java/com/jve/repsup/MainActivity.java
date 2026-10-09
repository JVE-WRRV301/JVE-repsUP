package com.jve.repsup;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jve.repsup.activities.LoginActivity;
import com.jve.repsup.database.ConnectionManager;
import com.jve.repsup.fragments.CalendarFragment;
import com.jve.repsup.fragments.HomeFragment;
import com.jve.repsup.fragments.MeFragment;
import com.jve.repsup.fragments.WorkoutFragment;
import com.jve.repsup.models.Avatar;
import com.jve.repsup.models.CosmeticItem;
import com.jve.repsup.models.User;
import com.jve.repsup.repositories.CosmeticRepository;
import com.jve.repsup.repositories.ScheduleRepository;
import com.jve.repsup.repositories.UserRepository;
import com.jve.repsup.utils.AvatarCompositor;
import com.jve.repsup.utils.SessionManager;
import com.jve.repsup.utils.StreakManager;
import com.jve.repsup.utils.XPCalculator;

import java.util.List;

/**
 * Main activity hosting the navigation shell with BottomNavigationView
 * and a persistent top bar. Uses fragment hide/show pattern to preserve state.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
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

    // Top bar views
    private ImageView ivAvatarThumb;
    private TextView tvLevel;
    private TextView tvStreak;
    private TextView tvCoins;

    private SessionManager sessionManager;
    private UserRepository userRepository;
    private CosmeticRepository cosmeticRepository;

    /** Guards the top-bar avatar face render so it runs once, not on every resume. */
    private boolean avatarThumbLoaded;

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

        // Let the app draw behind the status bar (top bar extends behind it)
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_main);

        userRepository = new UserRepository();
        cosmeticRepository = new CosmeticRepository();
        fragmentManager = getSupportFragmentManager();

        initTopBar();
        setupFragments(savedInstanceState);
        setupBottomNavigation();
        evaluateYesterdaysStreak();
        loadTopBarData();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
     * Loads user data from the database to populate the persistent top bar.
     * Public so child fragments can trigger a refresh after level-up or XP changes.
     */
    public void loadTopBarData() {
        int userId = sessionManager.getUserId();
        if (userId == -1) return;

        // Render the user's avatar face into the top-bar profile picture. The
        // avatar rarely changes, so render it once per session (not on every
        // resume) to avoid re-running its 3-query load chain each time a tab is
        // shown. The locker calls refreshAvatarThumb() when the avatar changes.
        if (!avatarThumbLoaded) {
            avatarThumbLoaded = true;
            loadAvatarThumb(userId);
        }

        userRepository.getUserById(userId, new ConnectionManager.DatabaseCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user == null) return;

                // Compute the correct level based on total XP
                int totalXp = user.getXp();
                int correctLevel = 1;
                while (XPCalculator.shouldLevelUp(totalXp, correctLevel)) {
                    correctLevel++;
                }

                // Fix DB if level is wrong (either too low or too high)
                if (correctLevel != user.getCurrentLevel()) {
                    userRepository.updateLevel(userId, correctLevel, null);
                }

                int displayLevel = correctLevel;
                runOnUiThread(() -> {
                    tvLevel.setText("Lv. " + displayLevel);
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
     * Forces the top-bar avatar face to re-render (e.g. after the user changes
     * their outfit/hair/skin in the locker). Safe to call from any fragment.
     */
    public void refreshAvatarThumb() {
        int userId = sessionManager.getUserId();
        if (userId == -1) return;
        avatarThumbLoaded = true;
        loadAvatarThumb(userId);
    }

    /**
     * Renders the user's avatar FACE (head + hair + neck) into the top-bar
     * profile picture. Mirrors the full-body avatar load used on the Me tab, but
     * crops to the face via {@link AvatarCompositor#compositeFace}.
     *
     * Resolution chain (all DB calls are off the UI thread):
     *   gender (user_profile) -> avatar record -> equipped outfit items -> render.
     * Falls back to sensible defaults so the picture still renders if a piece
     * is missing, and leaves the placeholder in place on hard failures.
     */
    private void loadAvatarThumb(int userId) {
        cosmeticRepository.getUserGender(userId, new ConnectionManager.DatabaseCallback<String>() {
            @Override
            public void onSuccess(String gender) {
                String g = (gender != null && !gender.trim().isEmpty())
                        ? gender.trim().toLowerCase() : "male";
                loadAvatarThumbBody(userId, g);
            }

            @Override
            public void onError(Exception e) {
                loadAvatarThumbBody(userId, "male");
            }
        });
    }

    private void loadAvatarThumbBody(int userId, String gender) {
        cosmeticRepository.getAvatar(userId, new ConnectionManager.DatabaseCallback<Avatar>() {
            @Override
            public void onSuccess(Avatar avatar) {
                cosmeticRepository.getEquippedItemsForOutfit(userId,
                        new ConnectionManager.DatabaseCallback<List<CosmeticItem>>() {
                            @Override
                            public void onSuccess(List<CosmeticItem> equipped) {
                                renderAvatarThumb(gender, avatar, equipped);
                            }

                            @Override
                            public void onError(Exception e) {
                                renderAvatarThumb(gender, avatar, null);
                            }
                        });
            }

            @Override
            public void onError(Exception e) { /* leave placeholder image */ }
        });
    }

    private void renderAvatarThumb(String gender, Avatar avatar, List<CosmeticItem> equipped) {
        if (avatar == null) return;
        final String bodyType = avatar.getBodyShape();
        final String skinTone = avatar.getSkinTone();
        final String hairColour = avatar.getHairColour();

        final int bgColor = androidx.core.content.ContextCompat.getColor(
                getApplicationContext(), R.color.dark_grey_surface);

        new Thread(() -> {
            if (isFinishing() || isDestroyed()) return;
            Bitmap face = AvatarCompositor.compositeFace(
                    getApplicationContext(), gender, bodyType, skinTone, hairColour, equipped);
            if (face == null) return;
            // Circular, surface-backed profile picture so the head reads cleanly
            // against a round chip regardless of the transparent face background.
            Bitmap circle = AvatarCompositor.toCircle(face, bgColor);
            if (face != circle) {
                face.recycle();
            }
            if (circle == null) return;
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed() || ivAvatarThumb == null) return;
                ivAvatarThumb.setImageBitmap(circle);
            });
        }).start();
    }

    /**
     * Evaluates yesterday's workout status and updates the user's streak accordingly.
     * Called on app launch to ensure missed workouts reset the streak.
     */
    private void evaluateYesterdaysStreak() {
        int userId = sessionManager.getUserId();
        if (userId == -1) return;

        ScheduleRepository scheduleRepository = new ScheduleRepository();

        // Compute "yesterday" from the phone's date so the streak check doesn't
        // depend on the MySQL server's timezone.
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DAY_OF_YEAR, -1);
        java.sql.Date yesterday = new java.sql.Date(cal.getTimeInMillis());

        scheduleRepository.getYesterdaysWorkoutStatus(userId, yesterday, new ConnectionManager.DatabaseCallback<boolean[]>() {
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
                });
            }

            @Override
            public void onError(Exception e) {
                // Log error silently — don't disrupt app launch
            }
        });
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
