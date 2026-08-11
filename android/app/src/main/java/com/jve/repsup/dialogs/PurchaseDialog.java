package com.jve.repsup.dialogs;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.jve.repsup.R;
import com.jve.repsup.models.CosmeticItem;

/**
 * Dialog for purchasing a locked cosmetic item.
 * Shows the item image, name, price in coins, and Cancel/Buy buttons.
 * If the user has insufficient coins, displays a "Not enough coins" message
 * and disables the Buy button.
 *
 * Validates: Requirements 22.1, 22.2, 22.3
 */
public class PurchaseDialog extends DialogFragment {

    private CosmeticItem item;
    private int userCoinBalance;
    private OnPurchaseListener listener;

    /**
     * Callback interface for purchase events.
     */
    public interface OnPurchaseListener {
        void onPurchaseConfirmed(CosmeticItem item);
    }

    /**
     * Creates a new PurchaseDialog instance.
     *
     * @param item            The cosmetic item to purchase
     * @param userCoinBalance The user's current coin balance
     * @param listener        Callback for purchase confirmation
     */
    public static PurchaseDialog newInstance(CosmeticItem item, int userCoinBalance,
                                            OnPurchaseListener listener) {
        PurchaseDialog dialog = new PurchaseDialog();
        dialog.item = item;
        dialog.userCoinBalance = userCoinBalance;
        dialog.listener = listener;
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_purchase, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView imgItem = view.findViewById(R.id.img_purchase_item);
        TextView txtName = view.findViewById(R.id.txt_purchase_item_name);
        TextView txtPrice = view.findViewById(R.id.txt_purchase_price);
        TextView txtInsufficientCoins = view.findViewById(R.id.txt_insufficient_coins);
        Button btnCancel = view.findViewById(R.id.btn_purchase_cancel);
        Button btnBuy = view.findViewById(R.id.btn_purchase_buy);

        // Populate item details
        txtName.setText(item.getName());
        txtPrice.setText(String.valueOf(item.getCoinCost()));

        // Load item image from base64 data
        if (item.getImageData() != null && !item.getImageData().isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(item.getImageData(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                imgItem.setImageBitmap(bitmap);
            } catch (Exception e) {
                imgItem.setImageResource(R.drawable.ic_launcher_foreground);
            }
        } else {
            imgItem.setImageResource(R.drawable.ic_launcher_foreground);
        }

        // Check if user has enough coins
        boolean canAfford = userCoinBalance >= item.getCoinCost();
        if (!canAfford) {
            txtInsufficientCoins.setVisibility(View.VISIBLE);
            btnBuy.setEnabled(false);
        } else {
            txtInsufficientCoins.setVisibility(View.GONE);
            btnBuy.setEnabled(true);
        }

        // Cancel button dismisses the dialog
        btnCancel.setOnClickListener(v -> dismiss());

        // Buy button triggers purchase callback
        btnBuy.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPurchaseConfirmed(item);
            }
            dismiss();
        });
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }
}
