package com.jve.repsup.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jve.repsup.MainActivity;
import com.jve.repsup.R;
import com.jve.repsup.activities.CreateGoalActivity;
import com.jve.repsup.adapters.GoalCardAdapter;
import com.jve.repsup.database.ConnectionManager;
import com.jve.repsup.dialogs.GoalCompleteDialog;
import com.jve.repsup.dialogs.LevelUpDialog;
import com.jve.repsup.models.Goal;
import com.jve.repsup.models.User;
import com.jve.repsup.repositories.GoalRepository;
import com.jve.repsup.repositories.UserRepository;
import com.jve.repsup.utils.SessionManager;
import com.jve.repsup.utils.XPCalculator;

import java.util.List;

/**
 * Fragment displaying the list of user goals in the CalendarFragment "Goals" tab.
 * Shows goal cards (red border) ordered by deadline with completion checkbox,
 * 3-dot menu (Edit/Delete), and awards XP on goal completion.
 */
public class GoalsListFragment extends Fragment implements GoalCardAdapter.OnGoalActionListener {

    private RecyclerView rvGoals;
    private TextView tvGoalsEmpty;
    private GoalCardAdapter adapter;
    private GoalRepository goalRepository;
    private UserRepository userRepository;
    private SessionManager sessionManager;

    public GoalsListFragment() {
        // Required empty public constructor
    }

    public static GoalsListFragment newInstance() {
        return new GoalsListFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_goals_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        goalRepository = new GoalRepository();
        userRepository = new UserRepository();
        sessionManager = new SessionManager(requireContext());

        initViews(view);
        loadGoals();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadGoals();
    }

    private void initViews(View view) {
        rvGoals = view.findViewById(R.id.rvGoals);
        tvGoalsEmpty = view.findViewById(R.id.tvGoalsEmpty);

        adapter = new GoalCardAdapter(this);
        rvGoals.setLayoutManager(new LinearLayoutManager(getContext()));
        rvGoals.setAdapter(adapter);
    }

