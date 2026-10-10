package com.jve.repsup.adapters;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * RecyclerView adapter for workout cards (blue border) in the Saved tab.
 * Displays workout name, exercise count, and exercise previews.
 * Provides a 3-dot menu with Schedule, Share, Edit, Duplicate, Reorder, and Delete
 * options. In read-only mode (the Default tab) the menu only offers Schedule.
 * Cards show the first 3 exercises plus a "+N more" line that expands the card.
 */
public class WorkoutCardAdapter extends RecyclerView.Adapter<WorkoutCardAdapter.WorkoutViewHolder> {

    private static final int MAX_PREVIEW_EXERCISES = 3;

    // Every workout, and the ones matching the current search (what the list shows).
    private List<Workout> allWorkouts = new ArrayList<>();
    private List<Workout> workouts = new ArrayList<>();
    private String query = "";
    private OnWorkoutActionListener listener;

    // Default (admin) workouts can be viewed and scheduled, but not changed.
    private final boolean readOnly;

    // Workouts whose card is currently expanded to show every exercise.
    private final Set<Integer> expandedWorkoutIds = new HashSet<>();

    /**
     * Listener interface for workout card interactions.
     */
    public interface OnWorkoutActionListener {
        void onWorkoutSchedule(Workout workout, int position);
        void onWorkoutShare(Workout workout, int position);
        void onWorkoutEdit(Workout workout, int position);
        void onWorkoutDuplicate(Workout workout, int position);
        void onWorkoutReorder(Workout workout, int position);
        void onWorkoutDelete(Workout workout, int position);
    }

    public WorkoutCardAdapter(OnWorkoutActionListener listener) {
        this(listener, false);
    }

    public WorkoutCardAdapter(OnWorkoutActionListener listener, boolean readOnly) {
        this.listener = listener;
        this.readOnly = readOnly;
    }

    public void setWorkouts(List<Workout> workouts) {
        this.allWorkouts = workouts != null ? new ArrayList<>(workouts) : new ArrayList<>();
        applyFilter();
    }

    /** Shows only the workouts whose name contains this text (empty shows all). */
    public void setQuery(String text) {
        this.query = text == null ? "" : text.trim().toLowerCase(Locale.ROOT);
        applyFilter();
    }

    /** How many workouts there are in total, ignoring the search. */
    public int getTotalCount() {
        return allWorkouts.size();
    }

    private void applyFilter() {
        List<Workout> matches = new ArrayList<>();
        for (Workout workout : allWorkouts) {
            if (query.isEmpty() || (workout.getName() != null
                    && workout.getName().toLowerCase(Locale.ROOT).contains(query))) {
                matches.add(workout);
            }
        }
        this.workouts = matches;
        notifyDataSetChanged();
    }

    public void removeWorkout(int position) {
        if (position >= 0 && position < workouts.size()) {
            Workout removed = workouts.remove(position);
            allWorkouts.remove(removed);
            notifyItemRemoved(position);
        }
    }

    public void addWorkout(Workout workout) {
        allWorkouts.add(workout);
        applyFilter();
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
        private final TextView tvWorkoutMore;
        private final ImageView btnWorkoutMenu;

        WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWorkoutCardName = itemView.findViewById(R.id.tvWorkoutCardName);
            tvWorkoutExerciseCount = itemView.findViewById(R.id.tvWorkoutExerciseCount);
            llExercisePreview = itemView.findViewById(R.id.llExercisePreview);
            tvWorkoutMore = itemView.findViewById(R.id.tvWorkoutMore);
            btnWorkoutMenu = itemView.findViewById(R.id.btnWorkoutMenu);
        }

        void bind(Workout workout, int position) {
            tvWorkoutCardName.setText(workout.getName());

            List<WorkoutExercise> exercises = workout.getExercises();
            int exerciseCount = exercises != null ? exercises.size() : 0;
            tvWorkoutExerciseCount.setText(itemView.getContext().getString(
                    R.string.workout_exercise_count_format, exerciseCount));

            // Populate exercise previews: the first 3, or all when expanded
            boolean expanded = expandedWorkoutIds.contains(workout.getWorkoutId());
            llExercisePreview.removeAllViews();
            if (exercises != null) {
                int previewCount = expanded
                        ? exercises.size()
                        : Math.min(exercises.size(), MAX_PREVIEW_EXERCISES);
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

            // "+N more" line (or "Show less" when expanded) toggles the card
            int hiddenCount = exerciseCount - MAX_PREVIEW_EXERCISES;
            if (hiddenCount > 0) {
                tvWorkoutMore.setVisibility(View.VISIBLE);
                tvWorkoutMore.setText(expanded
                        ? itemView.getContext().getString(R.string.workout_show_less)
                        : itemView.getContext().getString(R.string.home_more_exercises_format, hiddenCount));
                tvWorkoutMore.setOnClickListener(v -> {
                    int adapterPosition = getAdapterPosition();
                    if (adapterPosition == RecyclerView.NO_POSITION) return;
                    if (!expandedWorkoutIds.remove(workout.getWorkoutId())) {
                        expandedWorkoutIds.add(workout.getWorkoutId());
                    }
                    notifyItemChanged(adapterPosition);
                });
            } else {
                tvWorkoutMore.setVisibility(View.GONE);
                tvWorkoutMore.setOnClickListener(null);
            }

            // 3-dot menu
            btnWorkoutMenu.setOnClickListener(v -> showPopupMenu(v, workout, position));
        }

        private void showPopupMenu(View anchor, Workout workout, int position) {
            PopupMenu popupMenu = new PopupMenu(anchor.getContext(), anchor);
            popupMenu.inflate(R.menu.menu_workout_card);
            if (readOnly) {
                // Default workouts can only be scheduled.
                Menu menu = popupMenu.getMenu();
                for (int i = 0; i < menu.size(); i++) {
                    MenuItem menuItem = menu.getItem(i);
                    menuItem.setVisible(menuItem.getItemId() == R.id.action_schedule_workout);
                }
            }
            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.action_schedule_workout) {
                    if (listener != null) listener.onWorkoutSchedule(workout, position);
                    return true;
                } else if (itemId == R.id.action_share_workout) {
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
