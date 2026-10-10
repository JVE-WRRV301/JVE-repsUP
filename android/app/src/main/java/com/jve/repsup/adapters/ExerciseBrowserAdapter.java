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
import com.jve.repsup.utils.WeightFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    // Last logged weight (kg) per exercise id, shown in blue after the sets and reps.
    private Map<Integer, Double> lastWeights = new HashMap<>();
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

    public void setLastWeights(Map<Integer, Double> weights) {
        this.lastWeights = weights != null ? weights : new HashMap<>();
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

        // "3 sets × 12 reps · Easy", or "3 sets × 12 reps · 20 kg · Easy" once the user
        // has logged a weight for it.
        Double lastWeight = lastWeights.get(exercise.getExerciseId());
        String weightText = lastWeight != null && lastWeight > 0
                ? WeightFormat.kg(context, lastWeight) : null;
        String difficulty = DisplayText.capitalise(exercise.getDifficulty());
        String details = weightText != null
                ? context.getString(R.string.exercise_browser_details_weight_format,
                        exercise.getDefaultSets(), exercise.getDefaultReps(), weightText, difficulty)
                : context.getString(R.string.admin_exercise_details,
                        exercise.getDefaultSets(), exercise.getDefaultReps(), difficulty);
        int weightColor = ContextCompat.getColor(context, selected ? R.color.dark_text : R.color.light_blue);
        if (showGroup && exercise.getMuscleGroup() != null) {
            String group = exercise.getMuscleGroup();
            String full = context.getString(R.string.exercise_browser_group_prefix, group, details);
            SpannableString text = new SpannableString(full);
            if (weightText != null) {
                int start = full.indexOf(weightText, group.length());
                if (start >= 0) {
                    text.setSpan(new ForegroundColorSpan(weightColor), start,
                            start + weightText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            // A result from a different group than the tab you are on stands out in blue
            // (not on a selected row, which is already blue).
            if (!selected && !group.equalsIgnoreCase(currentGroup)) {
                text.setSpan(new ForegroundColorSpan(
                                ContextCompat.getColor(context, R.color.light_blue)),
                        0, group.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            holder.tvDetails.setText(text);
        } else if (weightText != null) {
            SpannableString text = new SpannableString(details);
            int start = details.indexOf(weightText);
            if (start >= 0) {
                text.setSpan(new ForegroundColorSpan(weightColor), start,
                        start + weightText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
