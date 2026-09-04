package com.jve.repsup.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.jve.repsup.R;
import com.jve.repsup.activities.WorkoutExecutionActivity;
import com.jve.repsup.database.ConnectionManager;
import com.jve.repsup.models.Goal;
import com.jve.repsup.models.ScheduledWorkout;
import com.jve.repsup.models.WorkoutExercise;
import com.jve.repsup.repositories.GoalRepository;
import com.jve.repsup.repositories.ScheduleRepository;
import com.jve.repsup.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * HomeFragment displaying today's scheduled workout and the next upcoming goal.
 * Shows a week strip with today highlighted, a TODAY card (blue border) with
 * workout details and START button, and a NEXT GOAL card (red border).
 */
public class HomeFragment extends Fragment {

    private LinearLayout llWeekStrip;
    private LinearLayout llTodayContent;
    private TextView tvTodayEmpty;
    private TextView tvWorkoutName;
    private TextView tvWorkoutDate;
    private LinearLayout llExerciseList;
    private TextView btnStart;

    private LinearLayout llGoalContent;
    private TextView tvGoalEmpty;
    private TextView tvGoalDeadline;
    private TextView tvGoalTitle;
    private TextView tvGoalPriority;
    private TextView tvGoalDescription;

    private ScheduleRepository scheduleRepository;
    private GoalRepository goalRepository;
    private SessionManager sessionManager;

    private ScheduledWorkout todaysWorkout;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        scheduleRepository = new ScheduleRepository();
        goalRepository = new GoalRepository();
        sessionManager = new SessionManager(requireContext());

