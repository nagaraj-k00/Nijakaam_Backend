package com.nijakaam.dto;

import com.nijakaam.dto.request.RegisterRequest;
import com.nijakaam.enums.Role;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RegisterRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Valid 10-digit mobile number passes validation")
    void valid10DigitMobileNumber_PassesValidation() {
        RegisterRequest request = new RegisterRequest(
                "John Doe", "john@example.com", "Password@123", "9876543210", Role.WORKER
        );

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Valid 10-digit mobile number should not have violations");
    }

    @Test
    @DisplayName("Valid mobile number with +91 country code prefix passes validation")
    void validMobileNumberWithPlus91_PassesValidation() {
        RegisterRequest request = new RegisterRequest(
                "John Doe", "john@example.com", "Password@123", "+919876543210", Role.WORKER
        );

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Valid +91 mobile number should not have violations");
    }

    @Test
    @DisplayName("Invalid mobile number starting with 0-5 fails validation")
    void invalidMobileNumber_StartingWithInvalidDigit_FailsValidation() {
        RegisterRequest request = new RegisterRequest(
                "John Doe", "john@example.com", "Password@123", "5876543210", Role.WORKER
        );

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("phone")));
    }

    @Test
    @DisplayName("Invalid mobile number with non-digits or wrong length fails validation")
    void invalidMobileNumber_NonDigitsOrWrongLength_FailsValidation() {
        RegisterRequest shortNumber = new RegisterRequest(
                "John Doe", "john@example.com", "Password@123", "98765", Role.WORKER
        );
        Set<ConstraintViolation<RegisterRequest>> violations1 = validator.validate(shortNumber);
        assertFalse(violations1.isEmpty());

        RegisterRequest nonDigits = new RegisterRequest(
                "John Doe", "john@example.com", "Password@123", "987654321a", Role.WORKER
        );
        Set<ConstraintViolation<RegisterRequest>> violations2 = validator.validate(nonDigits);
        assertFalse(violations2.isEmpty());
    }
}
