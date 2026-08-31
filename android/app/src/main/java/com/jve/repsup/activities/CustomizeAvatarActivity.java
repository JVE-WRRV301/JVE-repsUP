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
import com.jve.repsup.utils.AssetImageLoader;
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

    // Locally equipped items, keyed by cosmetic type (top/bottom/shoes/socks/hair/accessory).
    private Map<String, CosmeticItem> localEquipped = new HashMap<>();
    // Cosmetic IDs the user owns (unlocked), used to distinguish equippable vs purchasable.
    private Set<Integer> ownedCosmeticIds = new HashSet<>();
    private boolean hasUnsavedChanges = false;

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
        populateGridForType("hair");
    }

    private void showClothingTab() {
        skinColorScroll.setVisibility(View.GONE);
        setupSubTabs(CLOTHING_SUBTABS);
        tabSubcategories.setVisibility(View.VISIBLE);
        currentSubcategory = CLOTHING_SUBTABS[0];
        populateClothingTab();
    }

    private void populateClothingTab() {
        String type = currentClothingType();
        if (type == null) {
            showEmptyGrid();     // e.g. "Special" - no items authored yet
        } else {
            populateGridForType(type);
        }
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
            populateClothingTab();
        }
    }

    /**
     * Maps the current Clothing subtab label to its Cosmetic_Item.cosmetic_type.
     * Note the DB uses the SINGULAR "accessory". Returns null for subtabs that
     * have no cosmetic backing (e.g. "Special").
     */
    private String currentClothingType() {
        switch (currentSubcategory) {
            case "Top":         return "top";
            case "Bottom":      return "bottom";
            case "Shoes":       return "shoes";
            case "Accessories": return "accessory";
            default:            return null; // "Special" has no items yet
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
        // The Body tab's control is the skin-tone picker (rendered above the grid
        // via populateSkinColors). Body type comes from the fitness assessment and
        // is not changeable here, so the grid itself is empty.
        gridItems.removeAllViews();
    }

    // ==================== FACE TAB (no cosmetic data yet) ====================

    private void populateFacePlaceholders() {
        // Face details are stored on Avatar.facial_details, not as cosmetics.
        // There is no Cosmetic_Item data backing this tab yet, so show empty.
        showEmptyGrid();
    }

    // ==================== REAL DB-BACKED GRID ====================

    /**
     * Loads all cosmetics of the given type for the user's gender and renders
     * them: owned items are selectable/equippable, locked items open the purchase
     * dialog. The currently equipped item of this type shows the selected state.
     */
    private void populateGridForType(String cosmeticType) {
        gridItems.removeAllViews();

        cosmeticRepository.getCosmeticsForTab(cosmeticType, userGender,
                new DatabaseCallback<List<CosmeticItem>>() {
                    @Override
                    public void onSuccess(List<CosmeticItem> items) {
                        runOnUiThread(() -> renderGrid(items));
                    }

                    @Override
                    public void onError(Exception e) {
                        runOnUiThread(() -> showEmptyGrid());
                    }
                });
    }

    private void renderGrid(List<CosmeticItem> items) {
        gridItems.removeAllViews();
        if (items == null || items.isEmpty()) {
            showEmptyGrid();
            return;
        }
        for (CosmeticItem item : items) {
            gridItems.addView(createCosmeticItemView(item));
        }
    }

    /**
     * Clears the grid (used for tabs with no items, e.g. Face or Special).
     */
    private void showEmptyGrid() {
        gridItems.removeAllViews();
    }

    /**
     * Builds one grid cell for a real cosmetic item.
     * Owned items are tappable to equip/unequip; locked items show a price and
     * open the purchase dialog.
     */
    private View createCosmeticItemView(CosmeticItem item) {
        View view = LayoutInflater.from(this)
                .inflate(R.layout.item_locker_placeholder, gridItems, false);

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
        ImageView imgItem = view.findViewById(R.id.img_placeholder);

        // Load the item's thumbnail from its asset path (off the UI thread).
        loadItemThumbnail(imgItem, item.getImageData());

        // Free items (cost 0) are treated as owned so they can be equipped directly,
        // without a 0-coin "purchase" step. Paid items must be bought first.
        boolean owned = item.getCoinCost() <= 0 || ownedCosmeticIds.contains(item.getCosmeticId());
        boolean equipped = isEquipped(item);

        if (equipped) {
            container.setBackgroundResource(R.drawable.bg_placeholder_item_selected);
            imgCheck.setVisibility(View.VISIBLE);
        } else {
            container.setBackgroundResource(R.drawable.bg_placeholder_item);
            imgCheck.setVisibility(View.GONE);
        }

        if (owned) {
            lockOverlay.setVisibility(View.GONE);
            view.setOnClickListener(v -> toggleEquip(item));
        } else {
            lockOverlay.setVisibility(View.VISIBLE);
            txtPrice.setText(String.valueOf(item.getCoinCost()));
            view.setOnClickListener(v -> showPurchaseDialog(item));
        }

        return view;
    }

    /**
     * Loads a cosmetic thumbnail from its assets path on a background thread,
     * falling back to the default placeholder icon when unavailable.
     */
    private void loadItemThumbnail(ImageView target, String assetPath) {
        // The layout applies a muted tint to the placeholder icon; keep it for the
        // fallback, but clear it once a real item image loads so colours show true.
        target.setColorFilter(ContextCompat.getColor(this, R.color.muted));
        target.setImageResource(R.drawable.ic_launcher_foreground);
        if (assetPath == null || assetPath.trim().isEmpty()) {
            return;
        }
        new Thread(() -> {
            Bitmap bmp = AssetImageLoader.load(getApplicationContext(), assetPath);
            if (bmp != null) {
                runOnUiThread(() -> {
                    target.clearColorFilter();
                    target.setImageBitmap(bmp);
                });
            }
        }).start();
    }

    /**
     * True when the given item is the one currently equipped in its type slot.
     */
    private boolean isEquipped(CosmeticItem item) {
        if (item.getType() == null) return false;
        CosmeticItem equipped = localEquipped.get(item.getType().toLowerCase());
        return equipped != null && equipped.getCosmeticId() == item.getCosmeticId();
    }

    /**
     * Equips the item (or unequips it if it was already the equipped one),
     * then refreshes the grid and the avatar preview. Only one item per type.
     */
    private void toggleEquip(CosmeticItem item) {
        if (item.getType() == null) return;
        String type = item.getType().toLowerCase();

        if (isEquipped(item)) {
            localEquipped.remove(type);   // tap the equipped item again to remove it
        } else {
            localEquipped.put(type, item);
        }
        hasUnsavedChanges = true;
        refreshCurrentGrid();
        updateAvatarPreview();
    }

    /**
     * Shows the purchase dialog for a locked item, backed by a real DB purchase.
     */
    private void showPurchaseDialog(CosmeticItem item) {
        PurchaseDialog dialog = PurchaseDialog.newInstance(item, coinBalance,
                purchasedItem -> onPurchaseConfirmed(purchasedItem));
        dialog.show(getSupportFragmentManager(), "PurchaseDialog");
    }

    /**
     * Persists a purchase: deducts coins and unlocks the item in the DB, then
     * marks it owned locally and refreshes the grid.
     */
    private void onPurchaseConfirmed(CosmeticItem item) {
        cosmeticRepository.purchaseCosmetic(userId, item.getCosmeticId(), item.getCoinCost(),
                new DatabaseCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer result) {
                        coinBalance -= item.getCoinCost();
                        ownedCosmeticIds.add(item.getCosmeticId());
                        runOnUiThread(() -> {
                            txtCoinBalance.setText(String.valueOf(coinBalance));
                            Toast.makeText(CustomizeAvatarActivity.this,
                                    getString(R.string.locker_item_purchased, item.getName()),
                                    Toast.LENGTH_SHORT).show();
                            refreshCurrentGrid();
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        runOnUiThread(() -> Toast.makeText(CustomizeAvatarActivity.this,
                                getString(R.string.locker_save_error), Toast.LENGTH_SHORT).show());
                    }
                });
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
                populateGridForType("hair");
                break;
            case "Clothing":
                populateClothingTab();
                break;
        }
    }

    // ==================== DATA LOADING ====================

    private void loadData() {
        loadCoinBalance();
        loadGender();
        loadOwnedCosmetics();
        loadEquippedItems();
        loadAvatar();
    }

    /**
     * Loads the set of cosmetic IDs the user owns, so the grid can distinguish
     * owned (equippable) items from locked (purchasable) ones.
     */
    private void loadOwnedCosmetics() {
        cosmeticRepository.getOwnedCosmetics(userId, new DatabaseCallback<List<CosmeticItem>>() {
            @Override
            public void onSuccess(List<CosmeticItem> items) {
                ownedCosmeticIds.clear();
                if (items != null) {
                    for (CosmeticItem item : items) {
                        ownedCosmeticIds.add(item.getCosmeticId());
                    }
                }
                runOnUiThread(() -> refreshCurrentGrid());
            }

            @Override
            public void onError(Exception e) { /* grid still renders, all locked */ }
        });
    }

    /**
     * Loads the user's currently equipped outfit items into localEquipped so the
     * preview shows the saved outfit and the grid marks equipped items selected.
     */
    private void loadEquippedItems() {
        cosmeticRepository.getEquippedItemsForOutfit(userId, new DatabaseCallback<List<CosmeticItem>>() {
            @Override
            public void onSuccess(List<CosmeticItem> items) {
                localEquipped.clear();
                if (items != null) {
                    for (CosmeticItem item : items) {
                        if (item.getType() != null) {
                            localEquipped.put(item.getType().toLowerCase(), item);
                        }
                    }
                }
                runOnUiThread(() -> {
                    refreshCurrentGrid();
                    updateAvatarPreview();
                });
            }

            @Override
            public void onError(Exception e) { /* preview shows body only */ }
        });
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
