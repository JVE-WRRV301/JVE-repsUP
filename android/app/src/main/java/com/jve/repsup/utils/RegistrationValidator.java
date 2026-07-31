package com.jve.repsup.utils;

import java.util.function.Predicate;

/**
 * Pure validation logic for user registration forms.
 * Extracted from RegisterActivity so it can be unit tested without Android framework dependencies.
 */
public class RegistrationValidator {

    /**
     * Result of a registration validation check.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorReason;

        private ValidationResult(boolean valid, String errorReason) {
            this.valid = valid;
            this.errorReason = errorReason;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult failure(String reason) {
            return new ValidationResult(false, reason);
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorReason() {
            return errorReason;
        }
    }

    public static final String ERROR_FIELDS_REQUIRED = "All fields are required";
    public static final String ERROR_PASSWORD_MISMATCH = "Passwords do not match";
    public static final String ERROR_USERNAME_TAKEN = "Username is already taken";

    /**
     * Validates registration input fields.
     *
     * @param name             the user's display name
     * @param username         the desired username
     * @param email            the user's email address
     * @param password         the chosen password
     * @param confirmPassword  the password confirmation entry
     * @param isUsernameTaken  predicate that returns true if the username is already in use
     * @return ValidationResult indicating success or failure with a reason
     */
    public static ValidationResult validate(
            String name,
            String username,
            String email,
            String password,
            String confirmPassword,
            Predicate<String> isUsernameTaken) {

        // Check all required fields are non-null and non-empty after trimming
        if (isNullOrEmpty(name) || isNullOrEmpty(username) || isNullOrEmpty(email)
                || isNullOrEmpty(password) || isNullOrEmpty(confirmPassword)) {
            return ValidationResult.failure(ERROR_FIELDS_REQUIRED);
        }

        // Check password and confirm password match
        if (!password.equals(confirmPassword)) {
            return ValidationResult.failure(ERROR_PASSWORD_MISMATCH);
        }

        // Check username uniqueness
        if (isUsernameTaken.test(username)) {
            return ValidationResult.failure(ERROR_USERNAME_TAKEN);
        }

        return ValidationResult.success();
    }

    private static boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
