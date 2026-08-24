package com.jve.repsup.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.jve.repsup.R;
import com.jve.repsup.models.ScheduledWorkout;
import com.jve.repsup.models.WorkoutExercise;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * RecyclerView adapter for the collapsible weekly plan view.
 * Displays day headers (Monday–Sunday) that can be expanded/collapsed,
 * with workout cards showing name, exercise count, and status.
 */
public class WeeklyPlanAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_DAY_HEADER = 0;
    private static final int VIEW_TYPE_WORKOUT_CARD = 1;

    private final List<ListItem> displayItems = new ArrayList<>();
    private final Map<Integer, Boolean> collapsedDays = new HashMap<>();
    private final List<DayData> weekDays = new ArrayList<>();
    private final OnStartClickListener startClickListener;

    private Date today;

    /**
     * Listener for the START button tap on today's workout.
     */
    public interface OnStartClickListener {
        void onStartClick(ScheduledWorkout scheduledWorkout);
    }

    public WeeklyPlanAdapter(OnStartClickListener listener) {
        this.startClickListener = listener;
        this.today = new Date(System.currentTimeMillis());
    }

    /**
     * Sets the week data. weekStart is the Monday of the week to display.
     * scheduledWorkouts contains all scheduled workouts for that week.
     * Past days are automatically collapsed; today and future days are expanded.
     */
    public void setWeekData(Date weekStart, List<ScheduledWorkout> scheduledWorkouts) {
        weekDays.clear();
        collapsedDays.clear();
        this.today = new Date(System.currentTimeMillis());

        // Build 7 days starting from weekStart (Monday)
        Calendar cal = Calendar.getInstance();
        cal.setTime(weekStart);

        SimpleDateFormat dayNameFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
        SimpleDateFormat dayDateFormat = new SimpleDateFormat("MMM d", Locale.getDefault());

        for (int i = 0; i < 7; i++) {
            Date dayDate = new Date(cal.getTimeInMillis());
            String dayName = dayNameFormat.format(dayDate);
            String dayDateStr = dayDateFormat.format(dayDate);

            // Find workouts for this day
            List<ScheduledWorkout> dayWorkouts = new ArrayList<>();
            if (scheduledWorkouts != null) {
                for (ScheduledWorkout sw : scheduledWorkouts) {
                    if (sw.getScheduledDate() != null && isSameDay(sw.getScheduledDate(), dayDate)) {
                        dayWorkouts.add(sw);
                    }
                }
            }

            DayData dayData = new DayData(i, dayName, dayDateStr, dayDate, dayWorkouts);
            weekDays.add(dayData);

            // Auto-collapse past days (before today)
            boolean isPast = dayDate.before(today) && !isSameDay(dayDate, today);
            if (isPast) {
                collapsedDays.put(i, true);
            }

            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        rebuildDisplayList();
    }

    /**
     * Returns the adapter position of today's day header, or -1 if not found.
     */
    public int getTodayPosition() {
        for (int i = 0; i < displayItems.size(); i++) {
            ListItem item = displayItems.get(i);
            if (item.viewType == VIEW_TYPE_DAY_HEADER && isSameDay(item.dayData.date, today)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Rebuilds the flat display list from the week day data,
     * respecting collapsed/expanded state of each day.
     */
    private void rebuildDisplayList() {
        displayItems.clear();

        for (DayData day : weekDays) {
            boolean isCollapsed = Boolean.TRUE.equals(collapsedDays.get(day.dayIndex));
            displayItems.add(new ListItem(VIEW_TYPE_DAY_HEADER, day, null, isCollapsed));

            if (!isCollapsed) {
                if (day.workouts.isEmpty()) {
                    // Show a "rest day" placeholder workout item
                    displayItems.add(new ListItem(VIEW_TYPE_WORKOUT_CARD, day, null, false));
                } else {
                    for (ScheduledWorkout sw : day.workouts) {
                        displayItems.add(new ListItem(VIEW_TYPE_WORKOUT_CARD, day, sw, false));
                    }
                }
            }
        }

        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return displayItems.get(position).viewType;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_DAY_HEADER) {
            View view = inflater.inflate(R.layout.item_weekly_plan_day_header, parent, false);
            return new DayHeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_weekly_plan_workout, parent, false);
            return new WorkoutCardViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ListItem item = displayItems.get(position);

        if (item.viewType == VIEW_TYPE_DAY_HEADER) {
            bindDayHeader((DayHeaderViewHolder) holder, item);
        } else {
            bindWorkoutCard((WorkoutCardViewHolder) holder, item);
        }
    }

    private void bindDayHeader(DayHeaderViewHolder holder, ListItem item) {
        DayData day = item.dayData;
        boolean isToday = isSameDay(day.date, today);

        holder.tvDayName.setText(day.dayName + (isToday ? " (Today)" : ""));
        holder.tvDayDate.setText(day.dayDateStr);

        // Highlight today's header
        if (isToday) {
            holder.tvDayName.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.light_blue));
        } else {
            holder.tvDayName.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
        }

        // Rotate arrow based on collapsed state
        holder.ivDayExpand.setRotation(item.isCollapsed ? 180f : 0f);

        holder.itemView.setOnClickListener(v -> {
            boolean currentlyCollapsed = Boolean.TRUE.equals(collapsedDays.get(day.dayIndex));
            collapsedDays.put(day.dayIndex, !currentlyCollapsed);
            rebuildDisplayList();
        });
    }

    private void bindWorkoutCard(WorkoutCardViewHolder holder, ListItem item) {
        Context context = holder.itemView.getContext();
        DayData day = item.dayData;
        ScheduledWorkout sw = item.scheduledWorkout;

        // Clear previous exercise views
        holder.llExerciseList.removeAllViews();
        holder.tvAndMore.setVisibility(View.GONE);

        if (sw == null) {
            // Rest day - no workout scheduled
            holder.tvWorkoutName.setText(R.string.weekly_plan_rest_day);
            holder.tvWorkoutName.setTextColor(ContextCompat.getColor(context, R.color.muted));
            holder.llExerciseList.setVisibility(View.GONE);
            holder.tvStatus.setVisibility(View.GONE);
            holder.btnStart.setVisibility(View.GONE);
            return;
        }

        holder.llExerciseList.setVisibility(View.VISIBLE);

        // Workout name
        String workoutName = sw.getWorkout() != null ? sw.getWorkout().getName() : "Workout";
        holder.tvWorkoutName.setText(workoutName);
        holder.tvWorkoutName.setTextColor(ContextCompat.getColor(context, R.color.white));

        // Exercise list - show up to 3, then "and X more"
        if (sw.getWorkout() != null && sw.getWorkout().getExercises() != null
                && !sw.getWorkout().getExercises().isEmpty()) {
            List<WorkoutExercise> exercises = sw.getWorkout().getExercises();
            int totalExercises = exercises.size();
            int showCount = Math.min(3, totalExercises);

            LayoutInflater inflater = LayoutInflater.from(context);
            for (int i = 0; i < showCount; i++) {
                View exerciseRow = inflater.inflate(R.layout.item_workout_exercise_row, holder.llExerciseList, false);
                TextView tvName = exerciseRow.findViewById(R.id.tvExerciseName);
                TextView tvSets = exerciseRow.findViewById(R.id.tvExerciseSets);
                ImageView ivImage = exerciseRow.findViewById(R.id.ivExerciseImage);

                WorkoutExercise we = exercises.get(i);
                String exerciseName = we.getExercise() != null ? we.getExercise().getName() : "Exercise";
                tvName.setText(exerciseName);
                tvSets.setText(context.getString(R.string.home_sets_format, we.getSets()));

                // Load exercise image from drawable resources based on exercise name
                loadExerciseImage(ivImage, exerciseName);

                holder.llExerciseList.addView(exerciseRow);
            }

            // "and X more" text
            if (totalExercises > 3) {
                int remaining = totalExercises - 3;
                holder.tvAndMore.setText(context.getString(R.string.weekly_plan_and_more_format, remaining));
                holder.tvAndMore.setVisibility(View.VISIBLE);

                // Make "and X more" clickable to expand
                holder.tvAndMore.setOnClickListener(v -> {
                    // Add remaining exercises
                    for (int i = 3; i < totalExercises; i++) {
                        View exerciseRow = inflater.inflate(R.layout.item_workout_exercise_row, holder.llExerciseList, false);
                        TextView tvName = exerciseRow.findViewById(R.id.tvExerciseName);
                        TextView tvSets = exerciseRow.findViewById(R.id.tvExerciseSets);
                        ImageView ivImage = exerciseRow.findViewById(R.id.ivExerciseImage);

                        WorkoutExercise we = exercises.get(i);
                        String exerciseName = we.getExercise() != null ? we.getExercise().getName() : "Exercise";
                        tvName.setText(exerciseName);
                        tvSets.setText(context.getString(R.string.home_sets_format, we.getSets()));

                        loadExerciseImage(ivImage, exerciseName);

                        holder.llExerciseList.addView(exerciseRow);
                    }
                    holder.tvAndMore.setVisibility(View.GONE);
                });
            }
        }

        // Determine status
        boolean isToday = isSameDay(day.date, today);
        boolean isPast = day.date.before(today) && !isToday;

        if (isToday && !sw.isCompleted()) {
            // Today and not completed: show START button
            holder.tvStatus.setVisibility(View.GONE);
            holder.btnStart.setVisibility(View.VISIBLE);
            holder.btnStart.setOnClickListener(v -> {
                if (startClickListener != null) {
                    startClickListener.onStartClick(sw);
                }
            });
        } else if (sw.isCompleted()) {
            // Completed
            holder.tvStatus.setText(R.string.weekly_plan_status_completed);
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.light_blue));
            holder.tvStatus.setVisibility(View.VISIBLE);
            holder.btnStart.setVisibility(View.GONE);
        } else if (isPast) {
            // Past and not completed = Missed
            holder.tvStatus.setText(R.string.weekly_plan_status_missed);
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.red));
            holder.tvStatus.setVisibility(View.VISIBLE);
            holder.btnStart.setVisibility(View.GONE);
        } else {
            // Future: Scheduled
            holder.tvStatus.setText(R.string.weekly_plan_status_scheduled);
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.muted));
            holder.tvStatus.setVisibility(View.VISIBLE);
            holder.btnStart.setVisibility(View.GONE);
        }
    }

    /**
     * Loads an exercise image from drawable resources based on exercise name.
     * Converts name to a resource-safe format (lowercase, spaces to underscores).
     */
    private void loadExerciseImage(ImageView imageView, String exerciseName) {
        if (exerciseName != null && !exerciseName.isEmpty()) {
            Context context = imageView.getContext();
            String resName = "exercise_" + exerciseName.toLowerCase()
                    .replaceAll("[^a-z0-9]", "_")
                    .replaceAll("_+", "_");
            int resId = context.getResources().getIdentifier(
                    resName, "drawable", context.getPackageName());
            if (resId != 0) {
                imageView.setImageResource(resId);
            }
        }
    }

    @Override
    public int getItemCount() {
        return displayItems.size();
    }

    /**
     * Checks if two dates fall on the same calendar day.
     */
    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    // --- ViewHolders ---

    static class DayHeaderViewHolder extends RecyclerView.ViewHolder {
        final TextView tvDayName;
        final TextView tvDayDate;
        final ImageView ivDayExpand;

        DayHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayName = itemView.findViewById(R.id.tvDayName);
            tvDayDate = itemView.findViewById(R.id.tvDayDate);
            ivDayExpand = itemView.findViewById(R.id.ivDayExpand);
        }
    }

    static class WorkoutCardViewHolder extends RecyclerView.ViewHolder {
        final TextView tvWorkoutName;
        final LinearLayout llExerciseList;
        final TextView tvAndMore;
        final TextView tvStatus;
        final TextView btnStart;

        WorkoutCardViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWorkoutName = itemView.findViewById(R.id.tvWorkoutName);
            llExerciseList = itemView.findViewById(R.id.llExerciseList);
            tvAndMore = itemView.findViewById(R.id.tvAndMore);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnStart = itemView.findViewById(R.id.btnStart);
        }
    }

    // --- Internal data models ---

    /**
     * Holds data for a single day in the week.
     */
    private static class DayData {
        final int dayIndex;
        final String dayName;
        final String dayDateStr;
        final Date date;
        final List<ScheduledWorkout> workouts;

        DayData(int dayIndex, String dayName, String dayDateStr, Date date, List<ScheduledWorkout> workouts) {
            this.dayIndex = dayIndex;
            this.dayName = dayName;
            this.dayDateStr = dayDateStr;
            this.date = date;
            this.workouts = workouts;
        }
    }

    /**
     * Represents a single item in the flat display list.
     */
    private static class ListItem {
        final int viewType;
        final DayData dayData;
        final ScheduledWorkout scheduledWorkout;
        final boolean isCollapsed;

        ListItem(int viewType, DayData dayData, ScheduledWorkout scheduledWorkout, boolean isCollapsed) {
            this.viewType = viewType;
            this.dayData = dayData;
            this.scheduledWorkout = scheduledWorkout;
            this.isCollapsed = isCollapsed;
        }
    }
}
