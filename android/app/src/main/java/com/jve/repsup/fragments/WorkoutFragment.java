package com.jve.repsup.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.jve.repsup.R;
import com.jve.repsup.activities.CreateWorkoutActivity;
import com.jve.repsup.utils.DialogUtils;
import com.jve.repsup.adapters.ExerciseListAdapter;
import com.jve.repsup.adapters.MuscleGroupAdapter;
import com.jve.repsup.adapters.WorkoutCardAdapter;
import com.jve.repsup.database.ConnectionManager;
import com.jve.repsup.models.Exercise;
import com.jve.repsup.models.Workout;
import com.jve.repsup.models.WorkoutExercise;
import com.jve.repsup.repositories.WorkoutRepository;
import com.jve.repsup.utils.SessionManager;

import java.util.Arrays;
import java.util.List;

/**
 * WorkoutFragment displaying two sub-tabs: "Saved" (user workouts) and "Library"
 * (all exercises grouped by muscle group). Users can browse saved workout cards
 * (blue border) with exercise previews, or navigate the exercise library by
 * muscle group. The "+" button navigates to the Create User Workout flow.
 */
public class WorkoutFragment extends Fragment implements
        WorkoutCardAdapter.OnWorkoutActionListener,
        MuscleGroupAdapter.OnMuscleGroupClickListener,
        ExerciseListAdapter.OnExerciseClickListener {

    // Muscle groups available in the library
    private static final List<String> MUSCLE_GROUPS = Arrays.asList(
            "Chest", "Back", "Shoulders", "Biceps", "Triceps",
            "Legs", "Glutes", "Abs", "Calves", "Forearms"
    );

    // Views
    private TabLayout tabLayout;
    private FrameLayout flSavedContent;
    private FrameLayout flLibraryContent;
    private RecyclerView rvSavedWorkouts;
    private TextView tvSavedEmpty;
    private RecyclerView rvMuscleGroups;
    private LinearLayout llExerciseListContainer;
    private RecyclerView rvExerciseList;
    private ImageView btnLibraryBack;
    private TextView tvLibraryGroupTitle;
    private ImageView btnAddWorkout;

    // Adapters
    private WorkoutCardAdapter workoutCardAdapter;
    private MuscleGroupAdapter muscleGroupAdapter;
    private ExerciseListAdapter exerciseListAdapter;

    // Data
    private WorkoutRepository workoutRepository;
    private SessionManager sessionManager;

    public WorkoutFragment() {
        // Required empty public constructor
    }

    public static WorkoutFragment newInstance() {
        return new WorkoutFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_workout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        workoutRepository = new WorkoutRepository();
        sessionManager = new SessionManager(requireContext());

        initViews(view);
        setupTabs();
        setupAdapters();
        loadSavedWorkouts();
    }

    private void initViews(View view) {
        tabLayout = view.findViewById(R.id.tabLayout);
        flSavedContent = view.findViewById(R.id.flSavedContent);
        flLibraryContent = view.findViewById(R.id.flLibraryContent);
        rvSavedWorkouts = view.findViewById(R.id.rvSavedWorkouts);
        tvSavedEmpty = view.findViewById(R.id.tvSavedEmpty);
        rvMuscleGroups = view.findViewById(R.id.rvMuscleGroups);
        llExerciseListContainer = view.findViewById(R.id.llExerciseListContainer);
        rvExerciseList = view.findViewById(R.id.rvExerciseList);
        btnLibraryBack = view.findViewById(R.id.btnLibraryBack);
        tvLibraryGroupTitle = view.findViewById(R.id.tvLibraryGroupTitle);
        btnAddWorkout = view.findViewById(R.id.btnAddWorkout);

        btnAddWorkout.setOnClickListener(v -> navigateToCreateWorkout());
        btnLibraryBack.setOnClickListener(v -> showMuscleGroupGrid());
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText(R.string.workout_tab_saved));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.workout_tab_library));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    showSavedTab();
                } else {
                    showLibraryTab();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void setupAdapters() {
        workoutCardAdapter = new WorkoutCardAdapter(this);
        rvSavedWorkouts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSavedWorkouts.setAdapter(workoutCardAdapter);

        muscleGroupAdapter = new MuscleGroupAdapter(MUSCLE_GROUPS, this);
        rvMuscleGroups.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvMuscleGroups.setAdapter(muscleGroupAdapter);

        exerciseListAdapter = new ExerciseListAdapter(this);
        rvExerciseList.setLayoutManager(new LinearLayoutManager(getContext()));
        rvExerciseList.setAdapter(exerciseListAdapter);
    }

    private void showSavedTab() {
        flSavedContent.setVisibility(View.VISIBLE);
        flLibraryContent.setVisibility(View.GONE);
    }

    private void showLibraryTab() {
        flSavedContent.setVisibility(View.GONE);
        flLibraryContent.setVisibility(View.VISIBLE);
        showMuscleGroupGrid();
    }

    private void showMuscleGroupGrid() {
        rvMuscleGroups.setVisibility(View.VISIBLE);
        llExerciseListContainer.setVisibility(View.GONE);
    }

    private void showExerciseList(String muscleGroup) {
        rvMuscleGroups.setVisibility(View.GONE);
        llExerciseListContainer.setVisibility(View.VISIBLE);
        tvLibraryGroupTitle.setText(muscleGroup);
    }

    private void loadSavedWorkouts() {
        int userId = sessionManager.getUserId();
        if (userId == -1) return;

        workoutRepository.getUserWorkouts(userId, new ConnectionManager.DatabaseCallback<List<Workout>>() {
            @Override
            public void onSuccess(List<Workout> result) {
                if (getActivity() == null || !isAdded()) return;
                getActivity().runOnUiThread(() -> {
                    if (result != null && !result.isEmpty()) {
                        workoutCardAdapter.setWorkouts(result);
                        rvSavedWorkouts.setVisibility(View.VISIBLE);
                        tvSavedEmpty.setVisibility(View.GONE);
                    } else {
                        rvSavedWorkouts.setVisibility(View.GONE);
                        tvSavedEmpty.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                if (getActivity() == null || !isAdded()) return;
                getActivity().runOnUiThread(() -> {
                    tvSavedEmpty.setVisibility(View.VISIBLE);
                    rvSavedWorkouts.setVisibility(View.GONE);
                    DialogUtils.showError(getContext(), R.string.workout_load_error);
                });
            }
        });
    }

    private void loadExercisesForGroup(String muscleGroup) {
        workoutRepository.getExercisesByMuscleGroup(muscleGroup, new ConnectionManager.DatabaseCallback<List<Exercise>>() {
            @Override
            public void onSuccess(List<Exercise> result) {
                if (getActivity() == null || !isAdded()) return;
                getActivity().runOnUiThread(() -> {
                    exerciseListAdapter.setExercises(result);
                    showExerciseList(muscleGroup);
                });
            }

            @Override
            public void onError(Exception e) {
                if (getActivity() == null || !isAdded()) return;
                getActivity().runOnUiThread(() ->
                        DialogUtils.showError(getContext(), R.string.workout_exercises_load_error)
                );
            }
        });
    }

    private void navigateToCreateWorkout() {
        Intent intent = new Intent(getContext(), CreateWorkoutActivity.class);
        startActivity(intent);
    }

    @Override
    public void onMuscleGroupClick(String muscleGroup) {
        loadExercisesForGroup(muscleGroup);
    }

    @Override
    public void onExerciseClick(Exercise exercise, int position) {
        // Exercise detail view — future enhancement
    }

    @Override
    public void onWorkoutShare(Workout workout, int position) {
        StringBuilder shareText = new StringBuilder();
        shareText.append("Check out my workout: ").append(workout.getName()).append(" on Reps Up! \uD83D\uDCAA\n\n");

        List<WorkoutExercise> exercises = workout.getExercises();
        if (exercises != null && !exercises.isEmpty()) {
            shareText.append("Exercises:\n");
            for (int i = 0; i < exercises.size(); i++) {
                WorkoutExercise we = exercises.get(i);
                String exerciseName = we.getExercise() != null ? we.getExercise().getName() : "Exercise";
                shareText.append("\u2022 ").append(exerciseName)
                        .append(" \u2014 ").append(we.getSets()).append(" sets \u00d7 ")
                        .append(we.getReps()).append(" reps\n");
            }
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());
        startActivity(Intent.createChooser(shareIntent, null));
    }

    @Override
    public void onWorkoutEdit(Workout workout, int position) {
        Intent intent = new Intent(getContext(), CreateWorkoutActivity.class);
        intent.putExtra(CreateWorkoutActivity.EXTRA_EDIT_WORKOUT_ID, workout.getWorkoutId());
        startActivity(intent);
    }

    @Override
    public void onWorkoutDuplicate(Workout workout, int position) {
        workoutRepository.duplicateWorkout(workout.getWorkoutId(), new ConnectionManager.DatabaseCallback<Long>() {
            @Override
            public void onSuccess(Long result) {
                if (getActivity() == null || !isAdded()) return;
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(),
                            R.string.workout_duplicated_success, Toast.LENGTH_SHORT).show();
                    loadSavedWorkouts();
                });
            }

            @Override
            public void onError(Exception e) {
                if (getActivity() == null || !isAdded()) return;
                getActivity().runOnUiThread(() ->
                        DialogUtils.showError(getContext(), R.string.workout_duplicated_error)
                );
            }
        });
    }

    @Override
    public void onWorkoutReorder(Workout workout, int position) {
        List<WorkoutExercise> exercises = workout.getExercises();
        if (exercises == null || exercises.isEmpty()) {
            Toast.makeText(getContext(), R.string.workout_reorder_no_exercises, Toast.LENGTH_SHORT).show();
            return;
        }

        showReorderDialog(workout, exercises);
    }

    private void showReorderDialog(Workout workout, List<WorkoutExercise> exercises) {
        List<WorkoutExercise> reorderableList = new java.util.ArrayList<>(exercises);

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_reorder_exercises, null);
        RecyclerView rvReorder = dialogView.findViewById(R.id.rvReorderExercises);
        rvReorder.setLayoutManager(new LinearLayoutManager(getContext()));

        final ReorderExerciseAdapter[] adapterHolder = new ReorderExerciseAdapter[1];

        ItemTouchHelper.SimpleCallback touchCallback = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean isLongPressDragEnabled() {
                return false;
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                int fromPos = viewHolder.getAdapterPosition();
                int toPos = target.getAdapterPosition();
                java.util.Collections.swap(reorderableList, fromPos, toPos);
                adapterHolder[0].notifyItemMoved(fromPos, toPos);
                adapterHolder[0].notifyItemRangeChanged(Math.min(fromPos, toPos), Math.abs(fromPos - toPos) + 1);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(touchCallback);

        adapterHolder[0] = new ReorderExerciseAdapter(reorderableList,
                viewHolder -> itemTouchHelper.startDrag(viewHolder));
        rvReorder.setAdapter(adapterHolder[0]);
        itemTouchHelper.attachToRecyclerView(rvReorder);

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.workout_reorder_title)
                .setView(dialogView)
                .setPositiveButton(R.string.workout_reorder_save, (dialog, which) -> {
                    saveReorderedExercises(workout, reorderableList);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void saveReorderedExercises(Workout workout, List<WorkoutExercise> reorderedExercises) {
        for (int i = 0; i < reorderedExercises.size(); i++) {
            reorderedExercises.get(i).setOrderIndex(i + 1);
        }

        workoutRepository.updateWorkout(workout, reorderedExercises, new ConnectionManager.DatabaseCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                if (getActivity() == null || !isAdded()) return;
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(),
                            R.string.workout_reorder_success, Toast.LENGTH_SHORT).show();
                    loadSavedWorkouts();
                });
            }

            @Override
            public void onError(Exception e) {
                if (getActivity() == null || !isAdded()) return;
                getActivity().runOnUiThread(() ->
                        DialogUtils.showError(getContext(), R.string.workout_reorder_error)
                );
            }
        });
    }

    interface OnDragStartListener {
        void onDragStart(RecyclerView.ViewHolder viewHolder);
    }

    private static class ReorderExerciseAdapter extends RecyclerView.Adapter<ReorderExerciseAdapter.ViewHolder> {

        private final List<WorkoutExercise> exercises;
        private final OnDragStartListener dragStartListener;

        ReorderExerciseAdapter(List<WorkoutExercise> exercises, OnDragStartListener dragStartListener) {
            this.exercises = exercises;
            this.dragStartListener = dragStartListener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_reorder_exercise, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            WorkoutExercise we = exercises.get(position);
            String name = we.getExercise() != null ? we.getExercise().getName() : "Exercise";
            holder.tvName.setText(name);
            holder.tvOrder.setText(String.valueOf(position + 1));
            holder.ivDragHandle.setOnTouchListener((v, event) -> {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    dragStartListener.onDragStart(holder);
                }
                return false;
            });
        }

        @Override
        public int getItemCount() {
            return exercises.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName;
            TextView tvOrder;
            ImageView ivDragHandle;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvReorderExerciseName);
                tvOrder = itemView.findViewById(R.id.tvReorderExerciseOrder);
                ivDragHandle = itemView.findViewById(R.id.ivDragHandle);
            }
        }
    }

    @Override
    public void onWorkoutDelete(Workout workout, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.workout_delete_confirm_title)
                .setMessage(R.string.workout_delete_confirm_message)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    workoutRepository.deleteWorkout(workout.getWorkoutId(), new ConnectionManager.DatabaseCallback<Integer>() {
                        @Override
                        public void onSuccess(Integer result) {
                            if (getActivity() == null || !isAdded()) return;
                            getActivity().runOnUiThread(() -> {
                                workoutCardAdapter.removeWorkout(position);
                                Toast.makeText(getContext(),
                                        R.string.workout_deleted_success, Toast.LENGTH_SHORT).show();
                                if (workoutCardAdapter.getItemCount() == 0) {
                                    rvSavedWorkouts.setVisibility(View.GONE);
                                    tvSavedEmpty.setVisibility(View.VISIBLE);
                                }
                            });
                        }

                        @Override
                        public void onError(Exception e) {
                            if (getActivity() == null || !isAdded()) return;
                            getActivity().runOnUiThread(() ->
                                    DialogUtils.showError(getContext(), R.string.workout_deleted_error)
                            );
                        }
                    });
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSavedWorkouts();
    }
}
