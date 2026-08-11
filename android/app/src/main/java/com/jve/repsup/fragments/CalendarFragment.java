package com.jve.repsup.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.jve.repsup.R;
import com.jve.repsup.activities.CreateGoalActivity;
import com.jve.repsup.adapters.CalendarDayAdapter;
import com.jve.repsup.adapters.DayContentAdapter;
import com.jve.repsup.database.ConnectionManager;
import com.jve.repsup.models.Goal;
import com.jve.repsup.models.ScheduledWorkout;
import com.jve.repsup.repositories.GoalRepository;
import com.jve.repsup.repositories.ScheduleRepository;
import com.jve.repsup.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * CalendarFragment displaying the monthly calendar grid with workout and goal indicators,
 * "My Plan" and "Goals" tabs, and a FAB for scheduling workouts or creating goals.
 */
public class CalendarFragment extends Fragment implements CalendarDayAdapter.OnDayClickListener {

    private TextView tvMonthYear;
    private ImageView ivPrevMonth;
    private ImageView ivNextMonth;
    private RecyclerView rvCalendarGrid;
    private LinearLayout llDayContent;
    private TextView tvSelectedDayTitle;
    private RecyclerView rvDayContent;
    private TabLayout tabLayout;
    private FloatingActionButton fabAdd;

    private CalendarDayAdapter calendarDayAdapter;
    private DayContentAdapter dayContentAdapter;
    private ScheduleRepository scheduleRepository;
    private GoalRepository goalRepository;
    private SessionManager sessionManager;

    private Calendar currentMonth;
    private int selectedDay = -1;

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
        updateCalendar();
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
        fabAdd = view.findViewById(R.id.fabAdd);

        ivPrevMonth.setOnClickListener(v -> navigateMonth(-1));
        ivNextMonth.setOnClickListener(v -> navigateMonth(1));
    }

    private void setupCalendarGrid() {
        calendarDayAdapter = new CalendarDayAdapter(this);
        rvCalendarGrid.setLayoutManager(new GridLayoutManager(getContext(), 7));
        rvCalendarGrid.setAdapter(calendarDayAdapter);

        dayContentAdapter = new DayContentAdapter();
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

        TabLayout.Tab goalsTab = tabLayout.getTabAt(1);
        if (goalsTab != null) {
            goalsTab.select();
        }
    }

    private void setupFab() {
        fabAdd.setOnClickListener(v -> showAddOptions());
    }

    private void navigateMonth(int offset) {
        currentMonth.add(Calendar.MONTH, offset);
        selectedDay = -1;
        llDayContent.setVisibility(View.GONE);
        updateCalendar();
    }

    private void updateCalendar() {
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        tvMonthYear.setText(monthFormat.format(currentMonth.getTime()));

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

        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 1; i < firstDayOfWeek; i++) {
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
                        if (getActivity() == null) return;
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
                    }
                });

        goalRepository.getUserGoals(userId, new ConnectionManager.DatabaseCallback<List<Goal>>() {
            @Override
            public void onSuccess(List<Goal> result) {
                if (getActivity() == null) return;
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
            }
        });
    }

    @Override
    public void onDayClick(int day) {
        selectedDay = day;

        Calendar cal = (Calendar) currentMonth.clone();
        cal.set(Calendar.DAY_OF_MONTH, day);
        java.sql.Date selectedDate = new java.sql.Date(cal.getTimeInMillis());

        SimpleDateFormat titleFormat = new SimpleDateFormat("EEEE, MMM d", Locale.getDefault());
        tvSelectedDayTitle.setText(titleFormat.format(cal.getTime()));

        int userId = sessionManager.getUserId();
        if (userId == -1) return;

        scheduleRepository.getScheduledContentForDay(userId, selectedDate,
                new ConnectionManager.DatabaseCallback<List<ScheduledWorkout>>() {
                    @Override
                    public void onSuccess(List<ScheduledWorkout> result) {
                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(() -> {
                            if (result != null && !result.isEmpty()) {
                                llDayContent.setVisibility(View.VISIBLE);
                                dayContentAdapter.setItems(result);
                            } else {
                                llDayContent.setVisibility(View.GONE);
                            }
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(() -> llDayContent.setVisibility(View.GONE));
                    }
                });
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

        new AlertDialog.Builder(getContext())
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

    public static class PlaceholderFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            TextView tv = new TextView(getContext());
            tv.setText(R.string.calendar_my_plan_placeholder);
            tv.setTextColor(getResources().getColor(R.color.muted, null));
            tv.setGravity(android.view.Gravity.CENTER);
            tv.setPadding(32, 64, 32, 64);
            return tv;
        }
    }
}
