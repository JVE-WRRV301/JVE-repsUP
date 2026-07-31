package com.jve.repsup.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.jve.repsup.MainActivity;
import com.jve.repsup.R;
import com.jve.repsup.database.ConnectionManager;
import com.jve.repsup.models.Avatar;
import com.jve.repsup.models.OutfitSlot;
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

    // Default cosmetic IDs from seed data
    private static final int DEFAULT_TOP_ID = 1;      // Basic White Tank
    private static final int DEFAULT_BOTTOM_ID = 2;   // Basic Black Shorts
    private static final int DEFAULT_SHOES_ID = 3;    // White Sneakers

    private ImageView ivAvatar;
    private Button btnContinue;

    private String gender;
    private String bodyType;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatar_generation);

        ivAvatar = findViewById(R.id.ivAvatar);
        btnContinue = findViewById(R.id.btnContinue);

        // Get data from intent
        gender = getIntent().getStringExtra(EXTRA_GENDER);
        bodyType = getIntent().getStringExtra(EXTRA_BODY_TYPE);

        SessionManager sessionManager = new SessionManager(this);
        userId = sessionManager.getUserId();

        // Disable continue until setup is done
        btnContinue.setEnabled(false);

        btnContinue.setOnClickListener(v -> navigateToMain());

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

        // Use gender as skin_tone placeholder (will be refined later with actual skin tone selection)
        Object[] avatarParams = new Object[]{
                userId,
                gender != null ? gender : "male",
                bodyType != null ? bodyType : "mesomorph",
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
     * Inserts 3 User_Cosmetic records for the default outfit items (top, bottom, shoes).
     * All are marked as unlocked and equipped.
     */
    private void insertDefaultCosmetics(int avatarId) {
        String insertCosmeticSql = "INSERT INTO User_Cosmetic (user_id, cosmetic_id, is_unlocked, is_equipped) "
                + "VALUES (?, ?, ?, ?)";

        // Insert top (cosmetic_id=1)
        Object[] topParams = new Object[]{userId, DEFAULT_TOP_ID, true, true};
        ConnectionManager.executeInsert(insertCosmeticSql, topParams, new ConnectionManager.DatabaseCallback<Long>() {
            @Override
            public void onSuccess(Long result) {
                // Insert bottom (cosmetic_id=2)
                Object[] bottomParams = new Object[]{userId, DEFAULT_BOTTOM_ID, true, true};
                ConnectionManager.executeInsert(insertCosmeticSql, bottomParams, new ConnectionManager.DatabaseCallback<Long>() {
                    @Override
                    public void onSuccess(Long result) {
                        // Insert shoes (cosmetic_id=3)
                        Object[] shoesParams = new Object[]{userId, DEFAULT_SHOES_ID, true, true};
                        ConnectionManager.executeInsert(insertCosmeticSql, shoesParams, new ConnectionManager.DatabaseCallback<Long>() {
                            @Override
                            public void onSuccess(Long result) {
                                // Chain: create outfit slot
                                insertOutfitSlot(avatarId);
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e(TAG, "Failed to insert shoes cosmetic", e);
                                handleDbCompletion(avatarId);
                            }
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Failed to insert bottom cosmetic", e);
                        handleDbCompletion(avatarId);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Failed to insert top cosmetic", e);
                handleDbCompletion(avatarId);
            }
        });
    }

    /**
     * Inserts the Outfit_Slot record linking default cosmetics as equipped.
     */
    private void insertOutfitSlot(int avatarId) {
        String insertOutfitSql = "INSERT INTO Outfit_Slot (user_id, top_id, bottom_id, shoes_id, accessories_id) "
                + "VALUES (?, ?, ?, ?, ?)";

        Object[] outfitParams = new Object[]{
                userId, DEFAULT_TOP_ID, DEFAULT_BOTTOM_ID, DEFAULT_SHOES_ID, null
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
     * Composites and displays the avatar using AvatarCompositor.
     */
    private void displayAvatar() {
        // Build model objects for compositing
        Avatar avatar = new Avatar();
        avatar.setUserId(userId);
        avatar.setSkinTone(gender != null ? gender : "male");
        avatar.setBodyShape(bodyType != null ? bodyType : "mesomorph");

        OutfitSlot outfit = new OutfitSlot();
        outfit.setUserId(userId);
        outfit.setTopId(DEFAULT_TOP_ID);
        outfit.setBottomId(DEFAULT_BOTTOM_ID);
        outfit.setShoesId(DEFAULT_SHOES_ID);

        Bitmap avatarBitmap = AvatarCompositor.composite(this, avatar, outfit);
        ivAvatar.setImageBitmap(avatarBitmap);
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
