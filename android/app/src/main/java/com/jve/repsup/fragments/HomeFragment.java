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
     * Highlights today's day in the week strip with light_blue background.
     */
    private void highlightToday() {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        // Calendar.SUNDAY = 1, MONDAY = 2, ..., SATURDAY = 7
        // Our strip order: S M T W T F S (indices 0-6)
        int index = dayOfWeek - Calendar.SUNDAY;

        for (int i = 0; i < llWeekStrip.getChildCount(); i++) {
            TextView dayView = (TextView) llWeekStrip.getChildAt(i);
            if (i == index) {
                dayView.setBackgroundResource(R.drawable.bg_day_highlighted);
                dayView.setTextColor(getResources().getColor(R.color.dark_text, null));
            } else {
                dayView.setBackground(null);
                dayView.setTextColor(getResources().getColor(R.color.muted, null));
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
     */
    private void showWorkoutCard(ScheduledWorkout scheduledWorkout) {
        llTodayContent.setVisibility(View.VISIBLE);
        tvTodayEmpty.setVisibility(View.GONE);

        tvWorkoutName.setText(scheduledWorkout.getWorkout().getName());

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM d", Locale.getDefault());
        tvWorkoutDate.setText(dateFormat.format(scheduledWorkout.getScheduledDate()));

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

        tvGoalTitle.setText(goal.getDescription());
        tvGoalPriority.setText(goal.getPriority());
        tvGoalDescription.setText(goal.getDescription());
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
        if (todaysWorkout == null) return;

        Intent intent = new Intent(getContext(), WorkoutExecutionActivity.class);
        intent.putExtra(WorkoutExecutionActivity.EXTRA_SCHEDULED_WORKOUT_ID,
                todaysWorkout.getScheduledWorkoutId());
        intent.putExtra(WorkoutExecutionActivity.EXTRA_WORKOUT_ID,
                todaysWorkout.getWorkoutId());
        startActivity(intent);
    }
}
