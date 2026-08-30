package com.jve.repsup.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.jve.repsup.MainActivity;
import com.jve.repsup.R;
import com.jve.repsup.database.ConnectionManager;
import com.jve.repsup.utils.AvatarCompositor;
import com.jve.repsup.utils.SessionManager;

/**
 * Displays the "MEET YOUR AVATAR!" screen after assessment completion.
 * Creates the avatar record, links default cosmetics, and composites the avatar image.
 */
public class AvatarGenerationActivity extends AppCompatActivity {

    private static final String TAG = "AvatarGenerationActivity";

    public static final String EXTRA_GENDER = "extra_gender";
    public static final String EXTRA_BODY_TYPE = "extra_body_type";

    // Default cosmetic IDs from seed data. New users start wearing a tank, shorts,
    // socks (no shoes), and the default hair. Shoes are unlocked separately.
    private static final int DEFAULT_TOP_ID = 1;      // Basic White Tank
    private static final int DEFAULT_BOTTOM_ID = 2;   // Basic Black Shorts
    private static final int DEFAULT_SOCKS_ID = 4;    // Basic Socks
    private static final int DEFAULT_HAIR_ID = 5;     // Default Hair

    // Grey skin tone stored until the user picks a real tone in the locker.
    private static final String DEFAULT_SKIN_TONE = "grey";

    private ImageView ivAvatar;
    private Button btnContinue;
    private ImageButton btnBack;
    private TextView tvAvatarSubheading;

