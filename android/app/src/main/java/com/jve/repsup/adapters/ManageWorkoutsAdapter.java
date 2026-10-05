package com.jve.repsup.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jve.repsup.R;
import com.jve.repsup.models.Workout;
import com.jve.repsup.models.WorkoutExercise;

import java.util.ArrayList;
import java.util.List;

/**
 * Flat RecyclerView adapter for the Manage Workouts screen. Lists every default
 * workout as a card (no category/difficulty grouping). Tapping a card opens the
 * workout's detail/edit screen; the 3-dot menu offers Edit and Delete. Supports
 * real-time search filtering by name.
 */
public class ManageWorkoutsAdapter extends RecyclerView.Adapter<ManageWorkoutsAdapter.WorkoutCardViewHolder> {

    private List<Workout> allWorkouts = new ArrayList<>();
    private final List<Workout> filtered = new ArrayList<>();
    private String searchQuery = "";

    private final WorkoutActionListener actionListener;

    public interface WorkoutActionListener {
        void onWorkoutClick(Workout workout);
        void onEditWorkout(Workout workout);
        void onDeleteWorkout(Workout workout);
    }

    public ManageWorkoutsAdapter(WorkoutActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public void setWorkouts(List<Workout> workouts) {
        this.allWorkouts = workouts != null ? workouts : new ArrayList<>();
        applyFilter();
    }

    public void filter(String query) {
        this.searchQuery = query != null ? query.trim().toLowerCase() : "";
        applyFilter();
    }

    public boolean isEmpty() {
        return filtered.isEmpty();
    }

    private void applyFilter() {
        filtered.clear();
        for (Workout w : allWorkouts) {
            if (searchQuery.isEmpty()
                    || (w.getName() != null && w.getName().toLowerCase().contains(searchQuery))) {
                filtered.add(w);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WorkoutCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_manage_workout_card, parent, false);
        return new WorkoutCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutCardViewHolder holder, int position) {
        Workout workout = filtered.get(position);
        holder.tvWorkoutName.setText(workout.getName());

        // Exercise preview: first few names + "+N more".
        List<WorkoutExercise> exercises = workout.getExercises();
        if (exercises != null && !exercises.isEmpty()) {
            StringBuilder preview = new StringBuilder();
            int count = Math.min(exercises.size(), 3);
            for (int i = 0; i < count; i++) {
                WorkoutExercise we = exercises.get(i);
                if (we.getExercise() != null) {
                    if (preview.length() > 0) preview.append(", ");
                    preview.append(we.getExercise().getName());
                }
            }
            if (exercises.size() > 3) {
                preview.append(" +").append(exercises.size() - 3).append(" more");
            }
            holder.tvExercisePreview.setText(preview.toString());
            holder.tvExercisePreview.setVisibility(View.VISIBLE);
        } else {
            holder.tvExercisePreview.setVisibility(View.GONE);
        }

        // Tap the card to open detail/edit.
        holder.itemView.setOnClickListener(v -> actionListener.onWorkoutClick(workout));

        // 3-dot menu: Edit / Delete.
        holder.btnWorkoutMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenu().add(0, 1, 0, R.string.admin_action_edit);
            popup.getMenu().add(0, 2, 1, R.string.admin_action_delete);
            popup.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getItemId() == 1) {
                    actionListener.onEditWorkout(workout);
                    return true;
                } else if (menuItem.getItemId() == 2) {
                    actionListener.onDeleteWorkout(workout);
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return filtered.size();
    }

    static class WorkoutCardViewHolder extends RecyclerView.ViewHolder {
        TextView tvWorkoutName;
        TextView tvExercisePreview;
        ImageButton btnWorkoutMenu;

        WorkoutCardViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWorkoutName = itemView.findViewById(R.id.tvWorkoutName);
            tvExercisePreview = itemView.findViewById(R.id.tvExercisePreview);
            btnWorkoutMenu = itemView.findViewById(R.id.btnWorkoutMenu);
        }
    }
}
