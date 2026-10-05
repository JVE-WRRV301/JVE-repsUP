package com.jve.repsup.adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jve.repsup.R;
import com.jve.repsup.models.WorkoutExercise;

import java.util.List;

/**
 * RecyclerView adapter for the admin review / edit screens. Shows each selected
 * exercise with editable sets and reps fields (writing changes straight back into
 * the WorkoutExercise objects) plus a remove button. Mirrors the user-side
 * ExerciseDetailAdapter but uses sets + reps instead of reps + weight.
 */
public class AdminExerciseSetsRepsAdapter
        extends RecyclerView.Adapter<AdminExerciseSetsRepsAdapter.ViewHolder> {

    private final List<WorkoutExercise> items;
    private final OnExerciseRemovedListener removedListener;

    public interface OnExerciseRemovedListener {
        void onExerciseRemoved(int position);
    }

    public AdminExerciseSetsRepsAdapter(List<WorkoutExercise> items,
                                        OnExerciseRemovedListener removedListener) {
        this.items = items;
        this.removedListener = removedListener;
    }

    public void removeAt(int position) {
        if (position < 0 || position >= items.size()) return;
        items.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, items.size() - position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_exercise_sets_reps_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkoutExercise item = items.get(position);

        String name = item.getExercise() != null ? item.getExercise().getName() : "";
        holder.tvExerciseName.setText(name);

        // Remove old watchers before setting values to avoid stray callbacks.
        if (holder.setsWatcher != null) {
            holder.etSets.removeTextChangedListener(holder.setsWatcher);
        }
        if (holder.repsWatcher != null) {
            holder.etReps.removeTextChangedListener(holder.repsWatcher);
        }

        holder.etSets.setText(String.valueOf(item.getSets()));
        holder.etReps.setText(String.valueOf(item.getReps()));

        holder.setsWatcher = new SimpleWatcher(value -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            items.get(pos).setSets(value);
        });
        holder.repsWatcher = new SimpleWatcher(value -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            items.get(pos).setReps(value);
        });

        holder.etSets.addTextChangedListener(holder.setsWatcher);
        holder.etReps.addTextChangedListener(holder.repsWatcher);

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
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvExerciseName;
        EditText etSets;
        EditText etReps;
        ImageButton btnRemoveExercise;
        TextWatcher setsWatcher;
        TextWatcher repsWatcher;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExerciseName = itemView.findViewById(R.id.tvExerciseName);
            etSets = itemView.findViewById(R.id.etSets);
            etReps = itemView.findViewById(R.id.etReps);
            btnRemoveExercise = itemView.findViewById(R.id.btnRemoveExercise);
        }
    }

    /**
     * TextWatcher that parses an int and reports it (0 when blank/invalid).
     */
    private static class SimpleWatcher implements TextWatcher {
        interface IntConsumer { void accept(int value); }
        private final IntConsumer consumer;

        SimpleWatcher(IntConsumer consumer) { this.consumer = consumer; }

        @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
        @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}

        @Override
        public void afterTextChanged(Editable s) {
            int value;
            try {
                value = Integer.parseInt(s.toString().trim());
            } catch (NumberFormatException e) {
                value = 0;
            }
            consumer.accept(value);
        }
    }
}
