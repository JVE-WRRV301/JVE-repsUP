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
    private final List<Integer> setsList;
    private final List<Integer> repsList;
    private final List<Double> weightList;
    private final OnExerciseRemovedListener removedListener;

    /**
     * Listener notified when the user taps the remove button on a row.
     */
    public interface OnExerciseRemovedListener {
        void onExerciseRemoved(int position);
    }

    /**
     * Constructor with pre-populated reps and weight values.
     * Any missing values fall back to the exercise default reps / zero weight.
     */
    public ExerciseDetailAdapter(List<Exercise> exercises, List<Integer> initialSets,
                                 List<Integer> initialReps, List<Double> initialWeights, OnExerciseRemovedListener removedListener) {
        this.exercises = exercises;
        this.removedListener = removedListener;
        this.setsList = new ArrayList<>();
        this.repsList = new ArrayList<>();
        this.weightList = new ArrayList<>();

        for (int i = 0; i < exercises.size(); i++) {
            if (initialSets != null && i < initialSets.size()) {
                setsList.add(initialSets.get(i));
            } else {
                setsList.add(exercises.get(i).getDefaultSets());
            }
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

    /**
     * Removes the row at the given position and its associated reps/weight values.
     */
    public void removeAt(int position) {
        if (position < 0 || position >= exercises.size()) return;
        exercises.remove(position);
        if (position < setsList.size()) setsList.remove(position);
        if (position < repsList.size()) repsList.remove(position);
        if (position < weightList.size()) weightList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, exercises.size() - position);
    }

    public List<Integer> getSetsList() {
        return setsList;
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
        holder.etSets.setText(String.valueOf(setsList.get(position)));
        holder.etReps.setText(String.valueOf(repsList.get(position)));
        if (weightList.get(position) > 0) {
            holder.etWeight.setText(String.valueOf(weightList.get(position)));
        } else {
            holder.etWeight.setText("");
        }

        // Remove old watchers
        if (holder.setsWatcher != null) {
            holder.etSets.removeTextChangedListener(holder.setsWatcher);
        }
        if (holder.repsWatcher != null) {
            holder.etReps.removeTextChangedListener(holder.repsWatcher);
        }
        if (holder.weightWatcher != null) {
            holder.etWeight.removeTextChangedListener(holder.weightWatcher);
        }

        // Set new watchers
        holder.setsWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                int pos = holder.getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                try {
                    setsList.set(pos, Integer.parseInt(s.toString()));
                } catch (NumberFormatException e) {
                    setsList.set(pos, 0);
                }
            }
        };

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

        holder.etSets.addTextChangedListener(holder.setsWatcher);
        holder.etReps.addTextChangedListener(holder.repsWatcher);
        holder.etWeight.addTextChangedListener(holder.weightWatcher);

        holder.btnRemoveExercise.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            if (removedListener != null) {
                removedListener.onExerciseRemoved(pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvExerciseName;
        EditText etSets;
        EditText etReps;
        EditText etWeight;
        android.widget.ImageButton btnRemoveExercise;
        TextWatcher setsWatcher;
        TextWatcher repsWatcher;
        TextWatcher weightWatcher;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExerciseName = itemView.findViewById(R.id.tvExerciseName);
            etSets = itemView.findViewById(R.id.etSets);
            etReps = itemView.findViewById(R.id.etReps);
            etWeight = itemView.findViewById(R.id.etWeight);
            btnRemoveExercise = itemView.findViewById(R.id.btnRemoveExercise);
        }
    }
}
