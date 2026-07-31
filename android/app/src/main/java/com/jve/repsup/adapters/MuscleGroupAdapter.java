package com.jve.repsup.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.jve.repsup.R;

import java.util.List;

/**
 * RecyclerView adapter for displaying muscle groups in a grid.
 * Each item is a clickable card that triggers navigation to exercise selection.
 */
public class MuscleGroupAdapter extends RecyclerView.Adapter<MuscleGroupAdapter.MuscleGroupViewHolder> {

    private final List<String> muscleGroups;
    private final OnMuscleGroupClickListener listener;

    public interface OnMuscleGroupClickListener {
        void onMuscleGroupClick(String muscleGroup);
    }

    public MuscleGroupAdapter(List<String> muscleGroups, OnMuscleGroupClickListener listener) {
        this.muscleGroups = muscleGroups;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MuscleGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_muscle_group, parent, false);
        return new MuscleGroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MuscleGroupViewHolder holder, int position) {
        String muscleGroup = muscleGroups.get(position);
        holder.tvMuscleGroupName.setText(muscleGroup);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMuscleGroupClick(muscleGroup);
            }
        });
    }

    @Override
    public int getItemCount() {
        return muscleGroups.size();
    }

    static class MuscleGroupViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvMuscleGroupName;

        MuscleGroupViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardMuscleGroup);
            tvMuscleGroupName = itemView.findViewById(R.id.tvMuscleGroupName);
        }
    }
}
