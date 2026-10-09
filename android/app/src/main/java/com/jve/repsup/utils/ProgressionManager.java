package com.jve.repsup.utils;

import com.jve.repsup.database.ConnectionManager;
import com.jve.repsup.models.Rank;
import com.jve.repsup.models.User;
import com.jve.repsup.repositories.UserRepository;

/**
 * Single place that applies levelling and reward logic after any XP change.
 *
 * <p>Call {@link #applyProgression(int, UserRepository, ProgressionCallback)}
 * once the user's XP has already been written to the database. It:
 * <ul>
 *     <li>reads the user's latest XP, level and rank,</li>
 *     <li>works out the new level from the XP curve
 *     ({@link XPCalculator#levelForTotalXp(long)}),</li>
 *     <li>saves the new level when it changed,</li>
 *     <li>awards 50 coins per level gained and 100 coins per rank gained,</li>
 *     <li>promotes the rank when a higher one has become reachable,</li>
 *     <li>and reports back what happened through a {@link ProgressionResult} so
 *     the screen can show the Level Up / Rank Up dialogs.</li>
 * </ul>
 *
 * This replaces the level/coin/rank logic that was copied into
 * WorkoutCompleteActivity and GoalsListFragment.
 *
 * <p>The callback is delivered on a background thread (the repository threads),
 * so callers must hop to the UI thread before touching any views.
 */
public final class ProgressionManager {

    /** Coins awarded for each level a user gains. */
    public static final int COINS_PER_LEVEL = 50;
    /** Coins awarded for each rank a user gains. */
    public static final int COINS_PER_RANK = 100;

    private ProgressionManager() {
        // Utility class — no instances.
    }

    /**
     * The outcome of applying progression after an XP change: what the user's
     * level and rank became, how many of each they gained, and the coins
     * awarded. A screen uses this to decide which dialogs to show.
     */
    public static final class ProgressionResult {
        private final int oldLevel;
        private final int newLevel;
        private final boolean rankedUp;
        private final String newRankName;
        private final int coinsAwarded;

        public ProgressionResult(int oldLevel, int newLevel, boolean rankedUp,
                                 String newRankName, int coinsAwarded) {
            this.oldLevel = oldLevel;
            this.newLevel = newLevel;
            this.rankedUp = rankedUp;
            this.newRankName = newRankName;
            this.coinsAwarded = coinsAwarded;
        }

        /** The user's level before this XP change was applied. */
        public int getOldLevel() { return oldLevel; }

        /** The user's level after this XP change was applied. */
        public int getNewLevel() { return newLevel; }

        /** Number of levels gained (0 if none). */
        public int getLevelsGained() { return Math.max(0, newLevel - oldLevel); }

        /** True if the user levelled up at all. */
        public boolean leveledUp() { return newLevel > oldLevel; }

        /** True if the user also moved up to a new rank. */
        public boolean rankedUp() { return rankedUp; }

        /** The display name of the new rank, or null if the rank did not change. */
        public String getNewRankName() { return newRankName; }

        /** Total coins awarded for the levels and rank gained. */
        public int getCoinsAwarded() { return coinsAwarded; }
    }

    /** Reports the result of applying progression, or an error. */
    public interface ProgressionCallback {
        void onResult(ProgressionResult result);
        void onError(Exception e);
    }

    /**
     * Applies levelling and rewards after the user's XP has already changed in
     * the database. See the class docs for the full sequence.
     *
     * @param userId         the user whose XP just changed
     * @param userRepository repository used for all reads and writes
     * @param callback       receives the {@link ProgressionResult} (on a
     *                       background thread) or an error
     */
    public static void applyProgression(int userId, UserRepository userRepository,
                                        ProgressionCallback callback) {
        userRepository.getUserById(userId, new ConnectionManager.DatabaseCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user == null) {
                    callback.onError(new IllegalStateException("User " + userId + " not found"));
                    return;
                }

                int oldLevel = user.getCurrentLevel();
                // Use long XP so the curve maths never overflows. Never lower a
                // level: take the higher of the stored level and the curve's
                // level for the current XP.
                int newLevel = Math.max(oldLevel, XPCalculator.levelForTotalXp(user.getXp()));

                int levelsGained = Math.max(0, newLevel - oldLevel);
                int coinsAwarded = levelsGained * COINS_PER_LEVEL;

                if (newLevel != oldLevel) {
                    userRepository.updateLevel(userId, newLevel, null);
                }

                // Only a level gain can unlock a new rank, so look for a
                // promotion only when the user actually levelled up.
                if (levelsGained > 0) {
                    checkRankPromotion(userId, oldLevel, newLevel, coinsAwarded,
                            user.getRankId() != null ? user.getRankId() : 0,
                            userRepository, callback);
                } else {
                    callback.onResult(new ProgressionResult(
                            oldLevel, newLevel, false, null, coinsAwarded));
                }
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    /**
     * Looks for a rank the user now qualifies for and, if found, saves it and
     * adds the rank bonus to the coins awarded. Reports the final result either
     * way.
     */
    private static void checkRankPromotion(int userId, int oldLevel, int newLevel,
                                           int levelCoins, int currentRankId,
                                           UserRepository userRepository,
                                           ProgressionCallback callback) {
        userRepository.getNextRank(newLevel, currentRankId, new ConnectionManager.DatabaseCallback<Rank>() {
            @Override
            public void onSuccess(Rank nextRank) {
                if (nextRank != null) {
                    userRepository.updateRank(userId, nextRank.getRankId(), null);
                    int totalCoins = levelCoins + COINS_PER_RANK;
                    userRepository.updateCoins(userId, totalCoins, null);
                    callback.onResult(new ProgressionResult(
                            oldLevel, newLevel, true, nextRank.getDisplayName(), totalCoins));
                } else {
                    if (levelCoins > 0) {
                        userRepository.updateCoins(userId, levelCoins, null);
                    }
                    callback.onResult(new ProgressionResult(
                            oldLevel, newLevel, false, null, levelCoins));
                }
            }

            @Override
            public void onError(Exception e) {
                // Rank lookup failed: still award the level coins and report the
                // level gain so the user isn't short-changed.
                if (levelCoins > 0) {
                    userRepository.updateCoins(userId, levelCoins, null);
                }
                callback.onResult(new ProgressionResult(
                        oldLevel, newLevel, false, null, levelCoins));
            }
        });
    }
}
