package com.rahul.validator.service;

import com.rahul.validator.io.ProfileResponse;
import com.rahul.validator.io.ProfileRequest;

public interface ProfileService {
    ProfileResponse createProfile(ProfileRequest request);
    ProfileResponse getProfile(String email);
    void sendResetOtp(String email);
    void resetPassword(String email, String otp, String newPassword);
    void sendOtp(String email);
    void verifyOtp(String email, String otp);
    String getLoggedInUserId(String email);
}
