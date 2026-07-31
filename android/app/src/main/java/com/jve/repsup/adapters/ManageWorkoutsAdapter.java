package com.jve.repsup.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jve.repsup.R;
import com.jve.repsup.models.Workout;
import com.jve.repsup.models.WorkoutExercise;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Expandable RecyclerView adapter for the Manage Workouts screen.
 * Displays workouts grouped by category with difficulty sub-headers.
 * Supports collapsing/expanding category sections and real-time search filtering.
 */
public class ManageWorkoutsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_CATEGORY_HEADER = 0;
    private static final int VIEW_TYPE_DIFFICULTY_HEADER = 1;
    private static final int VIEW_TYPE_WORKOUT_CARD = 2;

    // Category to muscle group mappings (same as AdminCreateWorkoutActivity)
    private static final Map<String, List<String>> CATEGORY_MUSCLE_GROUPS = new HashMap<>();

    static {
        CATEGORY_MUSCLE_GROUPS.put("Abdominal", Arrays.asList("Abs", "Obliques", "Core"));
        CATEGORY_MUSCLE_GROUPS.put("Full Body", Arrays.asList("Full Body", "Compound"));
        CATEGORY_MUSCLE_GROUPS.put("Lower Body", Arrays.asList("Quads", "Hamstrings", "Glutes", "Calves"));
        CATEGORY_MUSCLE_GROUPS.put("Upper Body", Arrays.asList("Chest", "Back", "Shoulders", "Biceps", "Triceps"));
    }

    private List<Workout> allWorkouts = new ArrayList<>();
    private List<ListItem> displayItems = new ArrayList<>();
    private final Map<String, Boolean> collapsedCategories = new HashMap<>();
    private String searchQuery = "";

    private final WorkoutActionListener actionListener;

    public interface WorkoutActionListener {
        void onEditWorkout(Workout workout);
        void onDeleteWorkout(Workout workout);
    }

    public ManageWorkoutsAdapter(WorkoutActionListener actionListener) {
        this.actionListener = actionListener;
    }

    /**
     * Sets the full list of workouts and rebuilds the display list.
     */
    public void setWorkouts(List<Workout> workouts) {
        this.allWorkouts = workouts;
        rebuildDisplayList();
    }

    /**
     * Filters workouts by name based on the search query.
     */
    public void filter(String query) {
        this.searchQuery = query != null ? query.trim().toLowerCase() : "";
        rebuildDisplayList();
    }

    /**
     * Returns true if there are no items to display.
     */
    public boolean isEmpty() {
        return displayItems.isEmpty();
    }

    /**
     * Rebuilds the flat display list from grouped workouts,
     * applying search filtering and respecting collapsed state.
     */
    private void rebuildDisplayList() {
        displayItems.clear();

        // Filter workouts by search query
        List<Workout> filtered = new ArrayList<>();
        for (Workout w : allWorkouts) {
            if (searchQuery.isEmpty() || w.getName().toLowerCase().contains(searchQuery)) {
                filtered.add(w);
            }
        }

        // Group by category, then by difficulty
        Map<String, Map<String, List<Workout>>> grouped = groupWorkouts(filtered);

        // Build flat list
        for (Map.Entry<String, Map<String, List<Workout>>> categoryEntry : grouped.entrySet()) {
            String category = categoryEntry.getKey();
            boolean isCollapsed = Boolean.TRUE.equals(collapsedCategories.get(category));

            displayItems.add(new ListItem(VIEW_TYPE_CATEGORY_HEADER, category, null, null, isCollapsed));

            if (!isCollapsed) {
                for (Map.Entry<String, List<Workout>> diffEntry : categoryEntry.getValue().entrySet()) {
                    String difficulty = diffEntry.getKey();
                    displayItems.add(new ListItem(VIEW_TYPE_DIFFICULTY_HEADER, difficulty, null, null, false));

                    for (Workout workout : diffEntry.getValue()) {
                        displayItems.add(new ListItem(VIEW_TYPE_WORKOUT_CARD, null, null, workout, false));
                    }
                }
            }
        }

        notifyDataSetChanged();
    }

    /**
     * Groups workouts by category (derived from exercises' muscle groups) and difficulty.
     */
    private Map<String, Map<String, List<Workout>>> groupWorkouts(List<Workout> workouts) {
        Map<String, Map<String, List<Workout>>> grouped = new LinkedHashMap<>();

        for (Workout workout : workouts) {
            String category = deriveCategory(workout);
            String difficulty = deriveDifficulty(workout);

            grouped.computeIfAbsent(category, k -> new LinkedHashMap<>())
                    .computeIfAbsent(difficulty, k -> new ArrayList<>())
                    .add(workout);
        }

        return grouped;
    }

    /**
     * Derives the category for a workout based on the first exercise's muscle group.
     */
    private String deriveCategory(Workout workout) {
        List<WorkoutExercise> exercises = workout.getExercises();
        if (exercises == null || exercises.isEmpty()) {
            return "Uncategorized";
        }

        // Use the first exercise's muscle group to determine category
        WorkoutExercise first = exercises.get(0);
        if (first.getExercise() != null) {
            String muscleGroup = first.getExercise().getMuscleGroup();
            for (Map.Entry<String, List<String>> entry : CATEGORY_MUSCLE_GROUPS.entrySet()) {
                if (entry.getValue().contains(muscleGroup)) {
                    return entry.getKey();
                }
            }
        }

        return "Uncategorized";
    }

    /**
     * Derives the difficulty for a workout based on the majority/first exercise's difficulty.
     */
    private String deriveDifficulty(Workout workout) {
        List<WorkoutExercise> exercises = workout.getExercises();
        if (exercises == null || exercises.isEmpty()) {
            return "Unknown";
        }

        // Use the first exercise's difficulty
        WorkoutExercise first = exercises.get(0);
        if (first.getExercise() != null && first.getExercise().getDifficulty() != null) {
            return first.getExercise().getDifficulty();
        }

        return "Unknown";
    }

    @Override
    public int getItemViewType(int position) {
        return displayItems.get(position).viewType;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case VIEW_TYPE_CATEGORY_HEADER:
                return new CategoryHeaderViewHolder(
                        inflater.inflate(R.layout.item_manage_workout_category_header, parent, false));
            case VIEW_TYPE_DIFFICULTY_HEADER:
                return new DifficultyHeaderViewHolder(
                        inflater.inflate(R.layout.item_manage_workout_difficulty_header, parent, false));
            case VIEW_TYPE_WORKOUT_CARD:
            default:
                return new WorkoutCardViewHolder(
                        inflater.inflate(R.layout.item_manage_workout_card, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ListItem item = displayItems.get(position);

        switch (item.viewType) {
            case VIEW_TYPE_CATEGORY_HEADER:
                bindCategoryHeader((CategoryHeaderViewHolder) holder, item);
                break;
            case VIEW_TYPE_DIFFICULTY_HEADER:
                bindDifficultyHeader((DifficultyHeaderViewHolder) holder, item);
                break;
            case VIEW_TYPE_WORKOUT_CARD:
                bindWorkoutCard((WorkoutCardViewHolder) holder, item);
                break;
        }
    }

    private void bindCategoryHeader(CategoryHeaderViewHolder holder, ListItem item) {
        holder.tvCategoryName.setText(item.title);

        // Rotate arrow based on collapsed state
        holder.ivExpandCollapse.setRotation(item.isCollapsed ? 0f : 180f);

        holder.itemView.setOnClickListener(v -> {
            String category = item.title;
            boolean currentlyCollapsed = Boolean.TRUE.equals(collapsedCategories.get(category));
            collapsedCategories.put(category, !currentlyCollapsed);
            rebuildDisplayList();
        });
    }

    private void bindDifficultyHeader(DifficultyHeaderViewHolder holder, ListItem item) {
        holder.tvDifficultyName.setText(item.title);
    }

    private void bindWorkoutCard(WorkoutCardViewHolder holder, ListItem item) {
        Workout workout = item.workout;
        holder.tvWorkoutName.setText(workout.getName());

        // Build exercise preview text
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

        // 3-dot menu
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
        return displayItems.size();
    }

    // --- ViewHolders ---

    static class CategoryHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName;
        ImageView ivExpandCollapse;

        CategoryHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            ivExpandCollapse = itemView.findViewById(R.id.ivExpandCollapse);
        }
    }

    static class DifficultyHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvDifficultyName;

        DifficultyHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDifficultyName = itemView.findViewById(R.id.tvDifficultyName);
        }
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

    // --- Internal data model ---

    private static class ListItem {
        final int viewType;
        final String title;       // category or difficulty name
        final String subtitle;
        final Workout workout;
        final boolean isCollapsed;

        ListItem(int viewType, String title, String subtitle, Workout workout, boolean isCollapsed) {
            this.viewType = viewType;
            this.title = title;
            this.subtitle = subtitle;
            this.workout = workout;
            this.isCollapsed = isCollapsed;
        }
    }
}
