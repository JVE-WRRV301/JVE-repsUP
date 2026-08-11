package com.jve.repsup.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jve.repsup.R;
import com.jve.repsup.models.CosmeticItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * GridView adapter for displaying cosmetic items in the Locker.
 * Owned items are shown at full opacity; locked items at reduced opacity with a coin price badge.
 */
public class CosmeticGridAdapter extends BaseAdapter {

    private final Context context;
    private List<CosmeticItem> items;
    private final Set<Integer> ownedIds;
    private final Set<Integer> equippedIds;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(CosmeticItem item, boolean isOwned);
    }

    public CosmeticGridAdapter(Context context) {
        this.context = context;
        this.items = new ArrayList<>();
        this.ownedIds = new HashSet<>();
        this.equippedIds = new HashSet<>();
    }

    public void setItems(List<CosmeticItem> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOwnedIds(Set<Integer> ids) {
        ownedIds.clear();
        if (ids != null) ownedIds.addAll(ids);
        notifyDataSetChanged();
    }

    public void setEquippedIds(Set<Integer> ids) {
        equippedIds.clear();
        if (ids != null) equippedIds.addAll(ids);
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public CosmeticItem getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getCosmeticId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_cosmetic, parent, false);
            holder = new ViewHolder();
            holder.container = convertView.findViewById(R.id.cosmetic_item_container);
            holder.imgCosmetic = convertView.findViewById(R.id.img_cosmetic);
            holder.txtName = convertView.findViewById(R.id.txt_cosmetic_name);
            holder.badgeCoinPrice = convertView.findViewById(R.id.badge_coin_price);
            holder.txtCoinPrice = convertView.findViewById(R.id.txt_coin_price);
            holder.imgEquippedCheck = convertView.findViewById(R.id.img_equipped_check);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        CosmeticItem item = getItem(position);
        boolean isOwned = ownedIds.contains(item.getCosmeticId());
        boolean isEquipped = equippedIds.contains(item.getCosmeticId());

        // Set item name
        holder.txtName.setText(item.getName());

        // Load image from base64 data if available
        if (item.getImageData() != null && !item.getImageData().isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(item.getImageData(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                holder.imgCosmetic.setImageBitmap(bitmap);
            } catch (Exception e) {
                holder.imgCosmetic.setImageResource(R.drawable.ic_launcher_foreground);
            }
        } else {
            holder.imgCosmetic.setImageResource(R.drawable.ic_launcher_foreground);
        }

        // Owned vs locked styling
        if (isOwned) {
            holder.container.setAlpha(1.0f);
            holder.badgeCoinPrice.setVisibility(View.GONE);
        } else {
            holder.container.setAlpha(0.5f);
            holder.badgeCoinPrice.setVisibility(View.VISIBLE);
            holder.txtCoinPrice.setText(String.valueOf(item.getCoinCost()));
        }

        // Equipped indicator
        if (isEquipped) {
            holder.container.setBackgroundResource(R.drawable.bg_cosmetic_item_selected);
            holder.imgEquippedCheck.setVisibility(View.VISIBLE);
        } else {
            holder.container.setBackgroundResource(R.drawable.bg_cosmetic_item);
            holder.imgEquippedCheck.setVisibility(View.GONE);
        }

        // Click handler
        convertView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item, isOwned);
            }
        });

        return convertView;
    }

    private static class ViewHolder {
        LinearLayout container;
        ImageView imgCosmetic;
        TextView txtName;
        LinearLayout badgeCoinPrice;
        TextView txtCoinPrice;
        ImageView imgEquippedCheck;
    }
}