    private String gender;
    private String bodyType;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatar_generation);

        ivAvatar = findViewById(R.id.ivAvatar);
        btnContinue = findViewById(R.id.btnContinue);
        btnBack = findViewById(R.id.btnBack);
        tvAvatarSubheading = findViewById(R.id.tvAvatarSubheading);

        // Get data from intent
        gender = getIntent().getStringExtra(EXTRA_GENDER);
        bodyType = getIntent().getStringExtra(EXTRA_BODY_TYPE);

        SessionManager sessionManager = new SessionManager(this);
        userId = sessionManager.getUserId();

        // Set gender-aware subheading
        if (gender != null && gender.equalsIgnoreCase("female")) {
            tvAvatarSubheading.setText(R.string.avatar_subtitle_female);
        } else {
            tvAvatarSubheading.setText(R.string.avatar_subtitle_male);
        }

        // Disable continue until setup is done
        btnContinue.setEnabled(false);

        btnContinue.setOnClickListener(v -> navigateToMain());
        btnBack.setOnClickListener(v -> onBackPressed());

        // Start avatar creation process
        createAvatarRecords();
    }

    /**
     * Inserts the Avatar record, then chains to insert User_Cosmetic records,
     * Outfit_Slot record, and updates User.avatar_id.
     */
    private void createAvatarRecords() {
        String insertAvatarSql = "INSERT INTO Avatar (user_id, skin_tone, body_shape, facial_details) "
                + "VALUES (?, ?, ?, ?)";

        // Skin tone starts grey; the user chooses a real tone in the locker body tab.
        // Body shape comes from the assessment body type.
        Object[] avatarParams = new Object[]{
                userId,
                DEFAULT_SKIN_TONE,
                bodyType != null ? bodyType : "average",
                null // no facial details by default
        };

        ConnectionManager.executeInsert(insertAvatarSql, avatarParams, new ConnectionManager.DatabaseCallback<Long>() {
            @Override
            public void onSuccess(Long avatarId) {
                Log.d(TAG, "Avatar created with ID: " + avatarId);
                // Chain: insert default cosmetics
                insertDefaultCosmetics(avatarId.intValue());
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Failed to create Avatar record", e);
                runOnUiThread(() -> {
                    // Still show the avatar visually even if DB fails
                    displayAvatar();
                    btnContinue.setEnabled(true);
                    Toast.makeText(AvatarGenerationActivity.this,
                            getString(R.string.avatar_save_error), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /**
     * Inserts User_Cosmetic records for the default outfit items (top, bottom,
     * socks, hair). All are marked unlocked and equipped. No shoes by default -
     * the user wears socks only until they choose shoes in the locker.
     */
    private void insertDefaultCosmetics(int avatarId) {
        int[] defaultIds = {DEFAULT_TOP_ID, DEFAULT_BOTTOM_ID, DEFAULT_SOCKS_ID, DEFAULT_HAIR_ID};
        insertDefaultCosmetic(avatarId, defaultIds, 0);
    }

    /**
     * Inserts the default cosmetics one at a time (sequential, index-driven),
     * then chains to the outfit slot once all are inserted. A single failure
     * still proceeds to completion so the avatar renders.
     */
    private void insertDefaultCosmetic(int avatarId, int[] cosmeticIds, int index) {
        if (index >= cosmeticIds.length) {
            insertOutfitSlot(avatarId);
            return;
        }

        String insertCosmeticSql = "INSERT INTO User_Cosmetic (user_id, cosmetic_id, is_unlocked, is_equipped) "
                + "VALUES (?, ?, ?, ?)";
        Object[] params = new Object[]{userId, cosmeticIds[index], true, true};

        ConnectionManager.executeInsert(insertCosmeticSql, params, new ConnectionManager.DatabaseCallback<Long>() {
            @Override
            public void onSuccess(Long result) {
                insertDefaultCosmetic(avatarId, cosmeticIds, index + 1);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Failed to insert default cosmetic id=" + cosmeticIds[index], e);
                // Keep going so the remaining defaults and outfit slot still get set.
                insertDefaultCosmetic(avatarId, cosmeticIds, index + 1);
            }
        });
    }

    /**
     * Inserts the Outfit_Slot record linking default cosmetics as equipped.
     * Socks slot is filled; shoes slot is left empty by default.
     */
    private void insertOutfitSlot(int avatarId) {
        String insertOutfitSql = "INSERT INTO Outfit_Slot (user_id, top_id, bottom_id, shoes_id, socks_id, accessories_id) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        Object[] outfitParams = new Object[]{
                userId, DEFAULT_TOP_ID, DEFAULT_BOTTOM_ID, null, DEFAULT_SOCKS_ID, null
        };

        ConnectionManager.executeInsert(insertOutfitSql, outfitParams, new ConnectionManager.DatabaseCallback<Long>() {
            @Override
            public void onSuccess(Long result) {
                Log.d(TAG, "Outfit slot created");
                // Chain: update User.avatar_id
                updateUserAvatarId(avatarId);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Failed to create Outfit_Slot", e);
                handleDbCompletion(avatarId);
            }
        });
    }

    /**
     * Updates the User record to link the newly created avatar.
     */
    private void updateUserAvatarId(int avatarId) {
        String updateUserSql = "UPDATE User SET avatar_id = ? WHERE user_id = ?";
        Object[] updateParams = new Object[]{avatarId, userId};

        ConnectionManager.executeUpdate(updateUserSql, updateParams, new ConnectionManager.DatabaseCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                Log.d(TAG, "User.avatar_id updated");
                handleDbCompletion(avatarId);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Failed to update User.avatar_id", e);
                handleDbCompletion(avatarId);
            }
        });
    }

    /**
     * Called after all DB operations complete (success or partial failure).
     * Displays the composited avatar and enables the Continue button.
     */
    private void handleDbCompletion(int avatarId) {
        runOnUiThread(() -> {
            displayAvatar();
            btnContinue.setEnabled(true);
        });
    }

    /**
     * Displays the initial full-body avatar image based on gender and body type.
     * Uses a single pre-rendered drawable instead of the layer compositor.
     */
    private void displayAvatar() {
        int avatarRes = AvatarCompositor.getInitialAvatarResource(gender, bodyType);
        ivAvatar.setImageResource(avatarRes);
    }

    /**
     * Navigates to MainActivity and clears the back stack.
     */
    private void navigateToMain() {
        Intent intent = new Intent(AvatarGenerationActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
