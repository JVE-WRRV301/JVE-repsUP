package com.jve.repsup.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.jve.repsup.R;
import com.jve.repsup.utils.DateFormats;
import com.jve.repsup.activities.CreateWorkoutActivity;
import com.jve.repsup.activities.WorkoutExecutionActivity;
import com.jve.repsup.adapters.WeeklyPlanAdapter;
import com.jve.repsup.database.ConnectionManager;
import com.jve.repsup.models.ScheduledWorkout;
import com.jve.repsup.repositories.ScheduleRepository;
import com.jve.repsup.utils.DialogUtils;
import com.jve.repsup.utils.SessionManager;

import java.sql.Date;
import java.util.Calendar;
import java.util.List;

/**
 * Fragment displaying the weekly plan with collapsible day sections.
 * Each day shows scheduled workouts with status (Completed, Missed, Scheduled)
 * or a START button for today's workout.
 */
public class WeeklyPlanFragment extends Fragment implements WeeklyPlanAdapter.OnStartClickListener {

    private RecyclerView rvWeeklyPlan;
    private TextView tvWeekRange;
    private TextView tvEmptyState;
    private ImageView ivPrevWeek;
    private ImageView ivNextWeek;

    private WeeklyPlanAdapter adapter;
    private ScheduleRepository scheduleRepository;
    private SessionManager sessionManager;

    private Calendar currentWeekStart;

    public WeeklyPlanFragment() {
        // Required empty public constructor
    }

    public static WeeklyPlanFragment newInstance() {
        return new WeeklyPlanFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_weekly_plan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        scheduleRepository = new ScheduleRepository();
        sessionManager = new SessionManager(requireContext());

        initViews(view);
        setupRecyclerView();
        setupWeekNavigation();

        // Initialize to current week (starting Monday)
        currentWeekStart = getStartOfWeek(Calendar.getInstance());
        loadWeekData();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadWeekData();
    }

    private void initViews(View view) {
        rvWeeklyPlan = view.findViewById(R.id.rvWeeklyPlan);
        tvWeekRange = view.findViewById(R.id.tvWeekRange);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        ivPrevWeek = view.findViewById(R.id.ivPrevWeek);
        ivNextWeek = view.findViewById(R.id.ivNextWeek);
    }

    private void setupRecyclerView() {
        adapter = new WeeklyPlanAdapter(this);
        adapter.setOnMenuActionListener(new WeeklyPlanAdapter.OnMenuActionListener() {
            @Override
            public void onEditScheduledWorkout(ScheduledWorkout scheduledWorkout) {
                if (getContext() == null) return;
                Intent intent = new Intent(getContext(), CreateWorkoutActivity.class);
                intent.putExtra(CreateWorkoutActivity.EXTRA_EDIT_WORKOUT_ID, scheduledWorkout.getWorkoutId());
                // Pass the scheduled row so that, if the workout turns out to be a
                // default or another user's workout, Create Workout can save a
                // personal copy and repoint this scheduled workout at it instead
                // of changing the shared workout.
                intent.putExtra(CreateWorkoutActivity.EXTRA_SCHEDULED_WORKOUT_ID,
                        scheduledWorkout.getScheduledWorkoutId());
                startActivity(intent);
            }

            @Override
            public void onDeleteScheduledWorkout(ScheduledWorkout scheduledWorkout) {
                if (getContext() == null) return;

                if (scheduledWorkout.getSeriesId() > 0) {
                    // Part of a repeat: ask whether to remove just this one or
                    // this one and every later repeat.
                    // Custom layout so the two delete choices stack on the right
                    // and Cancel sits bottom left (the standard button bar
                    // squashes three buttons into one column).
                    View content = LayoutInflater.from(getContext())
                            .inflate(R.layout.dialog_delete_series, null);
                    androidx.appcompat.app.AlertDialog dialog =
                            new MaterialAlertDialogBuilder(getContext())
                                    .setTitle(R.string.weekly_plan_delete_title)
                                    .setView(content)
                                    .create();
                    content.findViewById(R.id.btnDeleteThisOne).setOnClickListener(v -> {
                        dialog.dismiss();
                        deleteOne(scheduledWorkout);
                    });
                    content.findViewById(R.id.btnDeleteAllFuture).setOnClickListener(v -> {
                        dialog.dismiss();
                        deleteAllFuture(scheduledWorkout);
                    });
                    content.findViewById(R.id.btnDeleteCancel).setOnClickListener(v -> dialog.dismiss());
                    dialog.show();
                    return;
                }

                new MaterialAlertDialogBuilder(getContext())
                        .setTitle(R.string.weekly_plan_delete_title)
                        .setMessage(R.string.weekly_plan_delete_message)
                        .setPositiveButton(R.string.workout_action_delete,
                                (dialog, which) -> deleteOne(scheduledWorkout))
                        .setNegativeButton(R.string.admin_action_cancel, null)
                        .show();
            }
        });
        rvWeeklyPlan.setLayoutManager(new LinearLayoutManager(getContext()));
        rvWeeklyPlan.setAdapter(adapter);
    }

    /** Deletes a single scheduled workout, then reloads the week. */
    private void deleteOne(ScheduledWorkout scheduledWorkout) {
        scheduleRepository.deleteScheduledWorkout(scheduledWorkout.getScheduledWorkoutId(),
                deleteCallback());
    }

