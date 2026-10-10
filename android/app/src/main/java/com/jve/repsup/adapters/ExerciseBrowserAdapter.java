package com.jve.repsup.adapters;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.jve.repsup.R;
import com.jve.repsup.models.Exercise;
import com.jve.repsup.utils.DisplayText;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Rows for the exercise browser (Library tab and Create Workout picker).
 * In selectable mode, chosen rows turn blue and show a tick. While searching,
 * each row also shows the muscle group it belongs to.
 */
public class ExerciseBrowserAdapter extends RecyclerView.Adapter<ExerciseBrowserAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Exercise exercise);
    }

    private List<Exercise> items = new ArrayList<>();
    private boolean showGroup = false;
    // The tab the user is on. A result from any other group gets its group name in blue.
    private String currentGroup = "";
    private final boolean selectable;
    private final Set<Integer> selectedIds;
    private final OnItemClickListener listener;

    public ExerciseBrowserAdapter(boolean selectable, Set<Integer> selectedIds,
                                  OnItemClickListener listener) {
        this.selectable = selectable;
        this.selectedIds = selectedIds;
        this.listener = listener;
    }

    public void setItems(List<Exercise> items, boolean showGroup, String currentGroup) {
        this.items = items != null ? items : new ArrayList<>();
        this.showGroup = showGroup;
        this.currentGroup = currentGroup != null ? currentGroup : "";
        notifyDataSetChanged();
    }

    /** Re-draws the row for this exercise (after its selection changed). */
    public void refresh(Exercise exercise) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getExerciseId() == exercise.getExerciseId()) {
                notifyItemChanged(i);
                return;
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise_browser, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Exercise exercise = items.get(position);
        android.content.Context context = holder.itemView.getContext();

        holder.tvName.setText(exercise.getName());

        boolean selected = selectable && selectedIds.contains(exercise.getExerciseId());

        String details = context.getString(R.string.admin_exercise_details,
                exercise.getDefaultSets(), exercise.getDefaultReps(),
                DisplayText.capitalise(exercise.getDifficulty()));
        if (showGroup && exercise.getMuscleGroup() != null) {
            String group = exercise.getMuscleGroup();
            SpannableString text = new SpannableString(
                    context.getString(R.string.exercise_browser_group_prefix, group, details));
            // A result from a different group than the tab you are on stands out in blue
            // (not on a selected row, which is already blue).
            if (!selected && !group.equalsIgnoreCase(currentGroup)) {
                text.setSpan(new ForegroundColorSpan(
                                ContextCompat.getColor(context, R.color.light_blue)),
                        0, group.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            holder.tvDetails.setText(text);
        } else {
            holder.tvDetails.setText(details);
        }

        holder.card.setCardBackgroundColor(ContextCompat.getColor(context,
                selected ? R.color.light_blue : R.color.dark_grey_surface));
        holder.tvName.setTextColor(ContextCompat.getColor(context,
                selected ? R.color.dark_text : R.color.white));
        holder.tvDetails.setTextColor(ContextCompat.getColor(context,
                selected ? R.color.dark_text : R.color.muted));
        holder.ivTick.setVisibility(selected ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(exercise);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final CardView card;
        final TextView tvName;
        final TextView tvDetails;
        final ImageView ivTick;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cardExercise);
            tvName = itemView.findViewById(R.id.tvExerciseName);
            tvDetails = itemView.findViewById(R.id.tvExerciseDetails);
            ivTick = itemView.findViewById(R.id.ivSelectedTick);
        }
    }
}
