package com.jve.repsup;

import com.jve.repsup.utils.RegistrationValidator;
import com.jve.repsup.utils.RegistrationValidator.ValidationResult;

import net.jqwik.api.*;
import net.jqwik.api.constraints.NotBlank;
import net.jqwik.api.constraints.StringLength;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for registration validation logic.
 * Uses jqwik to generate random invalid input combinations and verify rejection.
 *
 * Validates: Requirements 3.2, 3.3, 3.4
 */
class RegistrationValidationPropertyTest {

    // ─── Property 9: Registration Validation Rejects Invalid Inputs ───

    /**
     * Property 9a: Any registration with at least one empty required field is rejected.
     * For any combination where at least one of (name, username, email, password, confirmPassword)
     * is null or blank, the validator SHALL return EMPTY_FIELD.
     *
     * Validates: Requirements 3.4
     */
    @Property(tries = 200)
    void emptyFieldAlwaysRejected(
            @ForAll("fieldValuesWithAtLeastOneEmpty") FieldSet fields
    ) {
        ValidationResult result = RegistrationValidator.validate(
                fields.name, fields.username, fields.email,
                fields.password, fields.confirmPassword, false
        );

        assertEquals(ValidationResult.EMPTY_FIELD, result,
                "Registration with an empty required field must be rejected with EMPTY_FIELD");
    }

    /**
     * Property 9b: Any registration where password != confirmPassword is rejected.
     * For any non-empty fields where password and confirmPassword differ,
     * the validator SHALL return PASSWORD_MISMATCH.
     *
     * Validates: Requirements 3.3
     */
    @Property(tries = 200)
    void passwordMismatchAlwaysRejected(
            @ForAll @NotBlank @StringLength(min = 1, max = 50) String name,
            @ForAll @NotBlank @StringLength(min = 1, max = 30) String username,
            @ForAll @NotBlank @StringLength(min = 1, max = 50) String email,
            @ForAll("mismatchedPasswordPair") String[] passwords
    ) {
        ValidationResult result = RegistrationValidator.validate(
                name, username, email,
                passwords[0], passwords[1], false
        );

        assertEquals(ValidationResult.PASSWORD_MISMATCH, result,
                "Registration with mismatched passwords must be rejected with PASSWORD_MISMATCH");
    }

    /**
     * Property 9c: Any registration where username is already taken is rejected.
     * For any valid non-empty fields with matching passwords,
     * if isUsernameTaken is true, the validator SHALL return USERNAME_TAKEN.
     *
     * Validates: Requirements 3.2
     */
    @Property(tries = 200)
    void usernameTakenAlwaysRejected(
            @ForAll @NotBlank @StringLength(min = 1, max = 50) String name,
            @ForAll @NotBlank @StringLength(min = 1, max = 30) String username,
            @ForAll @NotBlank @StringLength(min = 1, max = 50) String email,
            @ForAll @NotBlank @StringLength(min = 1, max = 50) String password
    ) {
        ValidationResult result = RegistrationValidator.validate(
                name, username, email,
                password, password, true  // username is taken
        );

        assertEquals(ValidationResult.USERNAME_TAKEN, result,
                "Registration with a taken username must be rejected with USERNAME_TAKEN");
    }

    /**
     * Complementary property: Any valid input combination yields VALID.
     * For any non-empty fields with matching passwords and username not taken,
     * the validator SHALL return VALID.
     *
     * Validates: Requirements 3.2, 3.3, 3.4
     */
    @Property(tries = 200)
    void validInputsAlwaysAccepted(
            @ForAll @NotBlank @StringLength(min = 1, max = 50) String name,
            @ForAll @NotBlank @StringLength(min = 1, max = 30) String username,
            @ForAll @NotBlank @StringLength(min = 1, max = 50) String email,
            @ForAll @NotBlank @StringLength(min = 1, max = 50) String password
    ) {
        ValidationResult result = RegistrationValidator.validate(
                name, username, email,
                password, password, false  // username not taken
        );

        assertEquals(ValidationResult.VALID, result,
                "Registration with all valid inputs must return VALID");
    }

    // ─── Custom Providers ───

    @Provide
    Arbitrary<FieldSet> fieldValuesWithAtLeastOneEmpty() {
        Arbitrary<String> nonEmpty = Arbitraries.strings()
                .ofMinLength(1).ofMaxLength(30)
                .filter(s -> !s.trim().isEmpty());
        Arbitrary<String> emptyOrNull = Arbitraries.oneOf(
                Arbitraries.just(null),
                Arbitraries.just(""),
                Arbitraries.just("   ")
        );

        // Generate 5 field values, then ensure at least one is empty
        return Combinators.combine(
                Arbitraries.integers().between(0, 4),  // which field to make empty
                nonEmpty, nonEmpty, nonEmpty, nonEmpty, nonEmpty,
                emptyOrNull
        ).as((emptyIndex, name, username, email, password, confirm, emptyVal) -> {
            String[] fields = {name, username, email, password, confirm};
            fields[emptyIndex] = emptyVal;
            return new FieldSet(fields[0], fields[1], fields[2], fields[3], fields[4]);
        });
    }

    @Provide
    Arbitrary<String[]> mismatchedPasswordPair() {
        Arbitrary<String> password = Arbitraries.strings()
                .ofMinLength(1).ofMaxLength(50)
                .filter(s -> !s.trim().isEmpty());

        return Combinators.combine(password, password)
                .filter((p1, p2) -> !p1.equals(p2))
                .as((p1, p2) -> new String[]{p1, p2});
    }

    // ─── Helper class ───

    static class FieldSet {
        final String name;
        final String username;
        final String email;
        final String password;
        final String confirmPassword;

        FieldSet(String name, String username, String email, String password, String confirmPassword) {
            this.name = name;
            this.username = username;
            this.email = email;
            this.password = password;
            this.confirmPassword = confirmPassword;
        }

        @Override
        public String toString() {
            return String.format("FieldSet{name='%s', username='%s', email='%s', password='%s', confirm='%s'}",
                    name, username, email, password, confirmPassword);
        }
    }
}
