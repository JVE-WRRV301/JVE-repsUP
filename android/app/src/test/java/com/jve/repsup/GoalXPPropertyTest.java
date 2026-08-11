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
     * Property 2e: Null input always returns 0 XP.
     *
     * Validates: Requirements 8.5, 17.2
     */
    @Example
    void nullPriorityAlwaysReturns0XP() {
        int xp = XPCalculator.calculateGoalXP(null);
        assertEquals(0, xp, "Null priority must yield 0 XP");
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
        return Arbitraries.strings()
                .ofMinLength(0)
                .ofMaxLength(50)
                .filter(s -> !s.equals("Top") && !s.equals("Medium") && !s.equals("Low"));
    }

    @Provide
    Arbitrary<String> validPriorities() {
        return Arbitraries.of("Top", "Medium", "Low");
    }
}