    /**
     * Loads all goals for the current user from the database.
     */
    private void loadGoals() {
        int userId = sessionManager.getUserId();
        if (userId == -1) return;

        goalRepository.getUserGoals(userId, new ConnectionManager.DatabaseCallback<List<Goal>>() {
            @Override
            public void onSuccess(List<Goal> result) {
                if (getActivity() == null || !isAdded()) return;
                getActivity().runOnUiThread(() -> {
                    if (result != null && !result.isEmpty()) {
                        rvGoals.setVisibility(View.VISIBLE);
                        tvGoalsEmpty.setVisibility(View.GONE);
                        adapter.setGoals(result);
                    } else {
                        rvGoals.setVisibility(View.GONE);
                        tvGoalsEmpty.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                if (getActivity() == null || !isAdded()) return;
                getActivity().runOnUiThread(() -> {
                    rvGoals.setVisibility(View.GONE);
                    tvGoalsEmpty.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    /**
     * Called when the user taps the completion checkbox on a goal.
     * Marks the goal as completed, awards XP, inserts XP_Log, updates User.xp,
     * checks for level-up, and displays the CONGRATULATIONS! popup.
     */
    @Override
    public void onGoalComplete(Goal goal, int position) {
        int userId = sessionManager.getUserId();
        if (userId == -1) return;

        int xpEarned = goal.getXpReward();

        // Step 1: Mark goal as completed in database
        goalRepository.completeGoal(goal.getGoalId(), new ConnectionManager.DatabaseCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                if (getActivity() == null || !isAdded()) return;

                // Step 2: Update User.xp
                userRepository.updateXp(userId, xpEarned, new ConnectionManager.DatabaseCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer xpResult) {
                        // Step 3: Insert XP_Log record
                        userRepository.insertXpLog(userId, xpEarned, "goal",
                                goal.getGoalId(), xpEarned, 0,
                                new ConnectionManager.DatabaseCallback<Long>() {
                                    @Override
                                    public void onSuccess(Long logId) {
                                        if (getActivity() == null || !isAdded()) return;

                                        // Step 4: Check for level-up
                                        checkLevelUpAfterGoal(userId, goal, position, xpEarned);
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        // XP_Log insert failed but goal is completed — still check level-up
                                        if (getActivity() == null || !isAdded()) return;
                                        checkLevelUpAfterGoal(userId, goal, position, xpEarned);
                                    }
                                });
                    }

                    @Override
                    public void onError(Exception e) {
                        if (getActivity() == null || !isAdded()) return;
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), R.string.goal_complete_error,
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                if (getActivity() == null || !isAdded()) return;
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), R.string.goal_complete_error,
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /**
     * Checks if the user should level up after goal XP was awarded.
     * If so, increments the level, awards 50 coins, and shows a LevelUpDialog
     * before the goal completion dialog.
     */
    private void checkLevelUpAfterGoal(int userId, Goal goal, int position, int xpEarned) {
        userRepository.getUserById(userId, new ConnectionManager.DatabaseCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user == null || getActivity() == null || !isAdded()) return;

                int currentLevel = user.getCurrentLevel();
                int totalXp = user.getXp();

                // Check if user should level up (possibly multiple levels)
                int newLevel = currentLevel;
                while (XPCalculator.shouldLevelUp(totalXp, newLevel)) {
                    newLevel++;
                }

                if (newLevel > currentLevel) {
                    int finalLevel = newLevel;
                    // Update level in database
                    userRepository.updateLevel(userId, finalLevel, null);
                    // Award 50 coins per level gained
                    int coinsEarned = (finalLevel - currentLevel) * 50;
                    userRepository.updateCoins(userId, coinsEarned, null);

                    getActivity().runOnUiThread(() -> {
                        // Update local goal state
                        goal.setCompleted(true);
                        adapter.notifyItemChanged(position);

                        // Refresh top bar to show new level
                        refreshTopBar();

                        // Show Level Up dialog, then goal complete dialog after dismiss
                        LevelUpDialog levelUpDialog = new LevelUpDialog(getContext(), finalLevel);
                        levelUpDialog.setOnDismissListener(d -> {
                            showGoalCompleteDialog(goal.getDescription(), xpEarned);
                        });
                        levelUpDialog.show();
                    });
                } else {
                    getActivity().runOnUiThread(() -> {
                        // No level-up — just show goal complete dialog
                        goal.setCompleted(true);
                        adapter.notifyItemChanged(position);

                        // Refresh top bar to show updated XP/coins
                        refreshTopBar();

                        showGoalCompleteDialog(goal.getDescription(), xpEarned);
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                // Couldn't check level — still show goal complete dialog
                if (getActivity() == null || !isAdded()) return;
                getActivity().runOnUiThread(() -> {
                    goal.setCompleted(true);
                    adapter.notifyItemChanged(position);
                    showGoalCompleteDialog(goal.getDescription(), xpEarned);
                });
            }
        });
    }

    /**
     * Displays the "CONGRATULATIONS!" popup with goal title, XP earned, Share/Close.
     */
    private void showGoalCompleteDialog(String goalTitle, int xpEarned) {
        if (getContext() == null) return;
        GoalCompleteDialog dialog = new GoalCompleteDialog(getContext(), goalTitle, xpEarned);
        dialog.show();
    }

    /**
     * Refreshes the MainActivity top bar to reflect updated level/coins/streak.
     */
    private void refreshTopBar() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).loadTopBarData();
        }
    }

    /**
     * Opens CreateGoalActivity pre-populated with goal data for editing.
     */
    @Override
    public void onGoalEdit(Goal goal, int position) {
        Intent intent = new Intent(getContext(), CreateGoalActivity.class);
        intent.putExtra("goal_id", goal.getGoalId());

        // Split description into title and notes
        String description = goal.getDescription();
        String title = description;
        String notes = "";
        if (description != null && description.contains("\n---\n")) {
            int separatorIndex = description.indexOf("\n---\n");
            title = description.substring(0, separatorIndex);
            notes = description.substring(separatorIndex + 5);
        }

        intent.putExtra("goal_title", title);
        intent.putExtra("goal_notes", notes);
        intent.putExtra("goal_priority", goal.getPriority());
        if (goal.getDeadline() != null) {
            intent.putExtra("goal_deadline", goal.getDeadline().getTime());
        }
        startActivity(intent);
    }

    /**
     * Shows a confirmation dialog before deleting a goal.
     */
    @Override
    public void onGoalDelete(Goal goal, int position) {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle(R.string.goal_delete_confirm_title)
                .setMessage(R.string.goal_delete_confirm_message)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> deleteGoal(goal, position))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /**
     * Deletes the goal from the database and removes it from the adapter.
     */
    private void deleteGoal(Goal goal, int position) {
        goalRepository.deleteGoal(goal.getGoalId(), new ConnectionManager.DatabaseCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                if (getActivity() == null || !isAdded()) return;
                getActivity().runOnUiThread(() -> {
                    adapter.removeGoal(position);
                    Toast.makeText(getContext(), R.string.goal_delete_success,
                            Toast.LENGTH_SHORT).show();

                    // Show empty state if no goals remain
                    if (adapter.getItemCount() == 0) {
                        rvGoals.setVisibility(View.GONE);
                        tvGoalsEmpty.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                if (getActivity() == null || !isAdded()) return;
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), R.string.goal_delete_error,
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