    /** Deletes this workout and every later unfinished repeat in its series. */
    private void deleteAllFuture(ScheduledWorkout scheduledWorkout) {
        scheduleRepository.deleteScheduledWorkoutSeriesFrom(sessionManager.getUserId(),
                scheduledWorkout.getSeriesId(), scheduledWorkout.getScheduledDate(),
                deleteCallback());
    }

    /** Reloads the week after a delete, or tells the user it failed. */
    private ConnectionManager.DatabaseCallback<Integer> deleteCallback() {
        return new ConnectionManager.DatabaseCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                if (getActivity() == null || !isAdded()) return;
                getActivity().runOnUiThread(() -> loadWeekData());
            }

            @Override
            public void onError(Exception e) {
                if (getActivity() == null || !isAdded()) return;
                getActivity().runOnUiThread(() ->
                        DialogUtils.showError(getContext(), R.string.weekly_plan_delete_error));
            }
        };
    }

    private void setupWeekNavigation() {
        ivPrevWeek.setOnClickListener(v -> navigateWeek(-1));
        ivNextWeek.setOnClickListener(v -> navigateWeek(1));
    }

    /**
     * Navigates the week view by the given offset (-1 = previous, +1 = next).
     */
    private void navigateWeek(int offset) {
        currentWeekStart.add(Calendar.WEEK_OF_YEAR, offset);
        loadWeekData();
    }

    /**
     * Loads scheduled workouts for the current week and updates the UI.
     */
    private void loadWeekData() {
        updateWeekRangeHeader();

        int userId = sessionManager.getUserId();
        if (userId == -1) return;

        Date weekStartDate = new Date(currentWeekStart.getTimeInMillis());

        scheduleRepository.getScheduledWorkoutsForWeek(userId, weekStartDate,
                new ConnectionManager.DatabaseCallback<List<ScheduledWorkout>>() {
                    @Override
                    public void onSuccess(List<ScheduledWorkout> result) {
                        if (getActivity() == null || !isAdded()) return;
                        getActivity().runOnUiThread(() -> {
                            Date weekStart = new Date(currentWeekStart.getTimeInMillis());
                            adapter.setWeekData(weekStart, result);

                            // Show/hide empty state
                            if (result == null || result.isEmpty()) {
                                tvEmptyState.setVisibility(View.VISIBLE);
                                rvWeeklyPlan.setVisibility(View.VISIBLE);
                            } else {
                                tvEmptyState.setVisibility(View.GONE);
                                rvWeeklyPlan.setVisibility(View.VISIBLE);
                            }

                            // Auto-scroll to today's position
                            scrollToToday();
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        if (getActivity() == null || !isAdded()) return;
                        getActivity().runOnUiThread(() -> {
                            tvEmptyState.setVisibility(View.VISIBLE);
                            rvWeeklyPlan.setVisibility(View.GONE);
                        });
                    }
                });
    }

    /**
     * Updates the week range text header (e.g., "6 Oct – 12 Oct").
     */
    private void updateWeekRangeHeader() {
        Calendar weekEnd = (Calendar) currentWeekStart.clone();
        weekEnd.add(Calendar.DAY_OF_YEAR, 6);

        String startStr = DateFormats.formatDayMonth(currentWeekStart.getTime());
        String endStr = DateFormats.formatDayMonth(weekEnd.getTime());
        tvWeekRange.setText(getString(R.string.weekly_plan_week_range, startStr, endStr));
    }

    /**
     * Scrolls the RecyclerView to today's day header position.
     */
    private void scrollToToday() {
        int todayPos = adapter.getTodayPosition();
        if (todayPos >= 0) {
            rvWeeklyPlan.post(() -> {
                RecyclerView.LayoutManager lm = rvWeeklyPlan.getLayoutManager();
                if (lm instanceof LinearLayoutManager) {
                    ((LinearLayoutManager) lm).scrollToPositionWithOffset(todayPos, 0);
                }
            });
        }
    }

    /**
     * Returns a Calendar set to the Monday of the week containing the given date.
     */
    private Calendar getStartOfWeek(Calendar date) {
        Calendar cal = (Calendar) date.clone();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        // Roll back to Monday
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int daysToSubtract;
        if (dayOfWeek == Calendar.SUNDAY) {
            daysToSubtract = 6;
        } else {
            daysToSubtract = dayOfWeek - Calendar.MONDAY;
        }
        cal.add(Calendar.DAY_OF_YEAR, -daysToSubtract);
        return cal;
    }

    /**
     * Handles the START button click — navigates to WorkoutExecutionActivity.
     */
    @Override
    public void onStartClick(ScheduledWorkout scheduledWorkout) {
        if (getContext() == null || scheduledWorkout == null) return;

        Intent intent = new Intent(getContext(), WorkoutExecutionActivity.class);
        intent.putExtra(WorkoutExecutionActivity.EXTRA_SCHEDULED_WORKOUT_ID,
                scheduledWorkout.getScheduledWorkoutId());
        intent.putExtra(WorkoutExecutionActivity.EXTRA_WORKOUT_ID,
                scheduledWorkout.getWorkoutId());
        startActivity(intent);
    }
}
