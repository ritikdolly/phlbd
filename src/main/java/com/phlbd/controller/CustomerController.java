package com.phlbd.controller;

import com.phlbd.common.ApiResponse;
import com.phlbd.dto.auth.UserDto;
import com.phlbd.dto.customer.AddressDto;
import com.phlbd.dto.customer.AddressRequest;
import com.phlbd.dto.customer.UpdateProfileRequest;
import com.phlbd.security.SecurityUtils;
import com.phlbd.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
@Tag(name = "Customer Profile & Addresses", description = "Endpoints for customer personal profile and address book")
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/profile")
    @Operation(summary = "Get current customer profile")
    public ResponseEntity<ApiResponse<UserDto>> getProfile() {
        String userId = SecurityUtils.getCurrentUserId();
        UserDto profile = customerService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update customer personal information")
    public ResponseEntity<ApiResponse<UserDto>> updateProfile(@RequestBody UpdateProfileRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        UserDto updated = customerService.updateProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updated));
    }

    @GetMapping("/addresses")
    @Operation(summary = "Get saved delivery addresses")
    public ResponseEntity<ApiResponse<List<AddressDto>>> getAddresses() {
        String userId = SecurityUtils.getCurrentUserId();
        List<AddressDto> addresses = customerService.getAddresses(userId);
        return ResponseEntity.ok(ApiResponse.success(addresses));
    }

    @PostMapping("/addresses")
    @Operation(summary = "Add a new delivery address")
    public ResponseEntity<ApiResponse<AddressDto>> addAddress(@Valid @RequestBody AddressRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        AddressDto address = customerService.addAddress(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Address added successfully", address));
    }

    @PutMapping("/addresses/{id}")
    @Operation(summary = "Update an existing address")
    public ResponseEntity<ApiResponse<AddressDto>> updateAddress(
            @PathVariable String id,
            @Valid @RequestBody AddressRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        AddressDto address = customerService.updateAddress(userId, id, request);
        return ResponseEntity.ok(ApiResponse.success("Address updated successfully", address));
    }

    @DeleteMapping("/addresses/{id}")
    @Operation(summary = "Delete an address")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(@PathVariable String id) {
        String userId = SecurityUtils.getCurrentUserId();
        customerService.deleteAddress(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Address deleted successfully", null));
    }

    @PatchMapping("/addresses/{id}/default")
    @Operation(summary = "Set an address as default delivery location")
    public ResponseEntity<ApiResponse<Void>> setDefaultAddress(@PathVariable String id) {
        String userId = SecurityUtils.getCurrentUserId();
        customerService.setDefaultAddress(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Default address updated", null));
    }
}
