package com.jve.repsup.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.jve.repsup.R;
import com.jve.repsup.utils.SelectableButtonStyler;
import com.jve.repsup.adapters.AssessmentPagerAdapter;
import com.jve.repsup.database.ConnectionManager;
import com.jve.repsup.utils.DialogUtils;
import com.jve.repsup.utils.InsetsHelper;
import com.jve.repsup.utils.SessionManager;

/**
 * 9-step fitness assessment wizard using ViewPager2.
 * Steps: Welcome, Gender, Age, Weight, Height, Body Type, Workout Level, Schedule, Goals.
 * On completion, inserts a User_Profile record with collected data.
 */
public class FitnessAssessmentActivity extends AppCompatActivity {

    private ViewPager2 vpAssessment;
    private LinearLayout llProgressSegments;
    private Button btnBack;
    private Button btnNext;
    private ImageButton btnAssessmentBack;
    private AssessmentPagerAdapter adapter;
    private AssessmentData assessmentData;
    private View[] progressSegments;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Draw behind the system bars, then pad the root with the status/nav
        // insets so content isn't under the clock or gesture bar (replaces the
        // old hard-coded 72dp top gap).
        InsetsHelper.enableEdgeToEdge(getWindow());

        setContentView(R.layout.activity_fitness_assessment);

        InsetsHelper.applySystemBarsPadding(findViewById(R.id.assessmentRoot));

        assessmentData = new AssessmentData();

        // Retrieve user name from intent or session
        userName = getIntent().getStringExtra("user_name");
        if (userName == null || userName.isEmpty()) {
            SessionManager sessionManager = new SessionManager(this);
            userName = sessionManager.getName();
        }

