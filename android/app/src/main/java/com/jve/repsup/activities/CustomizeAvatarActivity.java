package com.jve.repsup.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.tabs.TabLayout;
import com.jve.repsup.R;
import com.jve.repsup.adapters.CosmeticGridAdapter;
import com.jve.repsup.database.ConnectionManager.DatabaseCallback;
import com.jve.repsup.dialogs.PurchaseDialog;
import com.jve.repsup.models.Avatar;
import com.jve.repsup.models.CosmeticItem;
import com.jve.repsup.models.OutfitSlot;
import com.jve.repsup.repositories.CosmeticRepository;
import com.jve.repsup.utils.AvatarCompositor;
import com.jve.repsup.utils.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Avatar customization screen (Locker).
 * Displays avatar preview at top, coin balance, category tabs (Body, Face, Hair, Clothing),
 * and a grid of owned/locked cosmetic items.
 *
 * Validates: Requirements 21.1, 21.2, 21.3, 21.4, 21.5
 */
public class CustomizeAvatarActivity extends AppCompatActivity {

    private static final String[] CATEGORIES = {"Body", "Face", "Hair", "Clothing"};

    // Category to cosmetic_type mapping
    private static final Map<String, String[]> CATEGORY_TYPES = new HashMap<>();
    static {
        CATEGORY_TYPES.put("Body", new String[]{"skin", "body"});
        CATEGORY_TYPES.put("Face", new String[]{"face"});
        CATEGORY_TYPES.put("Hair", new String[]{"hair"});
        CATEGORY_TYPES.put("Clothing", new String[]{"top", "bottom", "shoes"});
    }

    private ImageView imgAvatarPreview;
    private TextView txtCoinBalance;
    private TabLayout tabCategories;
    private GridView gridCosmetics;
    private ImageButton btnBack;
    private ImageButton btnSave;

    private CosmeticGridAdapter adapter;
    private CosmeticRepository cosmeticRepository;
    private SessionManager sessionManager;

    private int userId;
    private Avatar userAvatar;
    private OutfitSlot currentOutfitSlot;
    private int coinBalance;

    // All cosmetics loaded from DB
    private List<CosmeticItem> allCosmetics = new ArrayList<>();
    private Set<Integer> ownedCosmeticIds = new HashSet<>();

    // Locally equipped items (not yet persisted)
    private Map<String, CosmeticItem> localEquipped = new HashMap<>();
    private boolean hasUnsavedChanges = false;

