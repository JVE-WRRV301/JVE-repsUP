package com.jve.repsup;

import com.jve.repsup.utils.RegistrationValidator;
import com.jve.repsup.utils.RegistrationValidator.ValidationResult;

import net.jqwik.api.*;
import net.jqwik.api.constraints.NotBlank;

import java.util.HashSet;
import java.util.Set;

/**
 * Property-based tests for RegistrationValidator using jqwik.
 *
 * **Validates: Requirements 3.2, 3.3, 3.4**
 *
 * Property 9: Registration Validation Rejects Invalid Inputs
 * For any registration attempt where username is taken OR password != confirm
 * OR any required field is empty, the system rejects and no User record is inserted.
 */
class RegistrationValidatorPropertyTest {

    // ─── Property 9a: Any input with at least one empty required field is rejected ───

    /**
     * **Validates: Requirements 3.4**
     *
     * For any registration input where at least one required field is empty (null or blank),
     * validation must reject with the "fields required" error.
     */
    @Property(tries = 100)
    void emptyRequiredFieldAlwaysRejectsRegistration(
            @ForAll("inputWithEmptyField") RegistrationInput input) {

        ValidationResult result = RegistrationValidator.validate(
                input.name, input.username, input.email,
                input.password, input.confirmPassword,
                username -> false // username not taken
        );

        Assertions.assertThat(result.isValid()).isFalse();
        Assertions.assertThat(result.getErrorReason())
                .isEqualTo(RegistrationValidator.ERROR_FIELDS_REQUIRED);
    }

    // ─── Property 9b: Any input where password != confirmPassword is rejected ───

    /**
     * **Validates: Requirements 3.3**
     *
     * For any registration input where all fields are non-empty but password != confirmPassword,
     * validation must reject with the "passwords do not match" error.
     */
    @Property(tries = 100)
    void passwordMismatchAlwaysRejectsRegistration(
            @ForAll("inputWithPasswordMismatch") RegistrationInput input) {

        ValidationResult result = RegistrationValidator.validate(
                input.name, input.username, input.email,
                input.password, input.confirmPassword,
                username -> false // username not taken
        );

        Assertions.assertThat(result.isValid()).isFalse();
        Assertions.assertThat(result.getErrorReason())
                .isEqualTo(RegistrationValidator.ERROR_PASSWORD_MISMATCH);
    }

    // ─── Property 9c: Any input where username is taken is rejected ───

    /**
     * **Validates: Requirements 3.2**
     *
     * For any registration input where all fields are valid and passwords match,
     * but the username is already taken, validation must reject with the "username taken" error.
     */
    @Property(tries = 100)
    void takenUsernameAlwaysRejectsRegistration(
            @ForAll("validInputWithTakenUsername") RegistrationInput input) {

        Set<String> takenUsernames = new HashSet<>();
        takenUsernames.add(input.username);

        ValidationResult result = RegistrationValidator.validate(
                input.name, input.username, input.email,
                input.password, input.confirmPassword,
                takenUsernames::contains
        );

        Assertions.assertThat(result.isValid()).isFalse();
        Assertions.assertThat(result.getErrorReason())
                .isEqualTo(RegistrationValidator.ERROR_USERNAME_TAKEN);
    }

    // ─── Generators ───

    @Provide
    Arbitrary<RegistrationInput> inputWithEmptyField() {
        Arbitrary<String> nonEmpty = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20);
        Arbitrary<String> emptyField = Arbitraries.of("", "   ", null);

        // Generate all 5 fields, then randomly blank out at least one
        return Combinators.combine(
                nonEmpty, nonEmpty, nonEmpty, nonEmpty, nonEmpty,
                Arbitraries.integers().between(0, 4)
        ).as((name, username, email, password, confirm, fieldToBlank) -> {
            String blankValue = pickBlank();
            switch (fieldToBlank) {
                case 0: return new RegistrationInput(blankValue, username, email, password, confirm);
                case 1: return new RegistrationInput(name, blankValue, email, password, confirm);
                case 2: return new RegistrationInput(name, username, blankValue, password, confirm);
                case 3: return new RegistrationInput(name, username, email, blankValue, confirm);
                default: return new RegistrationInput(name, username, email, password, blankValue);
            }
        });
    }

    @Provide
    Arbitrary<RegistrationInput> inputWithPasswordMismatch() {
        Arbitrary<String> nonEmpty = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20);

        return Combinators.combine(nonEmpty, nonEmpty, nonEmpty, nonEmpty, nonEmpty)
                .filter((name, username, email, password, confirm) -> !password.equals(confirm))
                .as((name, username, email, password, confirm) ->
                        new RegistrationInput(name, username, email, password, confirm));
    }

    @Provide
    Arbitrary<RegistrationInput> validInputWithTakenUsername() {
        Arbitrary<String> nonEmpty = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20);

        return Combinators.combine(nonEmpty, nonEmpty, nonEmpty, nonEmpty)
                .as((name, username, email, password) ->
                        new RegistrationInput(name, username, email, password, password));
    }

    // ─── Helpers ───

    private String pickBlank() {
        int choice = (int) (Math.random() * 3);
        switch (choice) {
            case 0: return "";
            case 1: return "   ";
            default: return null;
        }
    }

    /**
     * Simple data holder for registration input fields used by generators.
     */
    static class RegistrationInput {
        final String name;
        final String username;
        final String email;
        final String password;
        final String confirmPassword;

        RegistrationInput(String name, String username, String email,
                         String password, String confirmPassword) {
            this.name = name;
            this.username = username;
            this.email = email;
            this.password = password;
            this.confirmPassword = confirmPassword;
        }

        @Override
        public String toString() {
            return String.format(
                    "RegistrationInput{name='%s', username='%s', email='%s', password='%s', confirmPassword='%s'}",
                    name, username, email, password, confirmPassword);
        }
    }

    /**
     * Simple assertion helper to avoid dependency on external assertion libraries.
     */
    static class Assertions {
        static BooleanAssertion assertThat(boolean value) {
            return new BooleanAssertion(value);
        }

        static StringAssertion assertThat(String value) {
            return new StringAssertion(value);
        }

        static class BooleanAssertion {
            private final boolean actual;

            BooleanAssertion(boolean actual) {
                this.actual = actual;
            }

            void isFalse() {
                if (actual) {
                    throw new AssertionError("Expected false but was true");
                }
            }

            void isTrue() {
                if (!actual) {
                    throw new AssertionError("Expected true but was false");
                }
            }
        }

        static class StringAssertion {
            private final String actual;

            StringAssertion(String actual) {
                this.actual = actual;
            }

            void isEqualTo(String expected) {
                if (actual == null && expected == null) return;
                if (actual == null || !actual.equals(expected)) {
                    throw new AssertionError(
                            "Expected '" + expected + "' but was '" + actual + "'");
                }
            }
        }
    }
}
