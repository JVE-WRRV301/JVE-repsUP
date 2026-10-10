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

import com.jve.repsup.MainActivity;
import com.jve.repsup.R;
import com.jve.repsup.utils.DateFormats;
import com.jve.repsup.utils.GoalPriorityStyle;
import com.jve.repsup.utils.SelectableButtonStyler;
import com.jve.repsup.activities.WorkoutExecutionActivity;
import com.jve.repsup.database.ConnectionManager;
import com.jve.repsup.utils.DialogUtils;
import com.jve.repsup.models.Goal;
import com.jve.repsup.models.ScheduledWorkout;
import com.jve.repsup.models.WorkoutExercise;
import com.jve.repsup.repositories.GoalRepository;
import com.jve.repsup.repositories.ScheduleRepository;
import com.jve.repsup.utils.SessionManager;

import java.util.Calendar;
import java.util.List;

/**
 * HomeFragment displaying today's scheduled workout and the next upcoming goal.
 * Shows a week strip with today highlighted, a TODAY card (blue border) with
 * workout details and START button, and a NEXT GOAL card (red border).
 */
public class HomeFragment extends Fragment {

    private TextView tvGreeting;
    private LinearLayout llWeekStrip;
    private LinearLayout llTodayContent;
    private TextView tvTodayEmpty;
    private TextView tvWorkoutName;
    private TextView tvWorkoutDate;
    private LinearLayout llExerciseList;
    private TextView btnStart;
    private TextView tvMoreExercises;
    private TextView tvMoreWorkouts;

    private LinearLayout llGoalContent;
    private TextView tvGoalEmpty;
    private TextView tvGoalDeadline;
    private TextView tvGoalTitle;
    private TextView tvGoalPriority;
    private TextView tvGoalDescription;

    private ScheduleRepository scheduleRepository;
    private GoalRepository goalRepository;
    private SessionManager sessionManager;

    // The workout the card is showing (the first unfinished one today).
    private ScheduledWorkout todaysWorkout;

    // How many exercises the card lists before it switches to "+N more".
    private static final int MAX_EXERCISES_SHOWN = 3;

    // The scheduled workout whose card is showing every exercise ("+N more" tapped), or -1.
    private int expandedWorkoutId = -1;
    // What the card is showing now, so a tap on "+N more" / "Show less" can redraw it.
    private ScheduledWorkout shownWorkout;
    private int shownOtherWorkouts;

    // True once a load error has been shown and until a load succeeds again, so
    // a database outage gives one message, not one per card or per tab switch.
    private boolean loadErrorShown = false;

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
        // Hidden tabs refresh in onHiddenChanged when they are shown again.
        if (isHidden()) {
            return;
        }
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
        tvGreeting = view.findViewById(R.id.tvGreeting);
        showGreeting();

        llWeekStrip = view.findViewById(R.id.llWeekStrip);

        // Today card views
        llTodayContent = view.findViewById(R.id.llTodayContent);
        tvTodayEmpty = view.findViewById(R.id.tvTodayEmpty);
        tvWorkoutName = view.findViewById(R.id.tvWorkoutName);
        tvWorkoutDate = view.findViewById(R.id.tvWorkoutDate);
        llExerciseList = view.findViewById(R.id.llExerciseList);
        btnStart = view.findViewById(R.id.btnStart);
        tvMoreExercises = view.findViewById(R.id.tvMoreExercises);
        tvMoreWorkouts = view.findViewById(R.id.tvMoreWorkouts);

        // Goal card views
        llGoalContent = view.findViewById(R.id.llGoalContent);
        tvGoalEmpty = view.findViewById(R.id.tvGoalEmpty);
        tvGoalDeadline = view.findViewById(R.id.tvGoalDeadline);
        tvGoalTitle = view.findViewById(R.id.tvGoalTitle);
        tvGoalPriority = view.findViewById(R.id.tvGoalPriority);
        tvGoalDescription = view.findViewById(R.id.tvGoalDescription);