    private String currentCategory = "Body";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customize_avatar);

        sessionManager = new SessionManager(this);
        cosmeticRepository = new CosmeticRepository();
        userId = sessionManager.getUserId();

        initViews();
        setupTabs();
        setupClickListeners();
        loadData();
    }

    private void initViews() {
        imgAvatarPreview = findViewById(R.id.img_avatar_preview);
        txtCoinBalance = findViewById(R.id.txt_coin_balance);
        tabCategories = findViewById(R.id.tab_categories);
        gridCosmetics = findViewById(R.id.grid_cosmetics);
        btnBack = findViewById(R.id.btn_back);
        btnSave = findViewById(R.id.btn_save);

        adapter = new CosmeticGridAdapter(this);
        gridCosmetics.setAdapter(adapter);

        adapter.setOnItemClickListener((item, isOwned) -> {
            if (isOwned) {
                equipLocally(item);
            } else {
                // Locked item tapped: show purchase dialog
                showPurchaseDialog(item);
            }
        });
    }

    private void setupTabs() {
        for (String category : CATEGORIES) {
            tabCategories.addTab(tabCategories.newTab().setText(category));
        }

        tabCategories.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentCategory = CATEGORIES[tab.getPosition()];
                filterAndDisplayItems();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> discardAndFinish());
        btnSave.setOnClickListener(v -> saveEquippedItems());
    }

    private void loadData() {
        // Load user coin balance
        loadCoinBalance();

        // Load user avatar record
        cosmeticRepository.getAvatar(userId, new DatabaseCallback<Avatar>() {
            @Override
            public void onSuccess(Avatar result) {
                userAvatar = result;
                runOnUiThread(() -> updateAvatarPreview());
            }

            @Override
            public void onError(Exception e) {
                // Use a fallback avatar if loading fails
                userAvatar = new Avatar();
                userAvatar.setUserId(userId);
                userAvatar.setSkinTone("male");
                userAvatar.setBodyShape("mesomorph");
                runOnUiThread(() -> updateAvatarPreview());
            }
        });

        // Load all cosmetics
        cosmeticRepository.getUserCosmetics(userId, new DatabaseCallback<List<CosmeticItem>>() {
            @Override
            public void onSuccess(List<CosmeticItem> result) {
                allCosmetics = result != null ? result : new ArrayList<>();
                // Load owned items
                loadOwnedCosmetics();
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(CustomizeAvatarActivity.this,
                                "Failed to load cosmetics", Toast.LENGTH_SHORT).show());
            }
        });

        // Load outfit slot for initial equipped state
        cosmeticRepository.getOutfitSlot(userId, new DatabaseCallback<OutfitSlot>() {
            @Override
            public void onSuccess(OutfitSlot result) {
                currentOutfitSlot = result;
                if (result != null) {
                    initializeEquippedFromOutfit(result);
                }
                runOnUiThread(() -> updateAvatarPreview());
            }

            @Override
            public void onError(Exception e) {
                // No outfit slot yet, that's fine
                currentOutfitSlot = new OutfitSlot();
                currentOutfitSlot.setUserId(userId);
            }
        });
    }

    private void loadCoinBalance() {
        String sql = "SELECT coins FROM User WHERE user_id = ?";
        com.jve.repsup.database.ConnectionManager.querySingle(sql, new Object[]{userId},
                rs -> rs.getInt("coins"),
                new DatabaseCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer result) {
                        coinBalance = result != null ? result : 0;
                        runOnUiThread(() -> txtCoinBalance.setText(String.valueOf(coinBalance)));
                    }

                    @Override
                    public void onError(Exception e) {
                        runOnUiThread(() -> txtCoinBalance.setText("0"));
                    }
                });
    }

    private void loadOwnedCosmetics() {
        cosmeticRepository.getOwnedCosmetics(userId, new DatabaseCallback<List<CosmeticItem>>() {
            @Override
            public void onSuccess(List<CosmeticItem> result) {
                ownedCosmeticIds.clear();
                if (result != null) {
                    for (CosmeticItem item : result) {
                        ownedCosmeticIds.add(item.getCosmeticId());
                    }
                }
                runOnUiThread(() -> filterAndDisplayItems());
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> filterAndDisplayItems());
            }
        });
    }

    private void initializeEquippedFromOutfit(OutfitSlot outfit) {
        // Map outfit slot IDs to actual cosmetic items once all cosmetics are loaded
        for (CosmeticItem item : allCosmetics) {
            int id = item.getCosmeticId();
            if ((outfit.getTopId() != null && outfit.getTopId() == id) ||
                    (outfit.getBottomId() != null && outfit.getBottomId() == id) ||
                    (outfit.getShoesId() != null && outfit.getShoesId() == id) ||
                    (outfit.getAccessoriesId() != null && outfit.getAccessoriesId() == id)) {
                localEquipped.put(item.getType(), item);
            }
        }
    }

    /**
     * Filters all cosmetics by the currently selected category tab and updates the grid.
     */
    private void filterAndDisplayItems() {
        String[] types = CATEGORY_TYPES.get(currentCategory);
        if (types == null) return;

        List<CosmeticItem> filtered = new ArrayList<>();
        for (CosmeticItem item : allCosmetics) {
            for (String type : types) {
                if (type.equals(item.getType())) {
                    filtered.add(item);
                    break;
                }
            }
        }

        // Build equipped IDs set for adapter
        Set<Integer> equippedIds = new HashSet<>();
        for (CosmeticItem equipped : localEquipped.values()) {
            equippedIds.add(equipped.getCosmeticId());
        }

        adapter.setItems(filtered);
        adapter.setOwnedIds(ownedCosmeticIds);
        adapter.setEquippedIds(equippedIds);
    }

    /**
     * Equips an owned item locally (no DB write yet) and updates the avatar preview.
     * Only one item per type can be equipped at a time.
     */
    private void equipLocally(CosmeticItem item) {
        // Replace any existing item of the same type
        localEquipped.put(item.getType(), item);
        hasUnsavedChanges = true;

        // Update grid to show new equipped state
        filterAndDisplayItems();

        // Update avatar preview in real-time
        updateAvatarPreview();
    }

    /**
     * Updates the avatar preview using AvatarCompositor with currently equipped items.
     */
    private void updateAvatarPreview() {
        if (userAvatar == null) return;

        List<CosmeticItem> equippedList = new ArrayList<>(localEquipped.values());

        // Build a temporary outfit slot for the compositor
        OutfitSlot tempSlot = new OutfitSlot();
        tempSlot.setUserId(userId);
        for (CosmeticItem item : equippedList) {
            switch (item.getType()) {
                case "top":
                    tempSlot.setTopId(item.getCosmeticId());
                    break;
                case "bottom":
                    tempSlot.setBottomId(item.getCosmeticId());
                    break;
                case "shoes":
                    tempSlot.setShoesId(item.getCosmeticId());
                    break;
            }
        }

        Bitmap avatarBitmap = AvatarCompositor.composite(this, userAvatar, tempSlot);
        imgAvatarPreview.setImageBitmap(avatarBitmap);
    }

    /**
     * Persists equipped items to User_Cosmetic and Outfit_Slot tables.
     */
    private void saveEquippedItems() {
        List<CosmeticItem> equippedList = new ArrayList<>(localEquipped.values());

        cosmeticRepository.saveAllEquipped(userId, equippedList, new DatabaseCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                hasUnsavedChanges = false;
                runOnUiThread(() -> {
                    Toast.makeText(CustomizeAvatarActivity.this,
                            "Outfit saved!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(CustomizeAvatarActivity.this,
                                "Failed to save outfit", Toast.LENGTH_SHORT).show());
            }
        });
    }

    /**
     * Shows the purchase dialog for a locked cosmetic item.
     * Validates: Requirements 22.1, 22.3
     */
    private void showPurchaseDialog(CosmeticItem item) {
        PurchaseDialog dialog = PurchaseDialog.newInstance(item, coinBalance, purchasedItem -> {
            // User confirmed purchase — execute the purchase flow
            cosmeticRepository.purchaseCosmetic(userId, purchasedItem.getCosmeticId(),
                    purchasedItem.getCoinCost(), new DatabaseCallback<Integer>() {
                        @Override
                        public void onSuccess(Integer result) {
                            runOnUiThread(() -> onPurchaseSuccess(purchasedItem));
                        }

                        @Override
                        public void onError(Exception e) {
                            runOnUiThread(() ->
                                    Toast.makeText(CustomizeAvatarActivity.this,
                                            "Purchase failed", Toast.LENGTH_SHORT).show());
                        }
                    });
        });
        dialog.show(getSupportFragmentManager(), "PurchaseDialog");
    }

    /**
     * Handles successful purchase: updates coin balance, marks item as owned, refreshes grid.
     * Validates: Requirement 22.2
     */
    private void onPurchaseSuccess(CosmeticItem item) {
        // Deduct cost from local coin balance and update display
        coinBalance -= item.getCoinCost();
        txtCoinBalance.setText(String.valueOf(coinBalance));

        // Mark item as owned locally
        ownedCosmeticIds.add(item.getCosmeticId());

        // Refresh the grid to show the item as unlocked
        filterAndDisplayItems();

        Toast.makeText(this, "Purchased " + item.getName() + "!", Toast.LENGTH_SHORT).show();
    }

    /**
     * Discards unsaved changes and finishes the activity.
     */
    private void discardAndFinish() {
        // Simply finish without saving — changes are discarded
        hasUnsavedChanges = false;
        finish();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        discardAndFinish();
    }
}
