package com.jve.repsup.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jve.repsup.R;
import com.jve.repsup.utils.DisplayText;
import com.jve.repsup.models.Exercise;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for exercise items in the library/selection views.
 * Displays exercise name, difficulty, muscle group, and image.
 */
public class ExerciseListAdapter extends RecyclerView.Adapter<ExerciseListAdapter.ExerciseViewHolder> {

    private List<Exercise> exercises = new ArrayList<>();
    private OnExerciseClickListener listener;

    /**
     * Listener interface for exercise item click events.
     */
    public interface OnExerciseClickListener {
        void onExerciseClick(Exercise exercise, int position);
    }

    public ExerciseListAdapter(OnExerciseClickListener listener) {
        this.listener = listener;
    }

    public void setExercises(List<Exercise> exercises) {
        this.exercises = exercises != null ? exercises : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise_list, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        Exercise exercise = exercises.get(position);
        holder.bind(exercise, position);
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    class ExerciseViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivExerciseImage;
        private final TextView tvExerciseName;
        private final TextView tvExerciseDifficulty;
        private final TextView tvExerciseMuscleGroup;

        ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            ivExerciseImage = itemView.findViewById(R.id.ivExerciseImage);
            tvExerciseName = itemView.findViewById(R.id.tvExerciseName);
            tvExerciseDifficulty = itemView.findViewById(R.id.tvExerciseDifficulty);
            tvExerciseMuscleGroup = itemView.findViewById(R.id.tvExerciseMuscleGroup);
        }

        void bind(Exercise exercise, int position) {
            tvExerciseName.setText(exercise.getName());
            tvExerciseDifficulty.setText(DisplayText.capitalise(exercise.getDifficulty()));
            tvExerciseMuscleGroup.setText(exercise.getMuscleGroup());

            // Placeholder image (exercises don't have image resources stored locally)
            ivExerciseImage.setImageResource(R.drawable.ic_exercise_placeholder);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onExerciseClick(exercise, position);
                }
            });
        }
    }
}
