package com.jve.repsup.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jve.repsup.R;
import com.jve.repsup.models.Goal;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter for goal cards (red border) in the Goals tab.
 * Displays deadline, title, priority badge, description, and completion checkbox.
 * Provides a 3-dot menu with Edit Goal and Delete Goal options.
 */
public class GoalCardAdapter extends RecyclerView.Adapter<GoalCardAdapter.GoalViewHolder> {

    private List<Goal> goals = new ArrayList<>();
    private OnGoalActionListener listener;

    /**
     * Listener interface for goal card interactions.
     */
    public interface OnGoalActionListener {
        void onGoalComplete(Goal goal, int position);
        void onGoalEdit(Goal goal, int position);
        void onGoalDelete(Goal goal, int position);
    }

    public GoalCardAdapter(OnGoalActionListener listener) {
        this.listener = listener;
    }

    public void setGoals(List<Goal> goals) {
        this.goals = goals != null ? goals : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void removeGoal(int position) {
        if (position >= 0 && position < goals.size()) {
            goals.remove(position);
            notifyItemRemoved(position);
        }
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_goal_card, parent, false);
        return new GoalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        Goal goal = goals.get(position);
        holder.bind(goal, position);
    }

    @Override
    public int getItemCount() {
        return goals.size();
    }

    class GoalViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvGoalDeadline;
        private final TextView tvGoalTitle;
        private final TextView tvGoalPriority;
        private final TextView tvGoalDescription;
        private final CheckBox cbGoalComplete;
        private final ImageView btnGoalMenu;

        private final SimpleDateFormat dateFormat =
                new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

        GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGoalDeadline = itemView.findViewById(R.id.tvGoalDeadline);
            tvGoalTitle = itemView.findViewById(R.id.tvGoalTitle);
            tvGoalPriority = itemView.findViewById(R.id.tvGoalPriority);
            tvGoalDescription = itemView.findViewById(R.id.tvGoalDescription);
            cbGoalComplete = itemView.findViewById(R.id.cbGoalComplete);
            btnGoalMenu = itemView.findViewById(R.id.btnGoalMenu);
        }

        void bind(Goal goal, int position) {
            // Deadline
            if (goal.getDeadline() != null) {
                tvGoalDeadline.setText(itemView.getContext().getString(
                        R.string.goal_deadline_format, dateFormat.format(goal.getDeadline())));
            } else {
                tvGoalDeadline.setText("");
            }

            // Title - using description as the goal title field
            tvGoalTitle.setText(goal.getDescription());

            // Priority badge
            tvGoalPriority.setText(goal.getPriority());

            // Description
            tvGoalDescription.setText(goal.getDescription());

            // Checkbox state
            cbGoalComplete.setOnCheckedChangeListener(null);
            cbGoalComplete.setChecked(goal.isCompleted());
            cbGoalComplete.setEnabled(!goal.isCompleted());

            // Checkbox listener
            cbGoalComplete.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked && !goal.isCompleted() && listener != null) {
                    listener.onGoalComplete(goal, position);
                }
            });

            // 3-dot menu
            btnGoalMenu.setOnClickListener(v -> showPopupMenu(v, goal, position));
        }

        private void showPopupMenu(View anchor, Goal goal, int position) {
            PopupMenu popupMenu = new PopupMenu(anchor.getContext(), anchor);
            popupMenu.inflate(R.menu.menu_goal_card);
            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.action_edit_goal) {
                    if (listener != null) {
                        listener.onGoalEdit(goal, position);
                    }
                    return true;
                } else if (itemId == R.id.action_delete_goal) {
                    if (listener != null) {
                        listener.onGoalDelete(goal, position);
                    }
                    return true;
                }
                return false;
            });
            popupMenu.show();
        }
    }
}
