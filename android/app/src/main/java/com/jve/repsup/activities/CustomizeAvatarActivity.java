package com.jve.repsup.activities;

import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.tabs.TabLayout;
import com.jve.repsup.R;
import com.jve.repsup.database.ConnectionManager.DatabaseCallback;
import com.jve.repsup.dialogs.PurchaseDialog;
import com.jve.repsup.models.Avatar;
import com.jve.repsup.models.CosmeticItem;
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
 * Displays avatar preview at top (expandable), coin balance, category tabs (Body, Face, Hair, Clothing),
 * sub-tabs for Face and Clothing, and a grid of placeholder items.
 *
 * Body tab: skin colour picker row + 6 body type placeholders (all unlocked).
 * Face tab: subtabs (Nose, Eyes, Mouth, Eyebrows) — 9 placeholders each, all unlocked.
 * Hair tab: 9 placeholders, first 3 free, rest locked.
 * Clothing tab: subtabs (Top, Bottom, Shoes, Accessories, Special) — 9 placeholders each,
 *   first 3 free in regular subtabs; all locked in Special with higher prices.
 */
public class CustomizeAvatarActivity extends AppCompatActivity {

    private static final String[] CATEGORIES = {"Body", "Face", "Hair", "Clothing"};
    private static final String[] FACE_SUBTABS = {"Nose", "Eyes", "Mouth", "Eyebrows"};
    private static final String[] CLOTHING_SUBTABS = {"Top", "Bottom", "Shoes", "Accessories", "Special"};

    // Skin tones: names stored in Avatar.skin_tone, with a representative swatch each.
    private static final String[] SKIN_TONE_NAMES = {"fair", "beige", "tan", "dark"};
    private static final String[] SKIN_TONE_SWATCHES = {"#F0CB8B", "#D19B5B", "#A0683B", "#5C3A1E"};

    // Clothing prices for locked items (positions 3-8)
    private static final int[] CLOTHING_PRICES = {50, 75, 100, 150, 200, 300};
    // Special tab prices (all 9 items locked)
    private static final int[] SPECIAL_PRICES = {500, 600, 700, 800, 900, 1000, 1200, 1500, 2000};
    // Hair prices for locked items (positions 3-8)
    private static final int[] HAIR_PRICES = {50, 75, 100, 150, 200, 300};

    // Views
    private ImageView imgAvatarPreview;
    private FrameLayout avatarPreviewContainer;
    private TextView txtCoinBalance;
    private TabLayout tabCategories;
    private TabLayout tabSubcategories;
    private GridLayout gridItems;
    private HorizontalScrollView skinColorScroll;
    private LinearLayout skinColorRow;
    private LinearLayout lockerPanel;
    private LinearLayout btnExpandCollapse;
    private ImageView imgExpandArrow;
    private ImageButton btnBack;
    private ImageButton btnSave;

    private CosmeticRepository cosmeticRepository;
    private SessionManager sessionManager;

    private int userId;
    private Avatar userAvatar;
    private int coinBalance;
    private boolean isExpanded = false;

    private String currentCategory = "Body";
    private String currentSubcategory = "";
    private int selectedSkinColorIndex = 0;
    private String userGender = "male"; // loaded from User_Profile; drives base body art

    // Track locally equipped items (by type)
    private Map<String, CosmeticItem> localEquipped = new HashMap<>();
    private Set<Integer> ownedCosmeticIds = new HashSet<>();
    private List<CosmeticItem> allCosmetics = new ArrayList<>();
    private boolean hasUnsavedChanges = false;

    // Track selected placeholder index per tab/subtab key
    private Map<String, Integer> selectedIndexPerTab = new HashMap<>();

    // Track which placeholder items have been unlocked (purchased) per tab key
    private Map<String, Set<Integer>> unlockedIndexPerTab = new HashMap<>();