        initViews(view);
        highlightToday();
        loadTodaysWorkout();
        loadNextGoal();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    /**
     * Called when this fragment is shown/hidden via the MainActivity hide/show
     * tab pattern. Unlike onResume(), this fires on tab switches (the activity
     * itself never pauses), so it's needed to refresh data after changes made
     * on other tabs (e.g. scheduling a workout or creating a goal).
     */
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            refreshData();
        }
    }

    /**
     * Reloads both today's workout and the next goal so the home page reflects
     * the latest data.
     */
    private void refreshData() {
        loadTodaysWorkout();
        loadNextGoal();
    }

    private void initViews(View view) {
        llWeekStrip = view.findViewById(R.id.llWeekStrip);

        // Today card views
        llTodayContent = view.findViewById(R.id.llTodayContent);
        tvTodayEmpty = view.findViewById(R.id.tvTodayEmpty);
        tvWorkoutName = view.findViewById(R.id.tvWorkoutName);
        tvWorkoutDate = view.findViewById(R.id.tvWorkoutDate);
        llExerciseList = view.findViewById(R.id.llExerciseList);
        btnStart = view.findViewById(R.id.btnStart);

        // Goal card views
        llGoalContent = view.findViewById(R.id.llGoalContent);
        tvGoalEmpty = view.findViewById(R.id.tvGoalEmpty);
        tvGoalDeadline = view.findViewById(R.id.tvGoalDeadline);
        tvGoalTitle = view.findViewById(R.id.tvGoalTitle);
        tvGoalPriority = view.findViewById(R.id.tvGoalPriority);
        tvGoalDescription = view.findViewById(R.id.tvGoalDescription);

        btnStart.setOnClickListener(v -> startWorkout());
    }

    /**
     * Populates the week strip with dates for the current week
     * and highlights today's column with light_blue color.
     */
    private void highlightToday() {
        Calendar calendar = Calendar.getInstance();
        int todayDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        // Calendar.SUNDAY = 1, so index in our strip is dayOfWeek - 1
        int todayIndex = todayDayOfWeek - Calendar.SUNDAY;

        // Calculate the start of the week (Sunday)
        Calendar weekStart = (Calendar) calendar.clone();
        weekStart.add(Calendar.DAY_OF_YEAR, -(todayDayOfWeek - Calendar.SUNDAY));

        int highlightColor = getResources().getColor(R.color.light_blue, null);
        int mutedColor = getResources().getColor(R.color.muted, null);

        for (int i = 0; i < llWeekStrip.getChildCount(); i++) {
            View child = llWeekStrip.getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout dayColumn = (LinearLayout) child;
                if (dayColumn.getChildCount() >= 2) {
                    TextView labelView = (TextView) dayColumn.getChildAt(0);
                    TextView dateView = (TextView) dayColumn.getChildAt(1);

                    // Set the date number
                    Calendar dayCalendar = (Calendar) weekStart.clone();
                    dayCalendar.add(Calendar.DAY_OF_YEAR, i);
                    int dayOfMonth = dayCalendar.get(Calendar.DAY_OF_MONTH);
                    dateView.setText(String.valueOf(dayOfMonth));

                    // Highlight today
                    if (i == todayIndex) {
                        labelView.setTextColor(highlightColor);
                        dateView.setTextColor(highlightColor);
                    } else {
                        labelView.setTextColor(mutedColor);
                        dateView.setTextColor(mutedColor);
                    }
                }
            }
        }
    }

    /**
     * Loads today's scheduled workout from the database.
     */
    private void loadTodaysWorkout() {
        int userId = sessionManager.getUserId();
        if (userId == -1) return;

        scheduleRepository.getTodaysWorkout(userId, new ConnectionManager.DatabaseCallback<ScheduledWorkout>() {
            @Override
            public void onSuccess(ScheduledWorkout result) {
                if (getActivity() == null || !isAdded()) return;
                getActivity().runOnUiThread(() -> {
                    todaysWorkout = result;
                    if (result != null && result.getWorkout() != null) {
                        showWorkoutCard(result);
                    } else {
                        showRestDayState();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                if (getActivity() == null || !isAdded()) return;
                getActivity().runOnUiThread(() -> {
                    showRestDayState();
                    Toast.makeText(getContext(),
                            R.string.login_connection_error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /**
     * Displays the workout card with name, date, and exercises.
     * Shows "Completed" status if the workout has already been finished.
     */
    private void showWorkoutCard(ScheduledWorkout scheduledWorkout) {
        llTodayContent.setVisibility(View.VISIBLE);
        tvTodayEmpty.setVisibility(View.GONE);

        tvWorkoutName.setText(scheduledWorkout.getWorkout().getName());

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM d", Locale.getDefault());
        tvWorkoutDate.setText(dateFormat.format(scheduledWorkout.getScheduledDate()));

        // Show START button or Completed status
        if (scheduledWorkout.isCompleted()) {
            btnStart.setText(R.string.calendar_status_completed);
            btnStart.setEnabled(false);
            btnStart.setAlpha(0.7f);
        } else {
            btnStart.setText(R.string.home_start);
            btnStart.setEnabled(true);
            btnStart.setAlpha(1.0f);
        }

        // Populate exercise list
        llExerciseList.removeAllViews();
        List<WorkoutExercise> exercises = scheduledWorkout.getWorkout().getExercises();
        if (exercises != null) {
            for (WorkoutExercise we : exercises) {
                View exerciseItem = LayoutInflater.from(getContext())
                        .inflate(R.layout.item_exercise_preview, llExerciseList, false);

                ImageView ivIcon = exerciseItem.findViewById(R.id.ivExerciseIcon);
                TextView tvName = exerciseItem.findViewById(R.id.tvExerciseName);
                TextView tvSets = exerciseItem.findViewById(R.id.tvExerciseSets);

                ivIcon.setImageResource(R.drawable.ic_exercise_placeholder);
                if (we.getExercise() != null) {
                    tvName.setText(we.getExercise().getName());
                }
                tvSets.setText(getString(R.string.home_sets_format, we.getSets()));

                llExerciseList.addView(exerciseItem);
            }
        }
    }

    /**
     * Shows the rest day empty state when no workout is scheduled.
     */
    private void showRestDayState() {
        llTodayContent.setVisibility(View.GONE);
        tvTodayEmpty.setVisibility(View.VISIBLE);
    }

    /**
     * Loads the next upcoming goal from the database.
     */
    private void loadNextGoal() {
        int userId = sessionManager.getUserId();
        if (userId == -1) return;

        goalRepository.getNextGoal(userId, new ConnectionManager.DatabaseCallback<Goal>() {
            @Override
            public void onSuccess(Goal result) {
                if (getActivity() == null || !isAdded()) return;
                getActivity().runOnUiThread(() -> {
                    if (result != null) {
                        showGoalCard(result);
                    } else {
                        showNoGoalsState();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                if (getActivity() == null || !isAdded()) return;
                getActivity().runOnUiThread(() -> {
                    showNoGoalsState();
                    Toast.makeText(getContext(),
                            R.string.login_connection_error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /**
     * Displays the goal card with deadline, title, priority, and description.
     */
    private void showGoalCard(Goal goal) {
        llGoalContent.setVisibility(View.VISIBLE);
        tvGoalEmpty.setVisibility(View.GONE);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        tvGoalDeadline.setText(getString(R.string.home_deadline_format,
                dateFormat.format(goal.getDeadline())));

        // Description stores the title and optional notes joined by "\n---\n"
        // (same format the schedule/goals list uses). Split them so the heading
        // shows only the title and the subheading shows only the notes.
        String description = goal.getDescription();
        String title = description;
        String notes = "";
        if (description != null && description.contains("\n---\n")) {
            int separatorIndex = description.indexOf("\n---\n");
            title = description.substring(0, separatorIndex);
            notes = description.substring(separatorIndex + 5);
        }

        tvGoalTitle.setText(title);
        tvGoalPriority.setText(goal.getPriority());

        if (!notes.isEmpty()) {
            tvGoalDescription.setText(notes);
            tvGoalDescription.setVisibility(View.VISIBLE);
        } else {
            tvGoalDescription.setVisibility(View.GONE);
        }
    }

    /**
     * Shows the empty state when no goals exist.
     */
    private void showNoGoalsState() {
        llGoalContent.setVisibility(View.GONE);
        tvGoalEmpty.setVisibility(View.VISIBLE);
    }

    /**
     * Navigates to WorkoutExecutionActivity with the scheduled workout data.
     */
    private void startWorkout() {
        if (todaysWorkout == null || todaysWorkout.isCompleted()) return;

        Intent intent = new Intent(getContext(), WorkoutExecutionActivity.class);
        intent.putExtra(WorkoutExecutionActivity.EXTRA_SCHEDULED_WORKOUT_ID,
                todaysWorkout.getScheduledWorkoutId());
        intent.putExtra(WorkoutExecutionActivity.EXTRA_WORKOUT_ID,
                todaysWorkout.getWorkoutId());
        startActivity(intent);
    }
}
