package com.jve.repsup.utils;

/**
 * Validates registration form inputs independently of Android UI.
 * Extracted from RegisterActivity to enable property-based testing.
 */
public class RegistrationValidator {

    public enum ValidationResult {
        VALID,
        EMPTY_FIELD,
        PASSWORD_MISMATCH,
        USERNAME_TAKEN
    }

    /**
     * Validates registration inputs.
     *
     * @param name             user's first name
     * @param username         desired username
     * @param email            user's email
     * @param password         desired password
     * @param confirmPassword  password confirmation
     * @param isUsernameTaken  whether the username already exists in the User table
     * @return ValidationResult indicating success or specific failure reason
     */
    public static ValidationResult validate(String name, String username, String email,
                                            String password, String confirmPassword,
                                            boolean isUsernameTaken) {
        // Check required fields are non-null and non-empty
        if (name == null || name.trim().isEmpty() ||
                username == null || username.trim().isEmpty() ||
                email == null || email.trim().isEmpty() ||
                password == null || password.trim().isEmpty() ||
                confirmPassword == null || confirmPassword.trim().isEmpty()) {
            return ValidationResult.EMPTY_FIELD;
        }

        // Check password match
        if (!password.equals(confirmPassword)) {
            return ValidationResult.PASSWORD_MISMATCH;
        }

        // Check username uniqueness
        if (isUsernameTaken) {
            return ValidationResult.USERNAME_TAKEN;
        }

        return ValidationResult.VALID;
    }
}
