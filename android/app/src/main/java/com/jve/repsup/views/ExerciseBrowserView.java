package com.jve.repsup.views;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.jve.repsup.R;
import com.jve.repsup.adapters.ExerciseBrowserAdapter;
import com.jve.repsup.adapters.ExerciseListAdapter;
import com.jve.repsup.database.ConnectionManager;
import com.jve.repsup.models.Exercise;
import com.jve.repsup.repositories.ExerciseLogRepository;
import com.jve.repsup.repositories.WorkoutRepository;
import com.jve.repsup.utils.DialogUtils;
import com.jve.repsup.utils.MuscleGroups;
import com.jve.repsup.utils.SessionManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Search box + one tab per muscle group + the exercises of the chosen group.
 * Used by the Library tab (tap an exercise to see it) and by the Create Workout
 * picker (tap to tick, with a limit). All exercises are loaded once and filtered
 * in memory, so switching tabs and searching are instant and always in the same
 * order. While the search box has text, matches from every group are listed
 * together, each labelled with its group.
 */
public class ExerciseBrowserView extends LinearLayout {

    /** Tells the host that the ticked exercises changed, or that the limit was hit. */
    public interface SelectionListener {
        void onSelectionChanged(int count);
        void onLimitReached();
    }

    private final EditText etSearch;
    private final TabLayout tabGroups;
    private final RecyclerView rvExercises;
    private final TextView tvEmpty;

    private final ExerciseBrowserAdapter adapter;
    private final List<Exercise> allExercises = new ArrayList<>();
    private final Map<Integer, Exercise> selected = new LinkedHashMap<>();
    // Selected ids as a view over the map, so the adapter always sees current state.
    private final java.util.Set<Integer> selectedIds = selected.keySet();

    private boolean selectable = false;
    private int maxSelections = Integer.MAX_VALUE;
    private boolean loaded = false;
    private boolean loading = false;

    private final Map<Integer, Double> lastWeights = new java.util.HashMap<>();

    private SelectionListener selectionListener;
    private ExerciseListAdapter.OnExerciseClickListener clickListener;

    public ExerciseBrowserView(@NonNull Context context) {
        this(context, null);
    }

    public ExerciseBrowserView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.view_exercise_browser, this, true);

        etSearch = findViewById(R.id.etExerciseSearch);
        tabGroups = findViewById(R.id.tabMuscleGroups);
        rvExercises = findViewById(R.id.rvBrowserExercises);
        tvEmpty = findViewById(R.id.tvBrowserEmpty);

        for (String group : MuscleGroups.ALL) {
            tabGroups.addTab(tabGroups.newTab().setText(group));
        }
        tabGroups.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                showCurrentList();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                showCurrentList();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        adapter = new ExerciseBrowserAdapter(true, selectedIds, this::onExerciseTapped);
        rvExercises.setLayoutManager(new LinearLayoutManager(context));
        rvExercises.setAdapter(adapter);
    }

    /**
     * Sets how the list behaves. In selectable mode a tap ticks / unticks the
     * exercise (up to maxSelections in total, counting the preselected ones).
     * Otherwise a tap goes to the click listener.
     */
    public void configure(boolean selectable, int maxSelections, @Nullable List<Exercise> preselected) {
        this.selectable = selectable;
        this.maxSelections = maxSelections;
        selected.clear();
        if (selectable && preselected != null) {
            for (Exercise exercise : preselected) {
                selected.put(exercise.getExerciseId(), exercise);
            }
        }
    }

    public void setSelectionListener(SelectionListener listener) {
        this.selectionListener = listener;
    }

    public void setOnExerciseClickListener(ExerciseListAdapter.OnExerciseClickListener listener) {
        this.clickListener = listener;
    }

    /** The ticked exercises, in the order they were ticked (preselected ones first). */
    public List<Exercise> getSelectedExercises() {
        return new ArrayList<>(selected.values());
    }

    public int getSelectedCount() {
        return selected.size();
    }

    /** The last weight (kg) the user logged for this exercise, or null if none. */
    @Nullable
    public Double getLastWeight(int exerciseId) {
        return lastWeights.get(exerciseId);
    }

    /**
     * Fetches the user's last logged weights and shows them on the rows. Runs every
     * time the list is shown, so a workout finished in the meantime is picked up.
     * If it fails the rows simply show no weight.
     */
    private void loadLastWeights() {
        int userId = new SessionManager(getContext()).getUserId();
        if (userId == -1) return;
        new ExerciseLogRepository().getLastWeights(userId,
                new ConnectionManager.DatabaseCallback<Map<Integer, Double>>() {
                    @Override
                    public void onSuccess(Map<Integer, Double> weights) {
                        post(() -> {
                            lastWeights.clear();
                            if (weights != null) lastWeights.putAll(weights);
                            adapter.setLastWeights(lastWeights);
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        // The rows just show no weight.
                    }
                });
    }

    /** Loads every exercise once (the weights are refreshed on every call). */
    public void loadIfNeeded() {
        loadLastWeights();
        if (loaded || loading) return;
        loading = true;
        new WorkoutRepository().getAllExercises(new ConnectionManager.DatabaseCallback<List<Exercise>>() {
            @Override
            public void onSuccess(List<Exercise> result) {
                post(() -> {
                    loading = false;
                    loaded = true;
                    allExercises.clear();
                    if (result != null) allExercises.addAll(result);
                    showCurrentList();
                });
            }

            @Override
            public void onError(Exception e) {
                post(() -> {
                    loading = false;
                    DialogUtils.showError(getContext(), R.string.create_workout_load_error);
                });
            }
        });
    }

    private void onExerciseTapped(Exercise exercise) {
        if (!selectable) {
            if (clickListener != null) clickListener.onExerciseClick(exercise, 0);
            return;
        }
        int id = exercise.getExerciseId();
        if (selected.containsKey(id)) {
            selected.remove(id);
        } else if (selected.size() >= maxSelections) {
            if (selectionListener != null) selectionListener.onLimitReached();
            return;
        } else {
            selected.put(id, exercise);
        }
        adapter.refresh(exercise);
        if (selectionListener != null) selectionListener.onSelectionChanged(selected.size());
    }

    /** Shows the search matches (all groups) or the chosen tab's exercises. */
    private void showCurrentList() {
        String query = etSearch.getText().toString().trim().toLowerCase(Locale.ROOT);
        boolean searching = !query.isEmpty();
        tabGroups.setVisibility(searching ? View.GONE : View.VISIBLE);

        int position = tabGroups.getSelectedTabPosition();
        if (position < 0) position = 0;
        String group = MuscleGroups.ALL.get(position);

        List<Exercise> shown = new ArrayList<>();
        if (searching) {
            for (Exercise exercise : allExercises) {
                if (exercise.getName() != null
                        && exercise.getName().toLowerCase(Locale.ROOT).contains(query)) {
                    shown.add(exercise);
                }
            }
        } else {
            for (Exercise exercise : allExercises) {
                if (group.equalsIgnoreCase(exercise.getMuscleGroup())) {
                    shown.add(exercise);
                }
            }
        }

        adapter.setItems(shown, searching, group);
        // Only say "no results" once the exercises have loaded.
        tvEmpty.setVisibility(loaded && shown.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