        vpAssessment = findViewById(R.id.vpAssessment);
        llProgressSegments = findViewById(R.id.llProgressSegments);
        btnBack = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);
        btnAssessmentBack = findViewById(R.id.btnAssessmentBack);

        adapter = new AssessmentPagerAdapter(this);
        vpAssessment.setAdapter(adapter);
        vpAssessment.setUserInputEnabled(false); // Disable swipe

        setupProgressBar();
        updateProgressBar(0);
        updateButtons(0);

        // Assessment back button (step 0 only). The account already exists at
        // this point (RegisterActivity created it and the session), so going
        // back can't return to the sign-up form. Instead, log the half-made user
        // out and return to the Welcome screen.
        btnAssessmentBack.setOnClickListener(v -> logOutToWelcome());

        btnBack.setOnClickListener(v -> {
            int current = vpAssessment.getCurrentItem();
            if (current > 0) {
                vpAssessment.setCurrentItem(current - 1);
            }
        });

        btnNext.setOnClickListener(v -> {
            int current = vpAssessment.getCurrentItem();
            if (current < AssessmentPagerAdapter.TOTAL_STEPS - 1) {
                vpAssessment.setCurrentItem(current + 1);
            } else {
                // Final step — save profile
                saveProfile();
            }
        });

        vpAssessment.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateProgressBar(position);
                updateButtons(position);
                validateCurrentStep();
            }
        });
    }

    /**
     * Creates 8 segmented progress indicators (one for each question step, excluding welcome).
     */
    private void setupProgressBar() {
        int questionSteps = AssessmentPagerAdapter.TOTAL_STEPS - 1; // Exclude welcome
        progressSegments = new View[questionSteps];
        for (int i = 0; i < questionSteps; i++) {
            View segment = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, (int) (6 * getResources().getDisplayMetrics().density));
            params.weight = 1;
            params.setMargins(
                    (int) (2 * getResources().getDisplayMetrics().density), 0,
                    (int) (2 * getResources().getDisplayMetrics().density), 0);
            segment.setLayoutParams(params);
            segment.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_grey_surface));
            llProgressSegments.addView(segment);
            progressSegments[i] = segment;
        }
    }

    /**
     * Updates the segmented progress bar colors based on the current step.
     * Only the current question's segment is highlighted (currentStep - 1 since step 0 is welcome).
     */
    private void updateProgressBar(int currentStep) {
        int questionIndex = currentStep - 1; // Offset by 1 (welcome is step 0)
        for (int i = 0; i < progressSegments.length; i++) {
            if (i <= questionIndex) {
                progressSegments[i].setBackgroundColor(
                        ContextCompat.getColor(this, R.color.light_blue));
            } else {
                progressSegments[i].setBackgroundColor(
                        ContextCompat.getColor(this, R.color.dark_grey_surface));
            }
        }
    }

    /**
     * Updates button text, visibility, and top bar based on the current step.
     * Step 0: show assessment back arrow, hide progress, hide bottom Back, show "Let's go" button.
     * Other steps: hide assessment back, show progress, show normal Back/Next.
     */
    private void updateButtons(int currentStep) {
        LinearLayout.LayoutParams nextParams = (LinearLayout.LayoutParams) btnNext.getLayoutParams();

        if (currentStep == 0) {
            // Step 0: Welcome page — full-width button
            btnAssessmentBack.setVisibility(View.VISIBLE);
            llProgressSegments.setVisibility(View.GONE);
            btnBack.setVisibility(View.GONE);
            btnNext.setText(R.string.btn_lets_go);

            // Make button span full width
            nextParams.weight = 2;
            nextParams.setMargins(0, 0, 0, 0);
            btnNext.setLayoutParams(nextParams);
        } else {
            // Other steps: normal navigation
            btnAssessmentBack.setVisibility(View.GONE);
            llProgressSegments.setVisibility(View.VISIBLE);
            btnBack.setVisibility(View.VISIBLE);

            // Restore half-width
            nextParams.weight = 1;
            int marginPx = (int) (6 * getResources().getDisplayMetrics().density);
            nextParams.setMargins(marginPx, 0, 0, 0);
            btnNext.setLayoutParams(nextParams);

            if (currentStep == AssessmentPagerAdapter.TOTAL_STEPS - 1) {
                btnNext.setText(R.string.assessment_finish);
            } else {
                btnNext.setText(R.string.assessment_next);
            }
        }
        validateCurrentStep();
    }

    /**
     * Enables or disables the Next button with appropriate styling.
     */
    public void setNextEnabled(boolean enabled) {
        SelectableButtonStyler.applyEnabled(btnNext, enabled);
    }

    /**
     * Validates whether the current step has enough data to proceed.
     */
    public void validateCurrentStep() {
        int step = vpAssessment.getCurrentItem();
        switch (step) {
            case 0: setNextEnabled(true); break; // Welcome always valid
            case 1: setNextEnabled(assessmentData.getGender() != null); break;
            case 2: setNextEnabled(true); break; // Age always has a value
            case 3: setNextEnabled(true); break; // Weight always has a value
            case 4: setNextEnabled(true); break; // Height always has a value
            case 5: setNextEnabled(assessmentData.getBodyType() != null); break;
            case 6: setNextEnabled(assessmentData.getWorkoutLevel() != null); break;
            case 7: setNextEnabled(assessmentData.getWorkoutsPerWeek() > 0); break;
            case 8: setNextEnabled(!assessmentData.getFitnessGoals().isEmpty()); break;
            default: setNextEnabled(true);
        }
    }

    /**
     * Called by fragments when they update assessment data, to re-validate the Next button.
     */
    public void notifyDataChanged() {
        validateCurrentStep();
    }

    /**
     * Logs the half-made user out and returns to the Welcome screen. Used by the
     * step-0 back arrow: the account already exists, so we clear the session (so
     * the next launch doesn't jump into a profile-less MainActivity) and send the
     * user back to the start, clearing the task stack.
     */
    private void logOutToWelcome() {
        new SessionManager(this).clearSession();
        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Saves the collected assessment data to the database.
     */
    private void saveProfile() {
        SessionManager sessionManager = new SessionManager(this);
        int userId = sessionManager.getUserId();

        // Disable Finish while the insert is in flight so it can't be double-tapped
        // into inserting a second user_profile row (user_id is UNIQUE).
        setNextEnabled(false);

        String sql = "INSERT INTO user_profile (user_id, user_gender, user_age, user_weight, "
                + "user_height, body_type, workout_lvl, num_workouts_per_week, fitness_goal) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Object[] params = new Object[]{
                userId,
                assessmentData.getGender(),
                assessmentData.getAge(),
                assessmentData.getWeightInKgValue(),
                assessmentData.getHeightInCmValue(),
                assessmentData.getBodyType(),
                assessmentData.getWorkoutLevel(),
                assessmentData.getWorkoutsPerWeek(),
                assessmentData.getFitnessGoalsAsString()
        };

        ConnectionManager.executeInsert(sql, params, new ConnectionManager.DatabaseCallback<Long>() {
            @Override
            public void onSuccess(Long result) {
                runOnUiThread(() -> {
                    if (isFinishing()) return;
                    // Navigate to AvatarGenerationActivity to generate avatar
                    Intent intent = new Intent(FitnessAssessmentActivity.this,
                            AvatarGenerationActivity.class);
                    intent.putExtra(AvatarGenerationActivity.EXTRA_GENDER,
                            assessmentData.getGender());
                    intent.putExtra(AvatarGenerationActivity.EXTRA_BODY_TYPE,
                            assessmentData.getBodyType());
                    startActivity(intent);
                    // Finish so Back from the avatar screen can't return here and
                    // re-insert the profile.
                    finish();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    if (isFinishing()) return;
                    // Re-enable Finish so the user can retry after a failed save.
                    setNextEnabled(true);
                    DialogUtils.showError(FitnessAssessmentActivity.this,
                            R.string.assessment_save_error);
                });
            }
        });
    }

    /**
     * Returns the shared assessment data holder.
     * Called by fragment steps to read/write assessment values.
     */
    public AssessmentData getAssessmentData() {
        return assessmentData;
    }

    /**
     * Returns the user's first name for the welcome fragment display.
     */
    public String getUserName() {
        return userName;
    }
}
