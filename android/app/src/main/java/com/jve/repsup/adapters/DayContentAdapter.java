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
import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for displaying scheduled workout cards for a selected day.
 * Shows workout name, exercise count, exercise previews (up to 3),
 * completion status, and a START button for today's workouts.
 */
public class DayContentAdapter extends RecyclerView.Adapter<DayContentAdapter.ContentViewHolder> {

    private static final int MAX_PREVIEW_EXERCISES = 3;

    private List<ScheduledWorkout> items = new ArrayList<>();
    private OnStartClickListener startClickListener;
    private Date selectedDate;

    public interface OnStartClickListener {
        void onStartClick(ScheduledWorkout scheduledWorkout);
    }

    public void setOnStartClickListener(OnStartClickListener listener) {
        this.startClickListener = listener;
    }

    public void setSelectedDate(Date date) {
        this.selectedDate = date;
    }

    public void setItems(List<ScheduledWorkout> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ContentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_day_content, parent, false);
        return new ContentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContentViewHolder holder, int position) {
        ScheduledWorkout sw = items.get(position);
        holder.bind(sw);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ContentViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvContentName;
        private final TextView tvExerciseCount;
        private final LinearLayout llExercisePreview;
        private final TextView tvAndMore;
        private final TextView tvContentStatus;
        private final TextView btnStart;

        ContentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContentName = itemView.findViewById(R.id.tvContentName);
            tvExerciseCount = itemView.findViewById(R.id.tvExerciseCount);
            llExercisePreview = itemView.findViewById(R.id.llExercisePreview);
            tvAndMore = itemView.findViewById(R.id.tvAndMore);
            tvContentStatus = itemView.findViewById(R.id.tvContentStatus);
            btnStart = itemView.findViewById(R.id.btnStart);
        }

        void bind(ScheduledWorkout sw) {
            Context context = itemView.getContext();

            // Workout name
            String workoutName = sw.getWorkout() != null ? sw.getWorkout().getName() : "Workout";
            tvContentName.setText(workoutName);

            // Exercise count and preview
            List<WorkoutExercise> exercises = sw.getWorkout() != null
                    ? sw.getWorkout().getExercises() : null;
            int exerciseCount = exercises != null ? exercises.size() : 0;

            tvExerciseCount.setText(context.getString(
                    R.string.workout_exercise_count_format, exerciseCount));

            // Populate exercise previews (up to 3)
            llExercisePreview.removeAllViews();
            tvAndMore.setVisibility(View.GONE);

            if (exercises != null && !exercises.isEmpty()) {
                int previewCount = Math.min(exercises.size(), MAX_PREVIEW_EXERCISES);
                LayoutInflater inflater = LayoutInflater.from(context);

                for (int i = 0; i < previewCount; i++) {
                    WorkoutExercise we = exercises.get(i);
                    View previewItem = inflater.inflate(
                            R.layout.item_exercise_preview, llExercisePreview, false);

                    ImageView ivIcon = previewItem.findViewById(R.id.ivExerciseIcon);
                    TextView tvName = previewItem.findViewById(R.id.tvExerciseName);
                    TextView tvSets = previewItem.findViewById(R.id.tvExerciseSets);

                    ivIcon.setImageResource(R.drawable.ic_exercise_placeholder);

                    String exerciseName = we.getExercise() != null
                            ? we.getExercise().getName() : "Exercise";
                    tvName.setText(exerciseName);
                    tvSets.setText(context.getString(R.string.home_sets_format, we.getSets()));

                    // Try to load exercise image from drawable
                    loadExerciseImage(ivIcon, exerciseName);

                    llExercisePreview.addView(previewItem);
                }

                // "and X more" text
                if (exerciseCount > MAX_PREVIEW_EXERCISES) {
                    int remaining = exerciseCount - MAX_PREVIEW_EXERCISES;
                    tvAndMore.setText(context.getString(
                            R.string.weekly_plan_and_more_format, remaining));
                    tvAndMore.setVisibility(View.VISIBLE);
                }
            }

            // Status and Start button
            Date today = new Date(System.currentTimeMillis());
            boolean isToday = isSameDay(selectedDate, today);
            boolean isPast = selectedDate != null && selectedDate.before(today) && !isToday;

            if (isToday && !sw.isCompleted()) {
                // Today and not completed — show START button
                tvContentStatus.setVisibility(View.GONE);
                btnStart.setVisibility(View.VISIBLE);
                btnStart.setOnClickListener(v -> {
                    if (startClickListener != null) {
                        startClickListener.onStartClick(sw);
                    }
                });
            } else if (sw.isCompleted()) {
                // Completed
                tvContentStatus.setText(R.string.calendar_status_completed);
                tvContentStatus.setTextColor(
                        ContextCompat.getColor(context, R.color.light_blue));
                tvContentStatus.setVisibility(View.VISIBLE);
                btnStart.setVisibility(View.GONE);
            } else if (isPast) {
                // Past and not completed — Missed
                tvContentStatus.setText(R.string.calendar_status_missed);
                tvContentStatus.setTextColor(
                        ContextCompat.getColor(context, R.color.red));
                tvContentStatus.setVisibility(View.VISIBLE);
                btnStart.setVisibility(View.GONE);
            } else {
                // Future — Scheduled
                tvContentStatus.setText(R.string.calendar_status_scheduled);
                tvContentStatus.setTextColor(
                        ContextCompat.getColor(context, R.color.muted));
                tvContentStatus.setVisibility(View.VISIBLE);
                btnStart.setVisibility(View.GONE);
            }
        }

        /**
         * Loads an exercise image from drawable resources based on exercise name.
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
    }

    /**
     * Checks if two dates represent the same calendar day.
     */
    private boolean isSameDay(Date d1, Date d2) {
        if (d1 == null || d2 == null) return false;
        java.util.Calendar cal1 = java.util.Calendar.getInstance();
        cal1.setTime(d1);
        java.util.Calendar cal2 = java.util.Calendar.getInstance();
        cal2.setTime(d2);
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR)
                && cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR);
    }
}
