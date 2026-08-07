package com.jve.repsup.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
 * RecyclerView adapter for workout cards (blue border) in the Saved tab.
 * Displays workout name, exercise count, and exercise previews.
 * Provides a 3-dot menu with Share, Edit, Duplicate, Reorder, and Delete options.
 */
public class WorkoutCardAdapter extends RecyclerView.Adapter<WorkoutCardAdapter.WorkoutViewHolder> {

    private static final int MAX_PREVIEW_EXERCISES = 3;

    private List<Workout> workouts = new ArrayList<>();
    private OnWorkoutActionListener listener;

    /**
     * Listener interface for workout card interactions.
     */
    public interface OnWorkoutActionListener {
        void onWorkoutShare(Workout workout, int position);
        void onWorkoutEdit(Workout workout, int position);
        void onWorkoutDuplicate(Workout workout, int position);
        void onWorkoutReorder(Workout workout, int position);
        void onWorkoutDelete(Workout workout, int position);
    }

    public WorkoutCardAdapter(OnWorkoutActionListener listener) {
        this.listener = listener;
    }

    public void setWorkouts(List<Workout> workouts) {
        this.workouts = workouts != null ? workouts : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void removeWorkout(int position) {
        if (position >= 0 && position < workouts.size()) {
            workouts.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void addWorkout(Workout workout) {
        workouts.add(workout);
        notifyItemInserted(workouts.size() - 1);
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout_card, parent, false);
        return new WorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        Workout workout = workouts.get(position);
        holder.bind(workout, position);
    }

    @Override
    public int getItemCount() {
        return workouts.size();
    }

    class WorkoutViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvWorkoutCardName;
        private final TextView tvWorkoutExerciseCount;
        private final LinearLayout llExercisePreview;
        private final ImageView btnWorkoutMenu;

        WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWorkoutCardName = itemView.findViewById(R.id.tvWorkoutCardName);
            tvWorkoutExerciseCount = itemView.findViewById(R.id.tvWorkoutExerciseCount);
            llExercisePreview = itemView.findViewById(R.id.llExercisePreview);
            btnWorkoutMenu = itemView.findViewById(R.id.btnWorkoutMenu);
        }

        void bind(Workout workout, int position) {
            tvWorkoutCardName.setText(workout.getName());

            List<WorkoutExercise> exercises = workout.getExercises();
            int exerciseCount = exercises != null ? exercises.size() : 0;
            tvWorkoutExerciseCount.setText(itemView.getContext().getString(
                    R.string.workout_exercise_count_format, exerciseCount));

            // Populate exercise previews (max 3)
            llExercisePreview.removeAllViews();
            if (exercises != null) {
                int previewCount = Math.min(exercises.size(), MAX_PREVIEW_EXERCISES);
                for (int i = 0; i < previewCount; i++) {
                    WorkoutExercise we = exercises.get(i);
                    View previewItem = LayoutInflater.from(itemView.getContext())
                            .inflate(R.layout.item_exercise_preview, llExercisePreview, false);

                    ImageView ivIcon = previewItem.findViewById(R.id.ivExerciseIcon);
                    TextView tvName = previewItem.findViewById(R.id.tvExerciseName);
                    TextView tvSets = previewItem.findViewById(R.id.tvExerciseSets);

                    ivIcon.setImageResource(R.drawable.ic_exercise_placeholder);
                    if (we.getExercise() != null) {
                        tvName.setText(we.getExercise().getName());
                    }
                    tvSets.setText(itemView.getContext().getString(
                            R.string.home_sets_format, we.getSets()));

                    llExercisePreview.addView(previewItem);
                }
            }

            // 3-dot menu
            btnWorkoutMenu.setOnClickListener(v -> showPopupMenu(v, workout, position));
        }

        private void showPopupMenu(View anchor, Workout workout, int position) {
            PopupMenu popupMenu = new PopupMenu(anchor.getContext(), anchor);
            popupMenu.inflate(R.menu.menu_workout_card);
            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.action_share_workout) {
                    if (listener != null) listener.onWorkoutShare(workout, position);
                    return true;
                } else if (itemId == R.id.action_edit_workout) {
                    if (listener != null) listener.onWorkoutEdit(workout, position);
                    return true;
                } else if (itemId == R.id.action_duplicate_workout) {
                    if (listener != null) listener.onWorkoutDuplicate(workout, position);
                    return true;
                } else if (itemId == R.id.action_reorder_workout) {
                    if (listener != null) listener.onWorkoutReorder(workout, position);
                    return true;
                } else if (itemId == R.id.action_delete_workout) {
                    if (listener != null) listener.onWorkoutDelete(workout, position);
                    return true;
                }
                return false;
            });
            popupMenu.show();
        }
    }
}
