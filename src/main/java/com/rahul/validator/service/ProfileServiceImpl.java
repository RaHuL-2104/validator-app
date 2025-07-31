package com.rahul.validator.service;

import com.rahul.validator.io.ProfileResponse;
import com.rahul.validator.io.ProfileRequest;
import com.rahul.validator.repository.UserRepository;
import com.rahul.validator.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    @Override
    public ProfileResponse createProfile(ProfileRequest request){
        UserEntity newProfile = convertToUserEntity(request);
        if(!userRepository.existsByEmail(request.getEmail())){
            newProfile = userRepository.save(newProfile);
            return convertToProfileResponse(newProfile);
        }
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
    }

    @Override
    public ProfileResponse getProfile(String email) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        return convertToProfileResponse(existingUser);
    }

    @Override
    public void sendResetOtp(String email) {
        UserEntity existingEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
        long expiryTime = System.currentTimeMillis() + (15*60*1000);
        existingEntity.setResetOtp(otp);
        existingEntity.setResetOtpExpireAt(expiryTime);
        userRepository.save(existingEntity);
        try{
            emailService.sendResetOtpEmail(existingEntity.getEmail(), otp);
        }catch(Exception ex){
            throw new RuntimeException("Unable to send email");
        }
    }

    @Override
    public void resetPassword(String email, String otp, String newPassword){
        UserEntity existingUser =  userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        if(existingUser.getResetOtp() == null || !existingUser.getResetOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }
        if(existingUser.getResetOtpExpireAt() < System.currentTimeMillis()) {
            throw new RuntimeException("OTP has expired");
        }
        existingUser.setPassword(passwordEncoder.encode(newPassword));
        existingUser.setResetOtp(null);
        existingUser.setResetOtpExpireAt(0L);
        userRepository.save(existingUser);
    }

    @Override
    public void sendOtp(String email){
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        
        if(existingUser.getIsAccountVerified() != null && existingUser.getIsAccountVerified()){
            return;
        }

        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
        long expiryTime = System.currentTimeMillis() + (24 * 60 * 60 *1000);

        existingUser.setVerifyOtp(otp);
        existingUser.setVerifyOtpExpireAt(expiryTime);
        userRepository.save(existingUser);
        try{
            emailService.sendOtpEmail(existingUser.getEmail(), otp);
        } catch(Exception ex){
            throw new RuntimeException("Unable to send email");
        }
    }

    @Override
    public void verifyOtp(String email, String otp){
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        
        if(existingUser.getVerifyOtp() == null || !existingUser.getVerifyOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }
        if(existingUser.getVerifyOtpExpireAt() < System.currentTimeMillis()) {
            throw new RuntimeException("OTP has expired");
        }
        
        existingUser.setIsAccountVerified(true);
        existingUser.setVerifyOtp(null);
        existingUser.setVerifyOtpExpireAt(0L);
        userRepository.save(existingUser);
    }

    @Override
    public String getLoggedInUserId(String email){
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
                return existingUser.getUserId();
    }


    private UserEntity convertToUserEntity(ProfileRequest request){
        return UserEntity.builder()
                .email(request.getEmail())
                .userId(UUID.randomUUID().toString())
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .isAccountVerified(false)
                .resetOtpExpireAt(0L)
                .verifyOtp(null)
                .verifyOtpExpireAt(0L)
                .resetOtp(null)
                .build();
    }
    private ProfileResponse convertToProfileResponse(UserEntity newProfile){
        return ProfileResponse.builder()
                .email(newProfile.getEmail())
                .name(newProfile.getName())
                .userId(newProfile.getUserId())
                .isAccountVerified(newProfile.getIsAccountVerified())
                .build();
    }
}
