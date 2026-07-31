package com.jve.repsup.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jve.repsup.R;
import com.jve.repsup.models.ScheduledWorkout;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for displaying scheduled content for a selected day.
 * Shows workout name and completion status.
 */
public class DayContentAdapter extends RecyclerView.Adapter<DayContentAdapter.ContentViewHolder> {

    private List<ScheduledWorkout> items = new ArrayList<>();

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

        String workoutName = sw.getWorkout() != null ? sw.getWorkout().getName() : "Workout";
        holder.tvContentName.setText(workoutName);

        if (sw.isCompleted()) {
            holder.tvContentStatus.setText(R.string.calendar_status_completed);
        } else {
            holder.tvContentStatus.setText(R.string.calendar_status_scheduled);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ContentViewHolder extends RecyclerView.ViewHolder {
        TextView tvContentName;
        TextView tvContentStatus;

        ContentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContentName = itemView.findViewById(R.id.tvContentName);
            tvContentStatus = itemView.findViewById(R.id.tvContentStatus);
        }
    }
}
