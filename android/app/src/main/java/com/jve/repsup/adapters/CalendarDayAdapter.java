package com.jve.repsup.adapters;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.jve.repsup.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * RecyclerView adapter for the monthly calendar grid.
 * Displays day numbers with blue (workout) and red (goal) indicators.
 */
public class CalendarDayAdapter extends RecyclerView.Adapter<CalendarDayAdapter.DayViewHolder> {

    private List<Integer> days = new ArrayList<>();
    private Set<Integer> workoutDays = new HashSet<>();
    private Set<Integer> goalDays = new HashSet<>();
    private int selectedDay = -1;
    private int todayDay = -1;
    private OnDayClickListener listener;

    public interface OnDayClickListener {
        void onDayClick(int day);
    }

    public CalendarDayAdapter(OnDayClickListener listener) {
        this.listener = listener;
    }

    /**
     * Sets the days to display in the grid.
     * Day values of 0 represent empty cells (padding for first week offset).
     */
    public void setDays(List<Integer> days, int todayDay) {
        this.days = days;
        this.todayDay = todayDay;
        notifyDataSetChanged();
    }

    /**
     * Sets the days that have scheduled workouts (blue indicator).
     */
    public void setWorkoutDays(Set<Integer> workoutDays) {
        this.workoutDays = workoutDays;
        notifyDataSetChanged();
    }

    /**
     * Sets the days that have goals (red indicator).
     */
    public void setGoalDays(Set<Integer> goalDays) {
        this.goalDays = goalDays;
        notifyDataSetChanged();
    }

    public void setSelectedDay(int day) {
        int oldSelected = selectedDay;
        selectedDay = day;
        // Refresh old and new selected cells
        if (oldSelected > 0) {
            int oldIndex = days.indexOf(oldSelected);
            if (oldIndex >= 0) notifyItemChanged(oldIndex);
        }
        if (selectedDay > 0) {
            int newIndex = days.indexOf(selectedDay);
            if (newIndex >= 0) notifyItemChanged(newIndex);
        }
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_day, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        int day = days.get(position);

        if (day == 0) {
            // Empty cell
            holder.tvDayNumber.setText("");
            holder.llDayCell.setBackground(null);
            holder.viewWorkoutIndicator.setVisibility(View.GONE);
            holder.viewGoalIndicator.setVisibility(View.GONE);
            holder.llIndicators.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(null);
            return;
        }

        holder.tvDayNumber.setText(String.valueOf(day));
        holder.llIndicators.setVisibility(View.VISIBLE);

        // Workout indicator (blue dot)
        if (workoutDays.contains(day)) {
            holder.viewWorkoutIndicator.setVisibility(View.VISIBLE);
            GradientDrawable dot = new GradientDrawable();
            dot.setShape(GradientDrawable.OVAL);
            dot.setColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.light_blue));
            holder.viewWorkoutIndicator.setBackground(dot);
        } else {
            holder.viewWorkoutIndicator.setVisibility(View.GONE);
        }

        // Goal indicator (red triangle-like shape using a rotated square)
        if (goalDays.contains(day)) {
            holder.viewGoalIndicator.setVisibility(View.VISIBLE);
            GradientDrawable triangle = new GradientDrawable();
            triangle.setShape(GradientDrawable.OVAL);
            triangle.setColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.red));
            holder.viewGoalIndicator.setBackground(triangle);
        } else {
            holder.viewGoalIndicator.setVisibility(View.GONE);
        }

        // Highlight today
        if (day == todayDay) {
            holder.tvDayNumber.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.light_blue));
        } else {
            holder.tvDayNumber.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
        }

        // Highlight selected day
        if (day == selectedDay) {
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.RECTANGLE);
            bg.setCornerRadius(8f);
            bg.setStroke(2, ContextCompat.getColor(holder.itemView.getContext(), R.color.light_blue));
            holder.llDayCell.setBackground(bg);
        } else {
            holder.llDayCell.setBackground(null);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                setSelectedDay(day);
                listener.onDayClick(day);
            }
        });
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayNumber;
        LinearLayout llDayCell;
        LinearLayout llIndicators;
        View viewWorkoutIndicator;
        View viewGoalIndicator;

        DayViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayNumber = itemView.findViewById(R.id.tvDayNumber);
            llDayCell = itemView.findViewById(R.id.llDayCell);
            llIndicators = itemView.findViewById(R.id.llIndicators);
            viewWorkoutIndicator = itemView.findViewById(R.id.viewWorkoutIndicator);
            viewGoalIndicator = itemView.findViewById(R.id.viewGoalIndicator);
        }
    }
}
