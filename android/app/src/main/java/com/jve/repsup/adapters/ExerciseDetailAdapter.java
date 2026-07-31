package com.jve.repsup.adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jve.repsup.R;
import com.jve.repsup.models.Exercise;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for editing reps and weight per exercise.
 * Used in the workout details/naming step of the Create Workout flow.
 */
public class ExerciseDetailAdapter extends RecyclerView.Adapter<ExerciseDetailAdapter.ViewHolder> {

    private final List<Exercise> exercises;
    private final List<Integer> repsList;
    private final List<Double> weightList;

    public ExerciseDetailAdapter(List<Exercise> exercises) {
        this.exercises = exercises;
        this.repsList = new ArrayList<>();
        this.weightList = new ArrayList<>();

        for (Exercise exercise : exercises) {
            repsList.add(exercise.getDefaultReps());
            weightList.add(0.0);
        }
    }

    /**
     * Constructor with pre-populated reps and weight values (used in edit mode).
     */
    public ExerciseDetailAdapter(List<Exercise> exercises, List<Integer> initialReps, List<Double> initialWeights) {
        this.exercises = exercises;
        this.repsList = new ArrayList<>();
        this.weightList = new ArrayList<>();

        for (int i = 0; i < exercises.size(); i++) {
            if (initialReps != null && i < initialReps.size()) {
                repsList.add(initialReps.get(i));
            } else {
                repsList.add(exercises.get(i).getDefaultReps());
            }
            if (initialWeights != null && i < initialWeights.size()) {
                weightList.add(initialWeights.get(i));
            } else {
                weightList.add(0.0);
            }
        }
    }

    public List<Integer> getRepsList() {
        return repsList;
    }

    public List<Double> getWeightList() {
        return weightList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise_detail_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Exercise exercise = exercises.get(position);
        holder.tvExerciseName.setText(exercise.getName());

        // Set default values
        holder.etReps.setText(String.valueOf(repsList.get(position)));
        if (weightList.get(position) > 0) {
            holder.etWeight.setText(String.valueOf(weightList.get(position)));
        } else {
            holder.etWeight.setText("");
        }

        // Remove old watchers
        if (holder.repsWatcher != null) {
            holder.etReps.removeTextChangedListener(holder.repsWatcher);
        }
        if (holder.weightWatcher != null) {
            holder.etWeight.removeTextChangedListener(holder.weightWatcher);
        }

        // Set new watchers
        holder.repsWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                int pos = holder.getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                try {
                    repsList.set(pos, Integer.parseInt(s.toString()));
                } catch (NumberFormatException e) {
                    repsList.set(pos, 0);
                }
            }
        };

        holder.weightWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                int pos = holder.getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                try {
                    weightList.set(pos, Double.parseDouble(s.toString()));
                } catch (NumberFormatException e) {
                    weightList.set(pos, 0.0);
                }
            }
        };

        holder.etReps.addTextChangedListener(holder.repsWatcher);
        holder.etWeight.addTextChangedListener(holder.weightWatcher);
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvExerciseName;
        EditText etReps;
        EditText etWeight;
        TextWatcher repsWatcher;
        TextWatcher weightWatcher;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExerciseName = itemView.findViewById(R.id.tvExerciseName);
            etReps = itemView.findViewById(R.id.etReps);
            etWeight = itemView.findViewById(R.id.etWeight);
        }
    }
}
