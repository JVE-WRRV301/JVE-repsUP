package com.jve.repsup.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.jve.repsup.R;
import com.jve.repsup.models.Exercise;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * RecyclerView adapter for exercise selection.
 * Allows the admin to select up to a maximum number of exercises.
 * Selected items are highlighted in blue.
 */
public class ExerciseSelectionAdapter extends RecyclerView.Adapter<ExerciseSelectionAdapter.ExerciseViewHolder> {

    private List<Exercise> exercises = new ArrayList<>();
    private final Set<Integer> selectedPositions = new HashSet<>();
    private final int maxSelections;
    private final SelectionCountListener countListener;

    public interface SelectionCountListener {
        void onCountChanged(int count);
    }

    public ExerciseSelectionAdapter(int maxSelections, SelectionCountListener countListener) {
        this.maxSelections = maxSelections;
        this.countListener = countListener;
    }

    public void setExercises(List<Exercise> exercises) {
        this.exercises = exercises;
        this.selectedPositions.clear();
        notifyDataSetChanged();
    }

    public List<Exercise> getSelectedExercises() {
        List<Exercise> selected = new ArrayList<>();
        for (int position : selectedPositions) {
            if (position < exercises.size()) {
                selected.add(exercises.get(position));
            }
        }
        return selected;
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise_selection, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        Exercise exercise = exercises.get(position);
        boolean isSelected = selectedPositions.contains(position);

        holder.tvExerciseName.setText(exercise.getName());
        holder.tvExerciseDetails.setText(
                holder.itemView.getContext().getString(R.string.admin_exercise_details,
                        exercise.getDefaultSets(), exercise.getDefaultReps(), exercise.getDifficulty())
        );

        // Highlight selected items in blue
        if (isSelected) {
            holder.cardView.setCardBackgroundColor(
                    holder.itemView.getContext().getResources().getColor(R.color.light_blue, null));
            holder.tvExerciseName.setTextColor(
                    holder.itemView.getContext().getResources().getColor(R.color.dark_text, null));
            holder.tvExerciseDetails.setTextColor(
                    holder.itemView.getContext().getResources().getColor(R.color.dark_text, null));
        } else {
            holder.cardView.setCardBackgroundColor(
                    holder.itemView.getContext().getResources().getColor(R.color.dark_grey_surface, null));
            holder.tvExerciseName.setTextColor(
                    holder.itemView.getContext().getResources().getColor(R.color.white, null));
            holder.tvExerciseDetails.setTextColor(
                    holder.itemView.getContext().getResources().getColor(R.color.muted, null));
        }

        holder.itemView.setOnClickListener(v -> {
            int adapterPos = holder.getAdapterPosition();
            if (adapterPos == RecyclerView.NO_POSITION) return;

            if (selectedPositions.contains(adapterPos)) {
                selectedPositions.remove(adapterPos);
            } else if (selectedPositions.size() < maxSelections) {
                selectedPositions.add(adapterPos);
            }
            notifyItemChanged(adapterPos);
            if (countListener != null) {
                countListener.onCountChanged(selectedPositions.size());
            }
        });
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvExerciseName;
        TextView tvExerciseDetails;

        ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardExercise);
            tvExerciseName = itemView.findViewById(R.id.tvExerciseName);
            tvExerciseDetails = itemView.findViewById(R.id.tvExerciseDetails);
        }
    }
}