    // Avatar preview heights
    private static final int AVATAR_HEIGHT_COLLAPSED = 200; // dp
    private static final int AVATAR_HEIGHT_EXPANDED = 420; // dp

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customize_avatar);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                discardAndFinish();
            }
        });

        sessionManager = new SessionManager(this);
        cosmeticRepository = new CosmeticRepository();
        userId = sessionManager.getUserId();

        initViews();
        setupTabs();
        setupExpandCollapse();
        setupClickListeners();
        loadData();

        // Show Body tab content by default
        showBodyTab();
    }

    private void initViews() {
        imgAvatarPreview = findViewById(R.id.img_avatar_preview);
        avatarPreviewContainer = findViewById(R.id.avatar_preview_container);
        txtCoinBalance = findViewById(R.id.txt_coin_balance);
        tabCategories = findViewById(R.id.tab_categories);
        tabSubcategories = findViewById(R.id.tab_subcategories);
        gridItems = findViewById(R.id.grid_items);
        skinColorScroll = findViewById(R.id.skin_color_scroll);
        skinColorRow = findViewById(R.id.skin_color_row);
        lockerPanel = findViewById(R.id.locker_panel);
        btnExpandCollapse = findViewById(R.id.btn_expand_collapse);
        imgExpandArrow = findViewById(R.id.img_expand_arrow);
        btnBack = findViewById(R.id.btn_back);
        btnSave = findViewById(R.id.btn_save);
    }

    private void setupTabs() {
        for (String category : CATEGORIES) {
            tabCategories.addTab(tabCategories.newTab().setText(category));
        }

        tabCategories.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentCategory = CATEGORIES[tab.getPosition()];
                onCategoryChanged();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupExpandCollapse() {
        btnExpandCollapse.setOnClickListener(v -> toggleExpand());
    }

    private void toggleExpand() {
        isExpanded = !isExpanded;

        float density = getResources().getDisplayMetrics().density;
        int startHeight = isExpanded
                ? (int) (AVATAR_HEIGHT_COLLAPSED * density)
                : (int) (AVATAR_HEIGHT_EXPANDED * density);
        int endHeight = isExpanded
                ? (int) (AVATAR_HEIGHT_EXPANDED * density)
                : (int) (AVATAR_HEIGHT_COLLAPSED * density);

        ValueAnimator animator = ValueAnimator.ofInt(startHeight, endHeight);
        animator.setDuration(300);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            ViewGroup.LayoutParams params = avatarPreviewContainer.getLayoutParams();
            params.height = value;
            avatarPreviewContainer.setLayoutParams(params);
        });
        animator.start();

        // Rotate the arrow
        imgExpandArrow.animate()
                .rotation(isExpanded ? 180f : 0f)
                .setDuration(300)
                .start();
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> discardAndFinish());
        btnSave.setOnClickListener(v -> saveEquippedItems());
    }

    // ==================== TAB SWITCHING ====================

    private void onCategoryChanged() {
        switch (currentCategory) {
            case "Body":
                showBodyTab();
                break;
            case "Face":
                showFaceTab();
                break;
            case "Hair":
                showHairTab();
                break;
            case "Clothing":
                showClothingTab();
                break;
        }
    }

    private void showBodyTab() {
        tabSubcategories.setVisibility(View.GONE);
        skinColorScroll.setVisibility(View.VISIBLE);
        populateSkinColors();
        populateBodyPlaceholders();
    }

    private void showFaceTab() {
        skinColorScroll.setVisibility(View.GONE);
        setupSubTabs(FACE_SUBTABS);
        tabSubcategories.setVisibility(View.VISIBLE);
        currentSubcategory = FACE_SUBTABS[0];
        populateFacePlaceholders();
    }

    private void showHairTab() {
        tabSubcategories.setVisibility(View.GONE);
        skinColorScroll.setVisibility(View.GONE);
        populateHairPlaceholders();
    }

    private void showClothingTab() {
        skinColorScroll.setVisibility(View.GONE);
        setupSubTabs(CLOTHING_SUBTABS);
        tabSubcategories.setVisibility(View.VISIBLE);
        currentSubcategory = CLOTHING_SUBTABS[0];
        populateClothingPlaceholders();
    }

    private void setupSubTabs(String[] subtabs) {
        tabSubcategories.removeAllTabs();
        tabSubcategories.clearOnTabSelectedListeners();

        for (String subtab : subtabs) {
            tabSubcategories.addTab(tabSubcategories.newTab().setText(subtab));
        }

        tabSubcategories.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentSubcategory = subtabs[tab.getPosition()];
                onSubcategoryChanged();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void onSubcategoryChanged() {
        if ("Face".equals(currentCategory)) {
            populateFacePlaceholders();
        } else if ("Clothing".equals(currentCategory)) {
            populateClothingPlaceholders();
        }
    }

    // ==================== BODY TAB ====================

    private void populateSkinColors() {
        skinColorRow.removeAllViews();

        float density = getResources().getDisplayMetrics().density;
        int size = (int) (40 * density);
        int margin = (int) (6 * density);

        for (int i = 0; i < SKIN_TONE_NAMES.length; i++) {
            View colorView = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(margin, margin, margin, margin);
            colorView.setLayoutParams(params);

            GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            circle.setColor(android.graphics.Color.parseColor(SKIN_TONE_SWATCHES[i]));

            if (i == selectedSkinColorIndex) {
                circle.setStroke((int) (3 * density),
                        ContextCompat.getColor(this, R.color.light_blue));
            }

            colorView.setBackground(circle);

            final int index = i;
            colorView.setOnClickListener(v -> onSkinToneSelected(index));

            skinColorRow.addView(colorView);
        }
    }

    /**
     * Sets the selected swatch index to match the avatar's stored skin tone.
     * Grey (the pre-selection default) leaves index 0 highlighted without
     * implying the user has chosen a tone.
     */
    private void syncSelectedToneFromAvatar() {
        if (userAvatar == null || userAvatar.getSkinTone() == null) {
            return;
        }
        String tone = userAvatar.getSkinTone().trim().toLowerCase();
        for (int i = 0; i < SKIN_TONE_NAMES.length; i++) {
            if (SKIN_TONE_NAMES[i].equals(tone)) {
                selectedSkinColorIndex = i;
                return;
            }
        }
    }

    /**
     * Applies a chosen skin tone: updates the avatar model, persists it, and
     * refreshes the preview so the base body re-renders in the new tone.
     */
    private void onSkinToneSelected(int index) {
        selectedSkinColorIndex = index;
        hasUnsavedChanges = true;

        String tone = SKIN_TONE_NAMES[index];
        if (userAvatar != null) {
            userAvatar.setSkinTone(tone);
        }

        // Persist the tone immediately (avatar body is a per-user attribute,
        // not part of the outfit save).
        cosmeticRepository.updateSkinTone(userId, tone, new DatabaseCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) { /* no-op */ }

            @Override
            public void onError(Exception e) { /* best-effort; preview still updates */ }
        });

        populateSkinColors(); // refresh selection ring
        updateAvatarPreview();
    }

    private void populateBodyPlaceholders() {
        gridItems.removeAllViews();

        // 6 body type placeholders, 3 per row, all unlocked
        for (int i = 0; i < 6; i++) {
            View item = createPlaceholderItem(false, 0, i);
            gridItems.addView(item);
        }
    }

    // ==================== FACE TAB ====================

    private void populateFacePlaceholders() {
        gridItems.removeAllViews();

        // 9 placeholders per face subtab, all unlocked
        for (int i = 0; i < 9; i++) {
            View item = createPlaceholderItem(false, 0, i);
            gridItems.addView(item);
        }
    }

    // ==================== HAIR TAB ====================

    private void populateHairPlaceholders() {
        gridItems.removeAllViews();

        for (int i = 0; i < 9; i++) {
            if (i < 3) {
                // First 3 are free/unlocked
                View item = createPlaceholderItem(false, 0, i);
                gridItems.addView(item);
            } else {
                // Rest are locked with prices
                int price = HAIR_PRICES[i - 3];
                View item = createPlaceholderItem(true, price, i);
                gridItems.addView(item);
            }
        }
    }

    // ==================== CLOTHING TAB ====================

    private void populateClothingPlaceholders() {
        gridItems.removeAllViews();

        boolean isSpecial = "Special".equals(currentSubcategory);

        for (int i = 0; i < 9; i++) {
            if (isSpecial) {
                // All items in Special tab are locked and expensive
                int price = SPECIAL_PRICES[i];
                View item = createPlaceholderItem(true, price, i);
                gridItems.addView(item);
            } else {
                if (i < 3) {
                    // First 3 are free/unlocked
                    View item = createPlaceholderItem(false, 0, i);
                    gridItems.addView(item);
                } else {
                    // Rest are locked with prices
                    int price = CLOTHING_PRICES[i - 3];
                    View item = createPlaceholderItem(true, price, i);
                    gridItems.addView(item);
                }
            }
        }
    }

    // ==================== PLACEHOLDER ITEM CREATION ====================

    /**
     * Returns a key string for tracking selection state in the current tab/subtab.
     */
    private String getCurrentTabKey() {
        if ("Face".equals(currentCategory) || "Clothing".equals(currentCategory)) {
            return currentCategory + "_" + currentSubcategory;
        }
        return currentCategory;
    }

    /**
     * Creates a placeholder rounded square item for the grid.
     *
     * @param isLocked Whether the item shows a lock overlay with price
     * @param price    The coin price (only relevant if isLocked is true)
     * @param index    The position index of this item in the current grid
     * @return The inflated and configured view
     */
    private View createPlaceholderItem(boolean isLocked, int price, int index) {
        View view = LayoutInflater.from(this)
                .inflate(R.layout.item_locker_placeholder, gridItems, false);

        // Set grid layout params for 3-column layout
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
        params.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
        view.setLayoutParams(params);

        View lockOverlay = view.findViewById(R.id.lock_overlay);
        TextView txtPrice = view.findViewById(R.id.txt_price);
        LinearLayout container = view.findViewById(R.id.placeholder_container);
        ImageView imgCheck = view.findViewById(R.id.img_selected_check);

        // Check if this item was purchased during this session
        String tabKey = getCurrentTabKey();
        Set<Integer> unlocked = unlockedIndexPerTab.get(tabKey);
        boolean effectivelyLocked = isLocked && (unlocked == null || !unlocked.contains(index));

        // Check if this item is currently selected
        Integer selectedIdx = selectedIndexPerTab.get(tabKey);
        if (selectedIdx != null && selectedIdx == index && !effectivelyLocked) {
            container.setBackgroundResource(R.drawable.bg_placeholder_item_selected);
            imgCheck.setVisibility(View.VISIBLE);
        } else {
            container.setBackgroundResource(R.drawable.bg_placeholder_item);
            imgCheck.setVisibility(View.GONE);
        }

        if (effectivelyLocked) {
            lockOverlay.setVisibility(View.VISIBLE);
            txtPrice.setText(String.valueOf(price));

            // Tap locked item to show purchase dialog
            final int itemPrice = price;
            final int itemIndex = index;
            view.setOnClickListener(v -> showPurchaseDialogForPlaceholder(itemPrice, itemIndex));
        } else {
            lockOverlay.setVisibility(View.GONE);

            // Tap unlocked item — select it (blue outline)
            final int itemIndex = index;
            view.setOnClickListener(v -> {
                selectedIndexPerTab.put(tabKey, itemIndex);
                hasUnsavedChanges = true;
                refreshCurrentGrid();
            });
        }

        return view;
    }

    /**
     * Shows the PurchaseDialog for a locked placeholder item.
     */
    private void showPurchaseDialogForPlaceholder(int price, int index) {
        // Create a placeholder CosmeticItem to pass to the dialog
        CosmeticItem placeholderItem = new CosmeticItem();
        placeholderItem.setCosmeticId(-(index + 1)); // Negative ID for placeholder
        placeholderItem.setName(currentCategory + " Item " + (index + 1));
        placeholderItem.setType(currentCategory.toLowerCase());
        placeholderItem.setCoinCost(price);
        placeholderItem.setImageData(null);

        String tabKey = getCurrentTabKey();

        PurchaseDialog dialog = PurchaseDialog.newInstance(placeholderItem, coinBalance, purchasedItem -> {
            // Deduct coins locally and mark item as unlocked
            coinBalance -= purchasedItem.getCoinCost();

            // Track this item as unlocked
            Set<Integer> unlocked = unlockedIndexPerTab.get(tabKey);
            if (unlocked == null) {
                unlocked = new HashSet<>();
                unlockedIndexPerTab.put(tabKey, unlocked);
            }
            unlocked.add(index);

            runOnUiThread(() -> {
                txtCoinBalance.setText(String.valueOf(coinBalance));
                Toast.makeText(this,
                        getString(R.string.locker_item_purchased, purchasedItem.getName()),
                        Toast.LENGTH_SHORT).show();
                refreshCurrentGrid();
            });
        });
        dialog.show(getSupportFragmentManager(), "PurchaseDialog");
    }

    /**
     * Refreshes the current grid based on active category/subcategory.
     */
    private void refreshCurrentGrid() {
        switch (currentCategory) {
            case "Body":
                populateBodyPlaceholders();
                break;
            case "Face":
                populateFacePlaceholders();
                break;
            case "Hair":
                populateHairPlaceholders();
                break;
            case "Clothing":
                populateClothingPlaceholders();
                break;
        }
    }

    // ==================== DATA LOADING ====================

    private void loadData() {
        loadCoinBalance();
        loadGender();
        loadAvatar();
    }

    private void loadGender() {
        cosmeticRepository.getUserGender(userId, new DatabaseCallback<String>() {
            @Override
            public void onSuccess(String result) {
                if (result != null && !result.trim().isEmpty()) {
                    userGender = result.trim().toLowerCase();
                    runOnUiThread(() -> updateAvatarPreview());
                }
            }

            @Override
            public void onError(Exception e) { /* keep default gender */ }
        });
    }

    private void loadCoinBalance() {
        cosmeticRepository.getCoinBalance(userId, new DatabaseCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                coinBalance = result != null ? result : 0;
                runOnUiThread(() -> txtCoinBalance.setText(String.valueOf(coinBalance)));
            }

            @Override
            public void onError(Exception e) {
                coinBalance = 0;
                runOnUiThread(() -> txtCoinBalance.setText("0"));
            }
        });
    }

    private void loadAvatar() {
        cosmeticRepository.getAvatar(userId, new DatabaseCallback<Avatar>() {
            @Override
            public void onSuccess(Avatar result) {
                userAvatar = result;
                syncSelectedToneFromAvatar();
                runOnUiThread(() -> {
                    if ("Body".equals(currentCategory)) {
                        populateSkinColors();
                    }
                    updateAvatarPreview();
                });
            }

            @Override
            public void onError(Exception e) {
                userAvatar = new Avatar();
                userAvatar.setUserId(userId);
                runOnUiThread(() -> updateAvatarPreview());
            }
        });
    }

    private void updateAvatarPreview() {
        if (userAvatar == null) return;

        // Snapshot the currently equipped items and body attributes to pass to the compositor.
        final List<CosmeticItem> equipped = new ArrayList<>(localEquipped.values());
        final String gender = userGender;
        final String bodyType = userAvatar.getBodyShape();
        final String skinTone = userAvatar.getSkinTone();

        // Compositing decodes assets from disk, so do it off the UI thread.
        new Thread(() -> {
            Bitmap avatarBitmap = AvatarCompositor.composite(
                    getApplicationContext(), gender, bodyType, skinTone, equipped);
            runOnUiThread(() -> imgAvatarPreview.setImageBitmap(avatarBitmap));
        }).start();
    }

    // ==================== SAVE / DISCARD ====================

    private void saveEquippedItems() {
        List<CosmeticItem> equippedList = new ArrayList<>(localEquipped.values());

        cosmeticRepository.saveAllEquipped(userId, equippedList, new DatabaseCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                hasUnsavedChanges = false;
                runOnUiThread(() -> {
                    Toast.makeText(CustomizeAvatarActivity.this,
                            getString(R.string.locker_outfit_saved), Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(CustomizeAvatarActivity.this,
                                getString(R.string.locker_save_error), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void discardAndFinish() {
        hasUnsavedChanges = false;
        finish();
    }

    // ==================== UTILITY ====================

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
