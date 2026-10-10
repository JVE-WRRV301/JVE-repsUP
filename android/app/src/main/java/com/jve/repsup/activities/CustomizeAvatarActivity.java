package com.jve.repsup.activities;

import android.util.Log;
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

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.jve.repsup.R;
import com.jve.repsup.database.ConnectionManager.DatabaseCallback;
import com.jve.repsup.dialogs.PurchaseDialog;
import com.jve.repsup.utils.DialogUtils;
import com.jve.repsup.utils.InsetsHelper;
import com.jve.repsup.utils.SnackbarUtils;
import com.jve.repsup.models.Avatar;
import com.jve.repsup.models.CosmeticItem;
import com.jve.repsup.repositories.CosmeticRepository;
import com.jve.repsup.utils.AvatarCompositor;
import com.jve.repsup.utils.AvatarState;
import com.jve.repsup.utils.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Avatar customisation screen (Locker).
 * Shows the avatar preview at the top (it can be lowered to fill the screen), the coin
 * balance, the category tabs (Body, Hair, Clothing), the Clothing sub-tabs and a grid of items.
 *
 * Body tab: skin colour row + one thumbnail per body type, drawn in the current skin tone.
 * Hair tab: hair styles from the database, plus a colour row for colourable styles.
 * Clothing tab: sub-tabs Top, Bottom and Shoes, each listing the items from the database.
 *
 * Items that cost 0 coins are free to wear. Paid items show their price and open the
 * purchase dialog. "Placeholder" items are not wearable yet and show "Soon" instead of a price.
 * Nothing is written to the database until Save is tapped; purchases are the exception
 * because they spend coins straight away.
 */
public class CustomizeAvatarActivity extends AppCompatActivity {

    private static final String TAG = "CustomizeAvatarActivity";

    private static final String[] CATEGORIES = {"Body", "Hair", "Clothing"};
    private static final String[] CLOTHING_SUBTABS = {"Top", "Bottom", "Shoes"};

    // Skin tones: names stored in Avatar.skin_tone, with a representative swatch each.
    private static final String[] SKIN_TONE_NAMES = {"fair", "tan", "dark"};
    private static final String[] SKIN_TONE_SWATCHES = {"#F0CB8B", "#A0683B", "#5C3A1E"};

    // Hair colours: names stored in Avatar.hair_colour, with a representative swatch each.
    // Colours without art yet fall back to grey in the compositor.
    private static final String[] HAIR_COLOUR_NAMES = {"grey", "brown", "black", "blonde", "ginger"};
    private static final String[] HAIR_COLOUR_SWATCHES = {"#9E9E9E", "#5A3A1E", "#1A1A1A", "#E6C878", "#B0592B"};

    // Body types the user can pick on the Body tab (stored in Avatar.body_shape),
    // gender-specific since the app only ships art for these three per gender.
    // Each is previewed with the avatar_{gender}_{bodytype} drawable.
    private static final String[] MALE_BODY_TYPES = {"average", "built", "chubby"};
    private static final String[] FEMALE_BODY_TYPES = {"average", "toned", "chubby"};

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
    private int selectedSkinColorIndex = -1; // -1 = no tone chosen yet (grey default)
    private int selectedHairColourIndex = 0;
    private String userGender = "male"; // loaded from User_Profile; drives base body art

    // Locally equipped items, keyed by cosmetic type (top/bottom/shoes/socks/hair/accessory).
    private Map<String, CosmeticItem> localEquipped = new HashMap<>();
    // Cosmetic IDs the user owns (unlocked), used to distinguish equippable vs purchasable.
    private Set<Integer> ownedCosmeticIds = new HashSet<>();
    private boolean hasUnsavedChanges = false;

