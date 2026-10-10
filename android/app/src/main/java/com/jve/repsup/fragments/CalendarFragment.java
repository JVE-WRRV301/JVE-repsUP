package com.jve.repsup.fragments;

import android.util.Log;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.jve.repsup.R;
import com.jve.repsup.utils.DateFormats;
import com.jve.repsup.utils.GoalPriorityStyle;
import com.jve.repsup.activities.CreateGoalActivity;
import com.jve.repsup.activities.WorkoutExecutionActivity;
import com.jve.repsup.adapters.CalendarDayAdapter;
import com.jve.repsup.adapters.DayContentAdapter;
import com.jve.repsup.database.ConnectionManager;
import com.jve.repsup.models.ExerciseResult;
import com.jve.repsup.models.Goal;
import com.jve.repsup.models.ScheduledWorkout;
import com.jve.repsup.repositories.ExerciseLogRepository;
import com.jve.repsup.repositories.GoalRepository;
import com.jve.repsup.repositories.ScheduleRepository;
import com.jve.repsup.utils.SessionManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * CalendarFragment displaying the monthly calendar grid with workout and goal indicators,
 * "My Plan" and "Goals" tabs, and a FAB for scheduling workouts or creating goals.
 */
public class CalendarFragment extends Fragment implements CalendarDayAdapter.OnDayClickListener {

    private static final String TAG = "CalendarFragment";

    private TextView tvMonthYear;
    private ImageView ivPrevMonth;
    private ImageView ivNextMonth;
    private RecyclerView rvCalendarGrid;
    private LinearLayout llDayContent;
    private TextView tvSelectedDayTitle;
    private RecyclerView rvDayContent;
    private TabLayout tabLayout;
    private ImageView btnAddSchedule;
    private LinearLayout llDayGoals;
    private TextView tvDayNothing;

    private CalendarDayAdapter calendarDayAdapter;
    private DayContentAdapter dayContentAdapter;
    private ScheduleRepository scheduleRepository;
    private GoalRepository goalRepository;
    private SessionManager sessionManager;

    private Calendar currentMonth;
    private int selectedDay = -1;

    // A date another screen asked us to open (e.g. a tap on the Home week strip);
    // applied the next time the calendar is shown.
    private java.util.Date pendingDate;

    // Bumped on every day load so a slow answer for an earlier tap can't
    // overwrite the panel for the day that is selected now.
    private int dayRequestId = 0;

    public CalendarFragment() {
        // Required empty public constructor
    }

    public static CalendarFragment newInstance() {
        return new CalendarFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        scheduleRepository = new ScheduleRepository();
        goalRepository = new GoalRepository();
        sessionManager = new SessionManager(requireContext());
        currentMonth = Calendar.getInstance();

        initViews(view);
        setupCalendarGrid();
        setupTabs();
        setupFab();
        updateCalendar();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Hidden tabs refresh in onHiddenChanged when they are shown again.
        if (isHidden()) {
            return;
        }
        refreshCalendar();
    }

