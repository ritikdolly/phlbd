package com.phlbd.service;

import com.phlbd.common.EntityMapper;
import com.phlbd.dto.auth.UserDto;
import com.phlbd.dto.customer.AddressDto;
import com.phlbd.dto.customer.AddressRequest;
import com.phlbd.dto.customer.UpdateProfileRequest;
import com.phlbd.entity.Address;
import com.phlbd.entity.User;
import com.phlbd.exception.ResourceNotFoundException;
import com.phlbd.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final UserRepository userRepository;

    public UserDto getProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return EntityMapper.toUserDto(user);
    }

    public UserDto updateProfile(String userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName().trim());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl().trim());
        }

        User updated = userRepository.save(user);
        return EntityMapper.toUserDto(updated);
    }

    public List<AddressDto> getAddresses(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return user.getAddresses().stream()
                .map(EntityMapper::toAddressDto)
                .collect(Collectors.toList());
    }

    public AddressDto addAddress(String userId, AddressRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        boolean isDefault = Boolean.TRUE.equals(request.getIsDefault()) || user.getAddresses().isEmpty();
        if (isDefault) {
            user.getAddresses().forEach(a -> a.setDefault(false));
        }

        Address address = Address.builder()
                .id(UUID.randomUUID().toString())
                .label(request.getLabel())
                .streetLine(request.getStreetLine())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .landmark(request.getLandmark())
                .contactPhone(request.getContactPhone() != null ? request.getContactPhone() : user.getPhone())
                .isDefault(isDefault)
                .build();

        user.addAddress(address);
        userRepository.save(user);
        return EntityMapper.toAddressDto(address);
    }

    public AddressDto updateAddress(String userId, String addressId, AddressRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Address address = user.getAddresses().stream()
                .filter(a -> a.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        if (Boolean.TRUE.equals(request.getIsDefault()) && !address.isDefault()) {
            user.getAddresses().forEach(a -> a.setDefault(false));
            address.setDefault(true);
        }

        address.setLabel(request.getLabel());
        address.setStreetLine(request.getStreetLine());
        address.setCity(request.getCity());
        if (request.getState() != null) address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        if (request.getLandmark() != null) address.setLandmark(request.getLandmark());
        if (request.getContactPhone() != null) address.setContactPhone(request.getContactPhone());

        userRepository.save(user);
        return EntityMapper.toAddressDto(address);
    }

    public void deleteAddress(String userId, String addressId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.removeAddress(addressId);
        userRepository.save(user);
    }

    public void setDefaultAddress(String userId, String addressId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.getAddresses().forEach(a -> a.setDefault(a.getId().equals(addressId)));
        userRepository.save(user);
    }
}