    // Avatar preview heights
    private static final int AVATAR_HEIGHT_COLLAPSED = 200; // dp
    // Fixed pieces of the locker panel (dp), used to work out how tall the avatar can be
    // when lowered: handle 36, category tabs 44, sub tabs 36, colour row 52 + 12 margin,
    // grid padding 24 and one row of items 116.
    private static final int PANEL_HANDLE_DP = 36;
    private static final int PANEL_CATEGORY_TABS_DP = 44;
    private static final int PANEL_SUB_TABS_DP = 36;
    private static final int PANEL_COLOUR_ROW_DP = 64;
    private static final int PANEL_GRID_PADDING_DP = 24;
    private static final int PANEL_FIRST_ROW_DP = 116;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Draw edge-to-edge, then keep content clear of the status/nav bars
        // (replaces the old hard-coded 32dp top padding on the top bar).
        InsetsHelper.enableEdgeToEdge(getWindow());

        setContentView(R.layout.activity_customize_avatar);

        InsetsHelper.applySystemBarsPadding(findViewById(R.id.customizeAvatarRoot));

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                confirmDiscardOrFinish();
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

        animateAvatarHeightTo(isExpanded
                ? computeExpandedAvatarHeight()
                : dpToPx(AVATAR_HEIGHT_COLLAPSED));