    /**
     * Fires when this tab is shown or hidden via the MainActivity hide/show
     * pattern (the activity itself never pauses on a tab switch), so changes made
     * elsewhere appear when the user comes back.
     */
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            refreshCalendar();
        }
    }

    /**
     * Opens the calendar on the given date: shows that date's month, highlights
     * the day and shows what is planned on it. Called by MainActivity when the
     * user taps a day on the Home week strip.
     */
    public void showDate(java.util.Date date) {
        pendingDate = date;
        if (isAdded() && getView() != null && !isHidden()) {
            refreshCalendar();
        }
    }

    /**
     * Redraws the grid and markers, then reloads the selected day's panel so a
     * workout or goal added, deleted or finished elsewhere shows up straight away.
     */
    private void refreshCalendar() {
        if (calendarDayAdapter == null) return;

        if (pendingDate != null) {
            Calendar target = Calendar.getInstance();
            target.setTime(pendingDate);
            pendingDate = null;
            currentMonth = target;
            selectedDay = target.get(Calendar.DAY_OF_MONTH);
        }

        updateCalendar();
        calendarDayAdapter.setSelectedDay(selectedDay);
        if (selectedDay > 0) {
            loadDayContent(selectedDay);
        } else {
            llDayContent.setVisibility(View.GONE);
        }
    }

    private void initViews(View view) {
        tvMonthYear = view.findViewById(R.id.tvMonthYear);
        ivPrevMonth = view.findViewById(R.id.ivPrevMonth);
        ivNextMonth = view.findViewById(R.id.ivNextMonth);
        rvCalendarGrid = view.findViewById(R.id.rvCalendarGrid);
        llDayContent = view.findViewById(R.id.llDayContent);
        tvSelectedDayTitle = view.findViewById(R.id.tvSelectedDayTitle);
        rvDayContent = view.findViewById(R.id.rvDayContent);
        tabLayout = view.findViewById(R.id.tabLayoutCalendar);
        btnAddSchedule = view.findViewById(R.id.btnAddSchedule);
        llDayGoals = view.findViewById(R.id.llDayGoals);
        tvDayNothing = view.findViewById(R.id.tvDayNothing);

        ivPrevMonth.setOnClickListener(v -> navigateMonth(-1));
        ivNextMonth.setOnClickListener(v -> navigateMonth(1));
    }

    private void setupCalendarGrid() {
        calendarDayAdapter = new CalendarDayAdapter(this);
        rvCalendarGrid.setLayoutManager(new GridLayoutManager(getContext(), 7));
        rvCalendarGrid.setAdapter(calendarDayAdapter);

        dayContentAdapter = new DayContentAdapter();
        dayContentAdapter.setOnStartClickListener(scheduledWorkout -> {
            if (getContext() == null || scheduledWorkout == null) return;
            Intent intent = new Intent(getContext(), WorkoutExecutionActivity.class);
            intent.putExtra(WorkoutExecutionActivity.EXTRA_SCHEDULED_WORKOUT_ID,
                    scheduledWorkout.getScheduledWorkoutId());
            intent.putExtra(WorkoutExecutionActivity.EXTRA_WORKOUT_ID,
                    scheduledWorkout.getWorkoutId());
            startActivity(intent);
        });
        rvDayContent.setLayoutManager(new LinearLayoutManager(getContext()));
        rvDayContent.setAdapter(dayContentAdapter);
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_my_plan));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_goals));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        showMyPlanTab();
                        break;
                    case 1:
                        showGoalsTab();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // The calendar opens on My Plan (the first tab). It is already selected
        // when added, which does not fire the listener, so show its content here.
        showMyPlanTab();
    }

    private void setupFab() {
        btnAddSchedule.setOnClickListener(v -> showAddOptions());
    }

    private void navigateMonth(int offset) {
        // Set day 1 first so adding a month from the 31st can't skip a month.
        currentMonth.set(Calendar.DAY_OF_MONTH, 1);
        currentMonth.add(Calendar.MONTH, offset);
        selectedDay = -1;
        dayRequestId++;
        // Clear the outline from the old month's selected day.
        calendarDayAdapter.setSelectedDay(-1);
        llDayContent.setVisibility(View.GONE);
        updateCalendar();
    }

    private void updateCalendar() {
        tvMonthYear.setText(DateFormats.formatMonthYear(currentMonth.getTime()));

        List<Integer> days = buildDaysList();

        Calendar today = Calendar.getInstance();
        int todayDay = -1;
        if (today.get(Calendar.YEAR) == currentMonth.get(Calendar.YEAR)
                && today.get(Calendar.MONTH) == currentMonth.get(Calendar.MONTH)) {
            todayDay = today.get(Calendar.DAY_OF_MONTH);
        }

        calendarDayAdapter.setDays(days, todayDay);
        loadMonthIndicators();
    }

    private List<Integer> buildDaysList() {
        List<Integer> days = new ArrayList<>();

        Calendar cal = (Calendar) currentMonth.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);

        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Weeks start on Monday: pad with blanks for the days before the 1st
        // (0 blanks if the 1st is a Monday, 6 if it's a Sunday).
        int leadingBlanks = DateFormats.daysSinceMonday(cal.get(Calendar.DAY_OF_WEEK));
        for (int i = 0; i < leadingBlanks; i++) {
            days.add(0);
        }

        for (int day = 1; day <= daysInMonth; day++) {
            days.add(day);
        }

        return days;
    }

    private void loadMonthIndicators() {
        int userId = sessionManager.getUserId();
        if (userId == -1) return;

        int year = currentMonth.get(Calendar.YEAR);
        int month = currentMonth.get(Calendar.MONTH) + 1;

        scheduleRepository.getScheduledWorkoutsForMonth(userId, year, month,
                new ConnectionManager.DatabaseCallback<List<ScheduledWorkout>>() {
                    @Override
                    public void onSuccess(List<ScheduledWorkout> result) {
                        if (getActivity() == null || !isAdded()) return;
                        Set<Integer> workoutDays = new HashSet<>();
                        if (result != null) {
                            for (ScheduledWorkout sw : result) {
                                if (sw.getScheduledDate() != null) {
                                    Calendar cal = Calendar.getInstance();
                                    cal.setTime(sw.getScheduledDate());
                                    workoutDays.add(cal.get(Calendar.DAY_OF_MONTH));
                                }
                            }
                        }
                        getActivity().runOnUiThread(() ->
                                calendarDayAdapter.setWorkoutDays(workoutDays));
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "loadMonthIndicators: database call failed", e);
                    }
                });

        goalRepository.getUserGoals(userId, new ConnectionManager.DatabaseCallback<List<Goal>>() {
            @Override
            public void onSuccess(List<Goal> result) {
                if (getActivity() == null || !isAdded()) return;
                Set<Integer> goalDaysSet = new HashSet<>();
                if (result != null) {
                    for (Goal goal : result) {
                        if (goal.getDeadline() != null && !goal.isCompleted()) {
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(goal.getDeadline());
                            if (cal.get(Calendar.YEAR) == currentMonth.get(Calendar.YEAR)
                                    && cal.get(Calendar.MONTH) == currentMonth.get(Calendar.MONTH)) {
                                goalDaysSet.add(cal.get(Calendar.DAY_OF_MONTH));
                            }
                        }
                    }
                }
                getActivity().runOnUiThread(() ->
                        calendarDayAdapter.setGoalDays(goalDaysSet));
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "loadMonthIndicators: database call failed", e);
            }
        });
    }

    @Override
    public void onDayClick(int day) {
        selectedDay = day;
        loadDayContent(day);
    }

    /**
     * Fills the panel under the grid for one day: that day's workouts, the goals
     * due on it, or a "Nothing planned" message when there are neither.
     */
    private void loadDayContent(int day) {
        int userId = sessionManager.getUserId();
        if (userId == -1) return;

        Calendar cal = (Calendar) currentMonth.clone();
        cal.set(Calendar.DAY_OF_MONTH, day);
        java.sql.Date selectedDate = new java.sql.Date(cal.getTimeInMillis());

        tvSelectedDayTitle.setText(DateFormats.formatShort(cal.getTime()));

        final int requestId = ++dayRequestId;

        scheduleRepository.getScheduledContentForDay(userId, selectedDate,
                new ConnectionManager.DatabaseCallback<List<ScheduledWorkout>>() {
                    @Override
                    public void onSuccess(List<ScheduledWorkout> workouts) {
                        if (getActivity() == null || !isAdded()) return;
                        loadLogsForDay(userId, cal, selectedDate, workouts, requestId);
                    }

                    @Override
                    public void onError(Exception e) {
                        if (getActivity() == null || !isAdded()) return;
                        getActivity().runOnUiThread(() -> {
                            if (requestId == dayRequestId) {
                                llDayContent.setVisibility(View.GONE);
                            }
                        });
                    }
                });
    }

    /**
     * Second step of loadDayContent: fetches what was logged for the day's completed
     * workouts (shown as what was actually done). If that fails the workouts just
     * show their plan.
     */
    private void loadLogsForDay(int userId, Calendar day, java.sql.Date selectedDate,
                                List<ScheduledWorkout> workouts, int requestId) {
        List<Integer> completedIds = new ArrayList<>();
        if (workouts != null) {
            for (ScheduledWorkout sw : workouts) {
                if (sw.isCompleted()) completedIds.add(sw.getScheduledWorkoutId());
            }
        }
        new ExerciseLogRepository().getLogsForScheduledWorkouts(completedIds,
                new ConnectionManager.DatabaseCallback<Map<Integer, Map<Integer, ExerciseResult>>>() {
                    @Override
                    public void onSuccess(Map<Integer, Map<Integer, ExerciseResult>> logs) {
                        if (getActivity() == null || !isAdded()) return;
                        loadGoalsForDay(userId, day, selectedDate, workouts, logs, requestId);
                    }

                    @Override
                    public void onError(Exception e) {
                        if (getActivity() == null || !isAdded()) return;
                        loadGoalsForDay(userId, day, selectedDate, workouts, new HashMap<>(), requestId);
                    }
                });
    }

    /** Third step of loadDayContent: finds the goals due on the day, then draws the panel. */
    private void loadGoalsForDay(int userId, Calendar day, java.sql.Date selectedDate,
                                 List<ScheduledWorkout> workouts,
                                 Map<Integer, Map<Integer, ExerciseResult>> logs, int requestId) {
        goalRepository.getUserGoals(userId, new ConnectionManager.DatabaseCallback<List<Goal>>() {
            @Override
            public void onSuccess(List<Goal> goals) {
                List<Goal> due = new ArrayList<>();
                if (goals != null) {
                    for (Goal goal : goals) {
                        if (goal.getDeadline() != null && !goal.isCompleted() && isSameDay(goal.getDeadline(), day)) {
                            due.add(goal);
                        }
                    }
                }
                showDayContent(workouts, due, selectedDate, logs, requestId);
            }

            @Override
            public void onError(Exception e) {
                // The workouts still show; the goals for this day are just left out.
                showDayContent(workouts, new ArrayList<>(), selectedDate, logs, requestId);
            }
        });
    }

    private void showDayContent(List<ScheduledWorkout> workouts, List<Goal> goalsDue,
                                java.sql.Date selectedDate,
                                Map<Integer, Map<Integer, ExerciseResult>> logs, int requestId) {
        if (getActivity() == null || !isAdded()) return;
        getActivity().runOnUiThread(() -> {
            // A newer tap or a month change has replaced this request.
            if (requestId != dayRequestId || !isAdded()) return;

            boolean hasWorkouts = workouts != null && !workouts.isEmpty();
            boolean hasGoals = !goalsDue.isEmpty();

            llDayContent.setVisibility(View.VISIBLE);

            if (hasWorkouts) {
                rvDayContent.setVisibility(View.VISIBLE);
                dayContentAdapter.setSelectedDate(selectedDate);
                dayContentAdapter.setLogs(logs);
                dayContentAdapter.setItems(workouts);
            } else {
                rvDayContent.setVisibility(View.GONE);
            }

            llDayGoals.removeAllViews();
            for (Goal goal : goalsDue) {
                View row = LayoutInflater.from(requireContext())
                        .inflate(R.layout.item_day_goal, llDayGoals, false);
                ((TextView) row.findViewById(R.id.tvDayGoalTitle)).setText(goal.getTitle());
                GoalPriorityStyle.apply(row.findViewById(R.id.tvDayGoalPriority), goal.getPriority());

                GradientDrawable diamond = new GradientDrawable();
                diamond.setShape(GradientDrawable.RECTANGLE);
                diamond.setColor(ContextCompat.getColor(requireContext(), R.color.red));
                row.findViewById(R.id.viewDayGoalMarker).setBackground(diamond);

                llDayGoals.addView(row);
            }

            tvDayNothing.setVisibility(hasWorkouts || hasGoals ? View.GONE : View.VISIBLE);
        });
    }

    private static boolean isSameDay(java.util.Date date, Calendar day) {
        Calendar other = Calendar.getInstance();
        other.setTime(date);
        return other.get(Calendar.YEAR) == day.get(Calendar.YEAR)
                && other.get(Calendar.DAY_OF_YEAR) == day.get(Calendar.DAY_OF_YEAR);
    }

    private void showMyPlanTab() {
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.flTabContent, WeeklyPlanFragment.newInstance())
                .commit();
    }

    private void showGoalsTab() {
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.flTabContent, GoalsListFragment.newInstance())
                .commit();
    }

    private void showAddOptions() {
        if (getContext() == null) return;

        String[] options = {
                getString(R.string.calendar_option_schedule_workout),
                getString(R.string.calendar_option_create_goal)
        };

        new MaterialAlertDialogBuilder(getContext())
                .setTitle(R.string.calendar_add_title)
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            navigateToScheduleWorkout();
                            break;
                        case 1:
                            navigateToCreateGoal();
                            break;
                    }
                })
                .show();
    }

    private void navigateToScheduleWorkout() {
        Intent intent = new Intent(getContext(), com.jve.repsup.activities.ScheduleWorkoutActivity.class);
        startActivity(intent);
    }

    private void navigateToCreateGoal() {
        Intent intent = new Intent(getContext(), CreateGoalActivity.class);
        startActivity(intent);
    }
}