        btnStart.setOnClickListener(v -> startWorkout());
        tvMoreWorkouts.setOnClickListener(v -> openCalendarOn(Calendar.getInstance().getTime()));
    }

    /**
     * Shows the connection error once. It stays quiet until a load succeeds
     * again, so the two cards failing together (or the user switching tabs while
     * the database is down) don't stack up dialogs.
     */
    private void showLoadErrorOnce() {
        if (loadErrorShown) return;
        loadErrorShown = true;
        DialogUtils.showError(getContext(), R.string.login_connection_error);
    }

    /** Opens the Calendar tab on the given day. */
    private void openCalendarOn(java.util.Date date) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).openCalendarOnDate(date);
        }
    }

    /**
     * Shows a time-of-day greeting using only the first word of the user's
     * first name, based on the phone's current time.
     */
    private void showGreeting() {
        String storedName = sessionManager.getName();
        String firstWord = "";
        if (storedName != null) {
            String trimmed = storedName.trim();
            if (!trimmed.isEmpty()) {
                firstWord = trimmed.split("\\s+")[0];
            }
        }

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int greetingResId;
        if (hour < 12) {
            greetingResId = R.string.home_greeting_morning;
        } else if (hour < 18) {
            greetingResId = R.string.home_greeting_afternoon;
        } else {
            greetingResId = R.string.home_greeting_evening;
        }

        tvGreeting.setText(getString(greetingResId, firstWord));
    }

    /**
     * Populates the week strip with dates for the current week
     * and highlights today's column with light_blue color.
     */
    private void highlightToday() {
        Calendar calendar = Calendar.getInstance();
        // The strip runs Monday to Sunday, so today's column is how many
        // days today is after Monday (Monday = 0 … Sunday = 6).
        int todayIndex = DateFormats.daysSinceMonday(calendar.get(Calendar.DAY_OF_WEEK));

        // Start of the week (Monday)
        Calendar weekStart = (Calendar) calendar.clone();
        weekStart.add(Calendar.DAY_OF_YEAR, -todayIndex);

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

                    // Tapping a day opens the Calendar on that day.
                    java.util.Date dayDate = dayCalendar.getTime();
                    dayColumn.setClickable(true);
                    dayColumn.setFocusable(true);
                    dayColumn.setBackgroundResource(selectableBackground());
                    dayColumn.setContentDescription(getString(R.string.home_open_day_format,
                            DateFormats.formatShort(dayDate)));
                    dayColumn.setOnClickListener(v -> openCalendarOn(dayDate));

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

    /** Resource id of the standard tap-ripple background. */
    private int selectableBackground() {
        android.util.TypedValue outValue = new android.util.TypedValue();
        requireContext().getTheme().resolveAttribute(
                android.R.attr.selectableItemBackground, outValue, true);
        return outValue.resourceId;
    }

    /**
     * Loads today's scheduled workouts from the database.
     */
    private void loadTodaysWorkout() {
        int userId = sessionManager.getUserId();
        if (userId == -1) return;

        scheduleRepository.getTodaysWorkouts(userId, new ConnectionManager.DatabaseCallback<List<ScheduledWorkout>>() {
            @Override
            public void onSuccess(List<ScheduledWorkout> result) {
                if (getActivity() == null || !isAdded()) return;
                getActivity().runOnUiThread(() -> {
                    if (!isAdded()) return;
                    loadErrorShown = false;
                    if (result != null && !result.isEmpty() && result.get(0).getWorkout() != null) {
                        todaysWorkout = result.get(0);
                        showWorkoutCard(todaysWorkout, result.size() - 1);
                    } else {
                        todaysWorkout = null;
                        showRestDayState();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                if (getActivity() == null || !isAdded()) return;
                getActivity().runOnUiThread(() -> {
                    if (!isAdded()) return;
                    todaysWorkout = null;
                    showRestDayState();
                    showLoadErrorOnce();
                });
            }
        });
    }

    /**
     * Displays the workout card with name, date, and the first few exercises
     * (with a "+N more" line for the rest). Shows "Completed" status if the
     * workout has already been finished. otherWorkouts is how many more workouts
     * are scheduled today; if there are any, a tappable "+N more workout today"
     * line opens today in the Calendar.
     */
    private void showWorkoutCard(ScheduledWorkout scheduledWorkout, int otherWorkouts) {
        shownWorkout = scheduledWorkout;
        shownOtherWorkouts = otherWorkouts;
        boolean expanded = scheduledWorkout.getScheduledWorkoutId() == expandedWorkoutId;

        llTodayContent.setVisibility(View.VISIBLE);
        tvTodayEmpty.setVisibility(View.GONE);

        tvWorkoutName.setText(scheduledWorkout.getWorkout().getName());

        tvWorkoutDate.setText(DateFormats.formatShort(scheduledWorkout.getScheduledDate()));

        // Show START button or Completed status
        if (scheduledWorkout.isCompleted()) {
            btnStart.setText(R.string.calendar_status_completed);
            // Shared disabled look (grey, muted text) instead of a faded button.
            SelectableButtonStyler.applyEnabled(btnStart, false);
        } else {
            btnStart.setText(R.string.home_start);
            SelectableButtonStyler.applyEnabled(btnStart, true);
        }

        // Populate exercise list
        llExerciseList.removeAllViews();
        List<WorkoutExercise> exercises = scheduledWorkout.getWorkout().getExercises();
        int hiddenExercises = 0;
        if (exercises != null) {
            int shown = expanded ? exercises.size() : Math.min(exercises.size(), MAX_EXERCISES_SHOWN);
            hiddenExercises = exercises.size() - Math.min(exercises.size(), MAX_EXERCISES_SHOWN);
            for (WorkoutExercise we : exercises.subList(0, shown)) {
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

        if (hiddenExercises > 0) {
            // Tapping the line shows every exercise; tapping "Show less" folds it back.
            tvMoreExercises.setText(expanded
                    ? getString(R.string.workout_show_less)
                    : getString(R.string.home_more_exercises_format, hiddenExercises));
            tvMoreExercises.setVisibility(View.VISIBLE);
            tvMoreExercises.setOnClickListener(v -> {
                if (shownWorkout == null) return;
                int id = shownWorkout.getScheduledWorkoutId();
                expandedWorkoutId = expandedWorkoutId == id ? -1 : id;
                showWorkoutCard(shownWorkout, shownOtherWorkouts);
            });
        } else {
            tvMoreExercises.setVisibility(View.GONE);
        }

        if (otherWorkouts > 0) {
            tvMoreWorkouts.setText(getResources().getQuantityString(
                    R.plurals.home_more_workouts_today, otherWorkouts, otherWorkouts));
            tvMoreWorkouts.setVisibility(View.VISIBLE);
        } else {
            tvMoreWorkouts.setVisibility(View.GONE);
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
                    if (!isAdded()) return;
                    loadErrorShown = false;
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
                    if (!isAdded()) return;
                    showNoGoalsState();
                    showLoadErrorOnce();
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

        tvGoalDeadline.setText(getString(R.string.home_deadline_format,
                DateFormats.formatLong(goal.getDeadline())));

        // Title and notes come from separate columns now: the heading shows the
        // title and the subheading shows the optional notes.
        String title = goal.getTitle();
        String notes = goal.getDescription() != null ? goal.getDescription() : "";

        tvGoalTitle.setText(title);
        // Same label and colour as on the Goals list.
        GoalPriorityStyle.apply(tvGoalPriority, goal.getPriority());

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
