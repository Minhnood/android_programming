package com.example.bai1;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ValidatorsTest {

    @Test
    public void validEmails() {
        assertTrue(Validators.isValidEmail("a@b.com"));
        assertTrue(Validators.isValidEmail("brian.nguyen.work@gmail.com"));
        assertTrue(Validators.isValidEmail("  user@domain.co  ")); // có khoảng trắng -> vẫn hợp lệ sau khi trim
    }

    @Test
    public void invalidEmails() {
        assertFalse(Validators.isValidEmail(null));
        assertFalse(Validators.isValidEmail(""));
        assertFalse(Validators.isValidEmail("plainaddress"));
        assertFalse(Validators.isValidEmail("a@b"));        // thiếu phần TLD
        assertFalse(Validators.isValidEmail("@b.com"));      // thiếu local part
        assertFalse(Validators.isValidEmail("a@@b.com"));
    }

    @Test
    public void passwordRules() {
        assertTrue(Validators.isValidPassword("123456"));
        assertTrue(Validators.isValidPassword("abcdef"));
        assertFalse(Validators.isValidPassword(null));
        assertFalse(Validators.isValidPassword(""));
        assertFalse(Validators.isValidPassword("12345")); // 5 ký tự
    }

    @Test
    public void phoneRules() {
        assertTrue(Validators.isValidPhone("0123456789"));
        assertTrue(Validators.isValidPhone("  0987654321  ")); // trim
        assertFalse(Validators.isValidPhone(null));
        assertFalse(Validators.isValidPhone("123456789"));     // không bắt đầu bằng 0
        assertFalse(Validators.isValidPhone("01234"));         // quá ngắn
        assertFalse(Validators.isValidPhone("01234567890"));   // quá dài
        assertFalse(Validators.isValidPhone("0abcd56789"));    // có chữ
    }
}
