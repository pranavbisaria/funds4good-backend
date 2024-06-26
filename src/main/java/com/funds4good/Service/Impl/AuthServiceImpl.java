package com.funds4good.Service.Impl;

import com.funds4good.Config.AppConstants;
import com.funds4good.Config.UserCache;
import com.funds4good.Exceptions.ResourceNotFoundException;
import com.funds4good.Models.*;
import com.funds4good.Payloads.*;
import com.funds4good.Repository.*;
import com.funds4good.Security.JwtAuthRequest;
import com.funds4good.Service.AuthService;
import com.funds4good.Service.JWTTokenGenerator;
import com.funds4good.Service.OTPService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepo roleRepo;
    private final UserCache userCache;
    private final JWTTokenGenerator jwtTokenGenerator;
    private final OTPService otpService;
    @Override
    public ResponseEntity<?> LoginAPI(JwtAuthRequest request, Integer RoleID) {
        request.setEmail(request.getEmail().trim().toLowerCase());
        User user = this.userRepo.findByEmail(request.getEmail()).orElseThrow(() ->new ResourceNotFoundException("User", "Email: " + request.getEmail(), 0));
        if (user.isEnabled()) {
            JwtAuthResponse response = this.jwtTokenGenerator.getTokenGenerate(request.getEmail(), request.getPassword());
            if (response == null) {
                return new ResponseEntity<>(new ApiResponse("Invalid Password", true), HttpStatus.UNAUTHORIZED);
            } else {
                response.setName(user.getName());
                response.setRoles(user.getRoles());
                response.setEmail(request.getEmail());
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
        }
        if (this.userCache.isCachePresent(request.getEmail())) {
            this.userCache.clearCache(request.getEmail());
        }
        OtpDto otpDto = new OtpDto(request.getEmail(), this.otpService.OTPRequest(request.getEmail()), null, false);
        this.userCache.setUserCache(request.getEmail(), otpDto);
        return new ResponseEntity<>(new ApiResponse("OTP has been successfully sent on the registered email id!!", true), HttpStatus.ACCEPTED);
    }
//    @Override
//    public ResponseEntity<?> registerEmail(EmailDto emailDto) {
//        String email = emailDto.getEmail().trim().toLowerCase();
//        Role newRole = this.roleRepo.findById(AppConstants.ROLE_NORMAL).orElse(null);
//        if (this.emailExists(email)) {
//            return new ResponseEntity<>(new ApiResponse("User already exist with the entered email id", false), HttpStatus.CONFLICT);
//        }
//        try {
//            if (this.userCache.isCachePresent(email)) {
//                this.userCache.clearCache(email);
//            }
//            OtpDto otpDto = new OtpDto(email, this.otpService.OTPRequest(email), newRole, false);
//            this.userCache.setUserCache(email, otpDto);
//            return new ResponseEntity<>(new ApiResponse("OTP Sent Success on the entered Email", true), HttpStatus.OK);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ResponseEntity<>(new ApiResponse("Can't able to make your request", false), HttpStatus.SERVICE_UNAVAILABLE);
//        }
//    }
//    @Override
//    public ResponseEntity<?> verifyToRegister(OtpDto otpDto) {
//        String email = otpDto.getEmail().trim().toLowerCase();
//        if (!this.userCache.isCachePresent(email)) {
//            return new ResponseEntity<>(new ApiResponse("Invalid Request", false), HttpStatus.FORBIDDEN);
//        } else {
//            OtpDto storedOtpDto = (OtpDto)this.userCache.getCache(otpDto.getEmail());
//            if (storedOtpDto.getOne_time_password() == otpDto.getOne_time_password()) {
//                return new ResponseEntity<>(new ApiResponse("OTP Successfully Verified", true), HttpStatus.OK);
//            } else {
//                return new ResponseEntity<>(new ApiResponse("Invalid OTP!!", false), HttpStatus.NOT_ACCEPTABLE);
//            }
//        }
//    }
    @Override
    public ResponseEntity<?> signupUser(UserDto userDto) {
        userDto.setName(userDto.getName().trim());
        String email = userDto.getEmail().trim().toLowerCase();
        if (this.emailExists(email)) {
            return new ResponseEntity<>(new ApiResponse("Email Already Exist", false), HttpStatus.CONFLICT);
        }
        User user = new User();
        user.setEmail(email);
        user.setName(userDto.getName());
        Role role = this.roleRepo.findById(AppConstants.ROLE_NORMAL).orElse(null);
        user.getRoles().add(role);
        user.setPassword(this.passwordEncoder.encode(userDto.getPassword()));
        this.userRepo.save(user);

        JwtAuthResponse response = this.jwtTokenGenerator.getTokenGenerate(userDto.getEmail(), userDto.getPassword());
        if (response == null) {
            return new ResponseEntity<>(new ApiResponse("Invalid Password", true), HttpStatus.UNAUTHORIZED);
        }

        response.setName(user.getName());
        response.setRoles(user.getRoles());
        response.setEmail(user.getEmail());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

//    @Override
//    public ResponseEntity<?> verifyOTPPasswordChange(OtpDto otpDto) {
//        String email = otpDto.getEmail().trim().toLowerCase();
//        User userOTP = this.userRepo.findByEmail(email).orElseThrow(() ->new ResourceNotFoundException("User", "Email: " + email, 0));
//        if (!userOTP.isEnabled() || userOTP.getPassword()==null) {
//            if (!this.userCache.isCachePresent(email)) {
//                return new ResponseEntity<>(new ApiResponse("Session Time-Out, please try again", false), HttpStatus.REQUEST_TIMEOUT);
//            } else {
//                OtpDto storedOtpDto = (OtpDto)this.userCache.getCache(email);
//                return storedOtpDto.getOne_time_password() == otpDto.getOne_time_password() ? new ResponseEntity<>(new ApiResponse("OTP Successfully Verified", true), HttpStatus.OK) : new ResponseEntity<>(new ApiResponse("Invalid OTP!!", false), HttpStatus.NOT_ACCEPTABLE);
//            }
//        } else {
//            return new ResponseEntity<>(new ApiResponse("INVALID ACTION!!!", false), HttpStatus.BAD_REQUEST);
//        }
//    }
    public boolean emailExists(String email) {
        return this.userRepo.findByEmail(email).isPresent();
    }
//    @Override
//    public ResponseEntity<?> resetPassword(ForgetPassword forgetPassword) {
//        String email = forgetPassword.getEmail().trim().toLowerCase();
//        User userRP = this.userRepo.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User", "Email :" + email, 0L));
//        if (!userRP.isEnabled()) {
//            if (!this.userCache.isCachePresent(email)) {
//                return new ResponseEntity<>(new ApiResponse("Session Time-Out, please try again", false), HttpStatus.REQUEST_TIMEOUT);
//            } else {
//                OtpDto storedOtpDto = (OtpDto)this.userCache.getCache(email);
//                if (storedOtpDto.getOne_time_password() == forgetPassword.getOtp()) {
//                    userRP.setPassword(this.passwordEncoder.encode(forgetPassword.getPassword()));
//                    userRP.setEnable(true);
//                    this.userRepo.save(userRP);
//                    return new ResponseEntity<>(new ApiResponse("Password Reset SUCCESS", true), HttpStatus.OK);
//                } else {
//                    return new ResponseEntity<>(new ApiResponse("Invalid OTP!!!", false), HttpStatus.FORBIDDEN);
//                }
//            }
//        } else {
//            return new ResponseEntity<>(new ApiResponse("Invalid Action!!", false), HttpStatus.NOT_ACCEPTABLE);
//        }
//    }
//    @Override
//    public ResponseEntity<?> sendOTPForget(EmailDto emailDto) throws Exception {
//        String email = emailDto.getEmail().trim().toLowerCase();
//        User user = this.userRepo.findByEmail(email).orElseThrow(() ->new ResourceNotFoundException("User", "Email: " + email, 0));
//        try {
//            OtpDto otp = new OtpDto(email, this.otpService.OTPRequest(email),null, true);
//            user.setEnable(false);
//            user.setPassword(null);
//            if (this.userCache.isCachePresent(email)) {
//                this.userCache.clearCache(email);
//            }
//            this.userCache.setUserCache(email, otp);
//            this.userRepo.save(user);
//            return new ResponseEntity<>(new ApiResponse("OTP Sent Success", true), HttpStatus.OK);
//        } catch (Exception e) {
//            throw new Exception("Cannot able to send the mail to the registered account", e);
//        }
//    }
}