        // Rotate the arrow
        imgExpandArrow.animate()
                .rotation(isExpanded ? 180f : 0f)
                .setDuration(300)
                .start();
    }

    /** Animates the avatar preview from its current height to {@code target} pixels. */
    private void animateAvatarHeightTo(int target) {
        int start = avatarPreviewContainer.getLayoutParams().height;
        if (start == target) {
            return;
        }
        ValueAnimator animator = ValueAnimator.ofInt(start, target);
        animator.setDuration(300);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            ViewGroup.LayoutParams params = avatarPreviewContainer.getLayoutParams();
            params.height = (int) animation.getAnimatedValue();
            avatarPreviewContainer.setLayoutParams(params);
        });
        animator.start();
    }

    /**
     * Works out how tall the avatar can be when lowered: everything on screen except the
     * top bar, the arrow, both tab rows, the colour row (when shown) and the first row of
     * items. Measured at runtime so it suits any screen size.
     */
    private int computeExpandedAvatarHeight() {
        View root = findViewById(R.id.customizeAvatarRoot);
        View topBar = findViewById(R.id.locker_top_bar);
        int collapsed = dpToPx(AVATAR_HEIGHT_COLLAPSED);
        if (root == null || topBar == null || root.getHeight() == 0) {
            return collapsed;
        }
        int fixedDp = PANEL_HANDLE_DP + PANEL_CATEGORY_TABS_DP + PANEL_GRID_PADDING_DP
                + PANEL_FIRST_ROW_DP;
        if (tabSubcategories.getVisibility() == View.VISIBLE) {
            fixedDp += PANEL_SUB_TABS_DP;
        }
        if (skinColorScroll.getVisibility() == View.VISIBLE) {
            fixedDp += PANEL_COLOUR_ROW_DP;
        }
        int available = root.getHeight() - root.getPaddingTop() - root.getPaddingBottom()
                - topBar.getHeight() - dpToPx(fixedDp);
        return Math.max(collapsed, available);
    }

    /** Re-fits the lowered avatar when the tab rows above the grid change. */
    private void refreshExpandedHeight() {
        if (isExpanded) {
            animateAvatarHeightTo(computeExpandedAvatarHeight());
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> confirmDiscardOrFinish());
        btnSave.setOnClickListener(v -> saveEquippedItems());
    }

    // ==================== TAB SWITCHING ====================

    private void onCategoryChanged() {
        switch (currentCategory) {
            case "Body":
                showBodyTab();
                break;
            case "Hair":
                showHairTab();
                break;
            case "Clothing":
                showClothingTab();
                break;
        }
        // The tab rows above the grid differ per category, so re-fit the lowered avatar.
        // Post it so the visibility changes above have been applied first.
        avatarPreviewContainer.post(this::refreshExpandedHeight);
    }

    private void showBodyTab() {
        tabSubcategories.setVisibility(View.GONE);
        skinColorScroll.setVisibility(View.VISIBLE);
        populateSkinColors();
        populateBodyPlaceholders();
    }

    private void showHairTab() {
        tabSubcategories.setVisibility(View.GONE);
        refreshHairColourRow();
        populateGridForType("hair");
    }

    /**
     * Shows the hair-colour picker only when the currently equipped hair supports
     * colour customization; hides it otherwise. Reuses the skin-colour row
     * container since it's the same visual pattern.
     */
    private void refreshHairColourRow() {
        CosmeticItem equippedHair = localEquipped.get("hair");
        boolean colourable = equippedHair != null && equippedHair.isColourable();
        if (colourable) {
            skinColorScroll.setVisibility(View.VISIBLE);
            populateHairColours();
        } else {
            skinColorScroll.setVisibility(View.GONE);
        }
    }

    /**
     * Renders the hair-colour swatches into the shared colour row.
     */
    private void populateHairColours() {
        skinColorRow.removeAllViews();

        float density = getResources().getDisplayMetrics().density;
        int size = (int) (40 * density);
        int margin = (int) (6 * density);

        for (int i = 0; i < HAIR_COLOUR_NAMES.length; i++) {
            View colorView = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(margin, margin, margin, margin);
            colorView.setLayoutParams(params);

            GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            circle.setColor(android.graphics.Color.parseColor(HAIR_COLOUR_SWATCHES[i]));

            if (i == selectedHairColourIndex) {
                circle.setStroke((int) (3 * density),
                        ContextCompat.getColor(this, R.color.light_blue));
            }
            colorView.setBackground(circle);

            final int index = i;
            colorView.setOnClickListener(v -> onHairColourSelected(index));
            skinColorRow.addView(colorView);
        }
    }

    /**
     * Applies a chosen hair colour: updates the avatar model, persists it, and
     * refreshes the preview. Colours without art fall back to grey in the compositor.
     */
    private void onHairColourSelected(int index) {
        selectedHairColourIndex = index;
        hasUnsavedChanges = true;

        String colour = HAIR_COLOUR_NAMES[index];
        if (userAvatar != null) {
            userAvatar.setHairColour(colour);
        }
        // Held locally only - persisted on Save (see saveEquippedItems).
        populateHairColours(); // refresh selection ring
        updateAvatarPreview();
    }

    /**
     * Sets the selected hair-colour index to match the avatar's stored colour.
     */
    private void syncSelectedHairColourFromAvatar() {
        if (userAvatar == null || userAvatar.getHairColour() == null) {
            return;
        }
        String colour = userAvatar.getHairColour().trim().toLowerCase();
        for (int i = 0; i < HAIR_COLOUR_NAMES.length; i++) {
            if (HAIR_COLOUR_NAMES[i].equals(colour)) {
                selectedHairColourIndex = i;
                return;
            }
        }
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
            showEmptyGrid();     // unknown sub-tab: nothing to list
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
        if ("Clothing".equals(currentCategory)) {
            populateClothingTab();
        }
    }

    /**
     * Maps the current Clothing subtab label to its Cosmetic_Item.cosmetic_type.
     * Note the DB uses the SINGULAR "accessory". Returns null for an unknown subtab.
     */
    private String currentClothingType() {
        switch (currentSubcategory) {
            case "Top":     return "top";
            case "Bottom":  return "bottom";
            case "Shoes":   return "shoes";
            default:        return null;
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
        // Default to "no selection" so a brand-new (grey) avatar shows no ring.
        selectedSkinColorIndex = -1;
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
        // Tone is grey/unset or no longer offered - leave nothing selected.
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

        // Held locally only - persisted (with body, hair colour, and outfit) when
        // the user taps Save. Nothing is written to the DB here, so backing out
        // without saving discards the change.
        populateSkinColors(); // refresh selection ring
        populateBodyPlaceholders(); // body thumbnails follow the skin tone
        updateAvatarPreview();
    }

    private void populateBodyPlaceholders() {
        // Body tab shows the skin-tone picker (above) plus a box per body type.
        // Selecting a body type updates the avatar's body_shape.
        gridItems.removeAllViews();

        String currentBody = userAvatar != null && userAvatar.getBodyShape() != null
                ? userAvatar.getBodyShape().trim().toLowerCase() : "";

        String[] bodyTypes = "female".equalsIgnoreCase(userGender)
                ? FEMALE_BODY_TYPES : MALE_BODY_TYPES;
        for (String bodyType : bodyTypes) {
            gridItems.addView(createBodyTypeView(bodyType, bodyType.equals(currentBody)));
        }
    }

    /**
     * Builds one Body-tab cell previewing a body type via the base_{gender}_{bodytype}_{tone}
     * drawable for the current skin tone (grey until a tone is chosen). Tapping it sets
     * the avatar's body_shape.
     */
    private View createBodyTypeView(String bodyType, boolean selected) {
        View view = LayoutInflater.from(this)
                .inflate(R.layout.item_locker_placeholder, gridItems, false);

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
        params.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
        view.setLayoutParams(params);

        View lockOverlay = view.findViewById(R.id.lock_overlay);
        LinearLayout container = view.findViewById(R.id.placeholder_container);
        ImageView imgCheck = view.findViewById(R.id.img_selected_check);
        ImageView imgItem = view.findViewById(R.id.img_placeholder);

        lockOverlay.setVisibility(View.GONE);

        // Preview drawable: the base body for this body type in the current skin tone.
        int resId = AvatarCompositor.getBaseBodyResource(this, userGender, bodyType,
                userAvatar != null ? userAvatar.getSkinTone() : null);
        if (resId != 0) {
            imgItem.clearColorFilter();
            imgItem.setImageResource(resId);
        } else {
            imgItem.setImageResource(R.drawable.ic_launcher_foreground);
        }

        if (selected) {
            container.setBackgroundResource(R.drawable.bg_placeholder_item_selected);
            imgCheck.setVisibility(View.VISIBLE);
        } else {
            container.setBackgroundResource(R.drawable.bg_placeholder_item);
            imgCheck.setVisibility(View.GONE);
        }

        view.setOnClickListener(v -> onBodyTypeSelected(bodyType));
        return view;
    }

    /**
     * Applies a chosen body type: updates the avatar model, persists it, and
     * refreshes the preview and the Body grid highlight.
     */
    private void onBodyTypeSelected(String bodyType) {
        if (userAvatar == null) return;
        userAvatar.setBodyShape(bodyType);
        hasUnsavedChanges = true;

        // Held locally only - persisted on Save (see saveEquippedItems).
        populateBodyPlaceholders(); // refresh selection highlight
        updateAvatarPreview();
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
        // Owned items first, then the rest; each group cheapest to most expensive.
        List<CosmeticItem> sorted = new ArrayList<>(items);
        sorted.sort((a, b) -> {
            boolean ownedA = isOwned(a);
            boolean ownedB = isOwned(b);
            if (ownedA != ownedB) {
                return ownedA ? -1 : 1;
            }
            if (a.getCoinCost() != b.getCoinCost()) {
                return Integer.compare(a.getCoinCost(), b.getCoinCost());
            }
            return Integer.compare(a.getCosmeticId(), b.getCosmeticId());
        });
        for (CosmeticItem item : sorted) {
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

        // Load the item's thumbnail using the same resolver the compositor uses,
        // so the grid shows the exact garment/hair art (with colour), body-type aware.
        loadItemThumbnail(imgItem, item);

        // Placeholder items are non-wearable filler: always shown locked, never
        // equippable, and tapping tells the user they're coming soon.
        if (AvatarCompositor.isPlaceholder(item)) {
            container.setBackgroundResource(R.drawable.bg_placeholder_item);
            imgCheck.setVisibility(View.GONE);
            lockOverlay.setVisibility(View.VISIBLE);
            txtPrice.setText(String.valueOf(item.getCoinCost()));
            view.setOnClickListener(v -> SnackbarUtils.show(this, R.string.locker_item_coming_soon));
            return view;
        }

        // Free items (cost 0) are treated as owned so they can be equipped directly,
        // without a 0-coin "purchase" step. Paid items must be bought first.
        boolean owned = isOwned(item);
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
    private void loadItemThumbnail(ImageView target, CosmeticItem item) {
        // Show a muted placeholder icon while the real art loads (or if it can't be
        // resolved). Once the real item PNG loads we clear the tint so its true
        // colours show through.
        target.setImageTintList(null);
        target.setColorFilter(ContextCompat.getColor(this, R.color.muted));
        target.setImageResource(R.drawable.ic_launcher_foreground);
        if (item == null || item.getType() == null) {
            return;
        }

        final String gender = userGender;
        // Thumbnails always use the AVERAGE body art (chubby/built garments can have
        // arm/hand cut-outs that look odd shrunk into a cell) and the GREY default
        // hair colour, regardless of the avatar's actual body/colour. The item still
        // renders correctly on the real body when equipped.
        final String bodyType = "average";
        final String hairColour = AvatarCompositor.DEFAULT_HAIR_COLOUR;

        new Thread(() -> {
            // Load a tightly-cropped thumbnail so the item is zoomed in and centred
            // in the cell (shoes/hair are small and off-centre on the full canvas).
            Bitmap bmp = AvatarCompositor.loadItemThumbnail(
                    getApplicationContext(), item, gender, bodyType, hairColour);
            if (bmp != null) {
                runOnUiThread(() -> {
                    // Fill the square cell with the cropped (square) item art so
                    // every item reads at roughly the same, clearly visible size.
                    target.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    target.clearColorFilter();
                    target.setImageBitmap(bmp);
                });
            }
        }).start();
    }

    /** Free items count as owned; paid items are owned once bought. */
    private boolean isOwned(CosmeticItem item) {
        return item.getCoinCost() <= 0 || ownedCosmeticIds.contains(item.getCosmeticId());
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
        // Equipping/removing hair changes whether the colour picker applies.
        if ("hair".equals(type) && "Hair".equals(currentCategory)) {
            refreshHairColourRow();
        }
        updateAvatarPreview();
    }

    /**
     * Shows the purchase dialog for a locked item, backed by a real DB purchase.
     */
    private void showPurchaseDialog(CosmeticItem item) {
        PurchaseDialog dialog = PurchaseDialog.newInstance(item, coinBalance,
                userGender, purchasedItem -> onPurchaseConfirmed(purchasedItem));
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
                            SnackbarUtils.show(CustomizeAvatarActivity.this,
                                    getString(R.string.locker_item_purchased, item.getName()));
                            refreshCurrentGrid();
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        boolean noCoins = e.getMessage() != null
                                && e.getMessage().contains("Insufficient");
                        runOnUiThread(() -> DialogUtils.showError(CustomizeAvatarActivity.this,
                                getString(noCoins ? R.string.locker_not_enough_coins
                                                  : R.string.locker_purchase_error)));
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
            public void onError(Exception e) {
                Log.e(TAG, "loadOwnedCosmetics: database call failed", e);
                /* grid still renders, all locked */
            }
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
                    if ("Hair".equals(currentCategory)) {
                        refreshHairColourRow();
                    }
                    updateAvatarPreview();
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "loadEquippedItems: database call failed", e);
                /* preview shows body only */
            }
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
            public void onError(Exception e) {
                Log.e(TAG, "loadGender: database call failed", e);
                /* keep default gender */
            }
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
                syncSelectedHairColourFromAvatar();
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
        final String hairColour = userAvatar.getHairColour();

        // Compositing decodes assets from disk, so do it off the UI thread.
        new Thread(() -> {
            Bitmap full = AvatarCompositor.composite(
                    getApplicationContext(), gender, bodyType, skinTone, hairColour, equipped);
            // Crop to the figure so it fills the preview with the feet at the bottom edge.
            android.graphics.Rect box = figureBounds(gender, bodyType, skinTone, hairColour);
            Bitmap avatarBitmap = Bitmap.createBitmap(full, box.left, box.top,
                    box.width(), box.height());
            runOnUiThread(() -> imgAvatarPreview.setImageBitmap(avatarBitmap));
        }).start();
    }

    private String figureBoundsKey;
    private android.graphics.Rect figureBoundsValue;

    /**
     * The crop box for the preview. It is worked out from the bare body (no clothes), plus
     * some room above for hair, so the avatar does not change size as items are swapped.
     */
    private synchronized android.graphics.Rect figureBounds(String gender, String bodyType,
                                                            String skinTone, String hairColour) {
        String key = gender + "|" + bodyType + "|" + skinTone;
        if (figureBoundsValue != null && key.equals(figureBoundsKey)) {
            return figureBoundsValue;
        }
        Bitmap bare = AvatarCompositor.composite(getApplicationContext(), gender, bodyType,
                skinTone, hairColour, new ArrayList<>());
        android.graphics.Rect r = AvatarCompositor.opaqueBounds(bare);
        int size = bare.getWidth();
        int pad = Math.round(size * 0.02f);
        int hairRoom = Math.round(size * 0.06f);
        // Keep the box square-ish wide enough for wide outfits (hoodies, jackets).
        int left = Math.max(0, r.left - Math.round(size * 0.08f));
        int right = Math.min(size, r.right + Math.round(size * 0.08f));
        int top = Math.max(0, r.top - hairRoom);
        int bottom = Math.min(bare.getHeight(), r.bottom + pad);
        figureBoundsKey = key;
        figureBoundsValue = new android.graphics.Rect(left, top, right, bottom);
        return figureBoundsValue;
    }

    // ==================== SAVE / DISCARD ====================

    private void saveEquippedItems() {
        // An avatar must be wearing at least a top and a bottom - no shirtless or
        // pantless avatars allowed.
        if (!localEquipped.containsKey("top") || !localEquipped.containsKey("bottom")) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.locker_invalid_outfit_title)
                    .setMessage(R.string.locker_top_bottom_required)
                    .setPositiveButton(R.string.locker_invalid_outfit_ok, (d, w) -> d.dismiss())
                    .show();
            return;
        }

        // Stop a second tap while saving; re-enabled below if the save fails.
        btnSave.setEnabled(false);

        // Persist the avatar body attributes (skin tone, body shape, hair colour)
        // together with the outfit. These were held locally until now so that
        // backing out without saving discards them. "Outfit saved!" is only shown
        // once every one of these writes has finished without an error.
        final String skinTone = userAvatar != null ? userAvatar.getSkinTone() : null;
        final String bodyShape = userAvatar != null ? userAvatar.getBodyShape() : null;
        final String hairColour = userAvatar != null ? userAvatar.getHairColour() : null;

        int writes = 1 + (skinTone != null ? 1 : 0) + (bodyShape != null ? 1 : 0)
                + (hairColour != null ? 1 : 0);
        final AtomicInteger pending = new AtomicInteger(writes);
        final AtomicBoolean failed = new AtomicBoolean(false);
        final DatabaseCallback<Integer> step = new DatabaseCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                stepDone();
            }

            @Override
            public void onError(Exception e) {
                failed.set(true);
                stepDone();
            }

            private void stepDone() {
                if (pending.decrementAndGet() == 0) {
                    runOnUiThread(() -> finishSave(!failed.get()));
                }
            }
        };

        if (skinTone != null) {
            cosmeticRepository.updateSkinTone(userId, skinTone, step);
        }
        if (bodyShape != null) {
            cosmeticRepository.updateBodyShape(userId, bodyShape, step);
        }
        if (hairColour != null) {
            cosmeticRepository.updateHairColour(userId, hairColour, step);
        }
        cosmeticRepository.saveAllEquipped(userId, new ArrayList<>(localEquipped.values()), step);
    }

    /** Runs on the UI thread once every save write has finished. */
    private void finishSave(boolean ok) {
        // Even a partial save may have changed the avatar, so let the Me tab redraw it.
        AvatarState.markChanged();
        if (ok) {
            hasUnsavedChanges = false;
            SnackbarUtils.show(this, getString(R.string.locker_outfit_saved));
            findViewById(android.R.id.content).postDelayed(this::finish, 1500);
        } else {
            btnSave.setEnabled(true);
            DialogUtils.showError(this, getString(R.string.locker_save_error));
        }
    }

    /**
     * Handles a back/exit request: if there are unsaved avatar changes, confirm
     * before discarding; otherwise finish immediately. Nothing is written to the
     * DB until Save, so discarding simply exits without persisting.
     */
    private void confirmDiscardOrFinish() {
        if (!hasUnsavedChanges) {
            finish();
            return;
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.locker_discard_title)
                .setMessage(R.string.locker_discard_message)
                .setPositiveButton(R.string.locker_discard_confirm, (d, w) -> discardAndFinish())
                .setNegativeButton(R.string.locker_discard_cancel, (d, w) -> d.dismiss())
                .show();
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
