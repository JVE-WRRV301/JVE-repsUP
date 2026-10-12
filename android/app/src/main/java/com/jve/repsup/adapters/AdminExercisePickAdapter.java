package com.jve.repsup.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.jve.repsup.R;
import com.jve.repsup.utils.DisplayText;
import com.jve.repsup.models.Exercise;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for the ADMIN create flow's exercise list. Tapping an
 * exercise invokes the click listener (which opens the sets/reps dialog to add it
 * to the workout). Single-tap behaviour.
 */
public class AdminExercisePickAdapter
        extends RecyclerView.Adapter<AdminExercisePickAdapter.ExerciseViewHolder> {

    private List<Exercise> exercises = new ArrayList<>();
    private final OnExerciseClickListener clickListener;

    public interface OnExerciseClickListener {
        void onExerciseClick(Exercise exercise);
    }

    public AdminExercisePickAdapter(OnExerciseClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setExercises(List<Exercise> exercises) {
        this.exercises = exercises != null ? exercises : new ArrayList<>();
        notifyDataSetChanged();
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

        holder.tvExerciseName.setText(exercise.getName());
        holder.tvExerciseDetails.setText(
                holder.itemView.getContext().getString(R.string.admin_exercise_details,
                        exercise.getDefaultSets(), exercise.getDefaultReps(),
                        DisplayText.capitalise(exercise.getDifficulty()))
        );

        holder.cardView.setCardBackgroundColor(
                holder.itemView.getContext().getResources().getColor(R.color.dark_grey_surface, null));
        holder.tvExerciseName.setTextColor(
                holder.itemView.getContext().getResources().getColor(R.color.white, null));
        holder.tvExerciseDetails.setTextColor(
                holder.itemView.getContext().getResources().getColor(R.color.muted, null));

        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            if (clickListener != null) {
                clickListener.onExerciseClick(exercises.get(pos));
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
