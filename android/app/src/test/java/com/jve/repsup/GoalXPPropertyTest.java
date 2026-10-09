package com.jve.repsup;

import com.jve.repsup.utils.XPCalculator;

import net.jqwik.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for Goal XP calculation logic.
 * Uses jqwik to generate arbitrary priority strings and verify the XP mapping.
 *
 * Validates: Requirements 8.5, 17.2
 */
class GoalXPPropertyTest {

    // ─── Property 2: Goal XP Matches Priority Mapping ───

    /**
     * Property 2a: "Top" priority always yields exactly 1000 XP.
     *
     * Validates: Requirements 8.5, 17.2
     */
    @Example
    void topPriorityAlwaysReturns1000XP() {
        int xp = XPCalculator.calculateGoalXP("Top");
        assertEquals(1000, xp, "Top priority must yield exactly 1000 XP");
    }

    /**
     * Review item 10: priority matching ignores case, so the lowercase values
     * the app/seed actually save ("top"/"medium"/"low") map to the same XP as
     * their capitalised forms.
     */
    @Example
    void priorityMatchingIgnoresCase() {
        assertEquals(1000, XPCalculator.calculateGoalXP("top"), "'top' must yield 1000 XP");
        assertEquals(500, XPCalculator.calculateGoalXP("MEDIUM"), "'MEDIUM' must yield 500 XP");
        assertEquals(250, XPCalculator.calculateGoalXP("  Low  "), "' Low ' must yield 250 XP");
    }

    /**
     * Property 2b: "Medium" priority always yields exactly 500 XP.
     *
     * Validates: Requirements 8.5, 17.2
     */
    @Example
    void mediumPriorityAlwaysReturns500XP() {
        int xp = XPCalculator.calculateGoalXP("Medium");
        assertEquals(500, xp, "Medium priority must yield exactly 500 XP");
    }

    /**
     * Property 2c: "Low" priority always yields exactly 250 XP.
     *
     * Validates: Requirements 8.5, 17.2
     */
    @Example
    void lowPriorityAlwaysReturns250XP() {
        int xp = XPCalculator.calculateGoalXP("Low");
        assertEquals(250, xp, "Low priority must yield exactly 250 XP");
    }

    /**
     * Property 2d: Any arbitrary string that is NOT a valid priority returns 0 XP.
     * Uses jqwik's Arbitraries.strings() to generate random strings and filters out valid priorities.
     *
     * Validates: Requirements 8.5, 17.2
     */
    @Property(tries = 200)
    void invalidPriorityAlwaysReturns0XP(
            @ForAll("invalidPriorityStrings") String priority
    ) {
        int xp = XPCalculator.calculateGoalXP(priority);
        assertEquals(0, xp,
                "Invalid priority '" + priority + "' must yield 0 XP");
    }

    /**
     * Property 2e: Null input is treated as Medium (review item 10), so it yields
     * 500 XP rather than 0.
     *
     * Validates: Requirements 8.5, 17.2
     */
    @Example
    void nullPriorityIsTreatedAsMedium() {
        int xp = XPCalculator.calculateGoalXP(null);
        assertEquals(500, xp, "Null priority must be treated as Medium (500 XP)");
    }

    /**
     * Property 2f: For any valid priority from the set {Top, Medium, Low},
     * the XP is exactly 1000, 500, or 250 respectively.
     * Uses jqwik to select randomly from the valid priority set.
     *
     * Validates: Requirements 8.5, 17.2
     */
    @Property(tries = 200)
    void validPriorityAlwaysMapsToCorrectXP(
            @ForAll("validPriorities") String priority
    ) {
        int xp = XPCalculator.calculateGoalXP(priority);

        switch (priority) {
            case "Top":
                assertEquals(1000, xp, "Top must yield 1000 XP");
                break;
            case "Medium":
                assertEquals(500, xp, "Medium must yield 500 XP");
                break;
            case "Low":
                assertEquals(250, xp, "Low must yield 250 XP");
                break;
            default:
                fail("Unexpected priority: " + priority);
        }
    }

    // ─── Custom Providers ───

    @Provide
    Arbitrary<String> invalidPriorityStrings() {
        // A string is "invalid" (0 XP) only when its trimmed, lower-cased form is
        // not one of the three priorities, since matching is now case-insensitive
        // and trims whitespace (review item 10).
        return Arbitraries.strings()
                .ofMinLength(0)
                .ofMaxLength(50)
                .filter(s -> {
                    String n = s.trim().toLowerCase();
                    return !n.equals("top") && !n.equals("medium") && !n.equals("low");
                });
    }

    @Provide
    Arbitrary<String> validPriorities() {
        return Arbitraries.of("Top", "Medium", "Low");
    }
}
