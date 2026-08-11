package com.jve.repsup.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.jve.repsup.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * RecyclerView adapter for multi-selectable muscle group grid.
 * Selected items are highlighted in blue.
 */
public class SelectableMuscleGroupAdapter extends RecyclerView.Adapter<SelectableMuscleGroupAdapter.ViewHolder> {

    private final List<String> muscleGroups;
    private final Set<Integer> selectedPositions = new HashSet<>();

    public SelectableMuscleGroupAdapter(List<String> muscleGroups) {
        this.muscleGroups = muscleGroups;
    }

    public List<String> getSelectedMuscleGroups() {
        List<String> selected = new ArrayList<>();
        for (int pos : selectedPositions) {
            if (pos < muscleGroups.size()) {
                selected.add(muscleGroups.get(pos));
            }
        }
        return selected;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_muscle_group_selectable, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String muscleGroup = muscleGroups.get(position);
        boolean isSelected = selectedPositions.contains(position);

        holder.tvMuscleGroupName.setText(muscleGroup);

        if (isSelected) {
            holder.cardView.setCardBackgroundColor(
                    holder.itemView.getContext().getResources().getColor(R.color.light_blue, null));
            holder.tvMuscleGroupName.setTextColor(
                    holder.itemView.getContext().getResources().getColor(R.color.dark_text, null));
        } else {
            holder.cardView.setCardBackgroundColor(
                    holder.itemView.getContext().getResources().getColor(R.color.dark_grey_surface, null));
            holder.tvMuscleGroupName.setTextColor(
                    holder.itemView.getContext().getResources().getColor(R.color.white, null));
        }

        holder.itemView.setOnClickListener(v -> {
            int adapterPos = holder.getAdapterPosition();
            if (adapterPos == RecyclerView.NO_POSITION) return;

            if (selectedPositions.contains(adapterPos)) {
                selectedPositions.remove(adapterPos);
            } else {
                selectedPositions.add(adapterPos);
            }
            notifyItemChanged(adapterPos);
        });
    }

    @Override
    public int getItemCount() {
        return muscleGroups.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvMuscleGroupName;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardMuscleGroup);
            tvMuscleGroupName = itemView.findViewById(R.id.tvMuscleGroupName);
        }
    }
}
