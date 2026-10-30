package com.phlbd.service;

import com.phlbd.common.EntityMapper;
import com.phlbd.dto.coupon.ApplyCouponRequest;
import com.phlbd.dto.coupon.CouponDto;
import com.phlbd.dto.coupon.CouponRequest;
import com.phlbd.dto.coupon.CouponValidationResponse;
import com.phlbd.entity.Coupon;
import com.phlbd.exception.BadRequestException;
import com.phlbd.exception.ResourceNotFoundException;
import com.phlbd.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    public CouponValidationResponse validateCoupon(ApplyCouponRequest request) {
        Optional<Coupon> couponOpt = couponRepository.findByCodeIgnoreCase(request.getCode().trim());
        if (couponOpt.isEmpty()) {
            return CouponValidationResponse.builder()
                    .valid(false)
                    .code(request.getCode().toUpperCase())
                    .discountAmount(BigDecimal.ZERO)
                    .message("Invalid coupon code")
                    .build();
        }

        Coupon coupon = couponOpt.get();
        if (!coupon.isValidForAmount(request.getSubtotal())) {
            return CouponValidationResponse.builder()
                    .valid(false)
                    .code(coupon.getCode())
                    .discountAmount(BigDecimal.ZERO)
                    .message("Coupon is not applicable or minimum order criteria (" + coupon.getMinOrderValue() + " INR) not met")
                    .build();
        }

        BigDecimal discount = coupon.calculateDiscount(request.getSubtotal());
        return CouponValidationResponse.builder()
                .valid(true)
                .code(coupon.getCode())
                .discountAmount(discount)
                .message("Coupon applied successfully! You saved " + discount + " INR")
                .build();
    }

    public void recordCouponUsage(String code) {
        if (code == null || code.isBlank()) return;
        couponRepository.findByCodeIgnoreCase(code.trim()).ifPresent(coupon -> {
            coupon.setTimesUsed(coupon.getTimesUsed() + 1);
            couponRepository.save(coupon);
        });
    }

    public List<CouponDto> getAllCoupons() {
        return couponRepository.findAll().stream()
                .map(EntityMapper::toCouponDto)
                .collect(Collectors.toList());
    }

    public CouponDto createCoupon(CouponRequest request) {
        String code = request.getCode().toUpperCase().trim();
        if (couponRepository.existsByCodeIgnoreCase(code)) {
            throw new BadRequestException("Coupon code already exists: " + code);
        }

        Coupon coupon = Coupon.builder()
                .code(code)
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .minOrderValue(request.getMinOrderValue() != null ? request.getMinOrderValue() : BigDecimal.ZERO)
                .maxDiscount(request.getMaxDiscount())
                .validFrom(request.getValidFrom())
                .validUntil(request.getValidUntil())
                .usageLimit(request.getUsageLimit() != null ? request.getUsageLimit() : 1000)
                .timesUsed(0)
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        Coupon saved = couponRepository.save(coupon);
        return EntityMapper.toCouponDto(saved);
    }

    public CouponDto updateCoupon(String id, CouponRequest request) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "id", id));

        String code = request.getCode().toUpperCase().trim();
        if (!code.equalsIgnoreCase(coupon.getCode()) && couponRepository.existsByCodeIgnoreCase(code)) {
            throw new BadRequestException("Coupon code already exists: " + code);
        }

        coupon.setCode(code);
        coupon.setDiscountType(request.getDiscountType());
        coupon.setDiscountValue(request.getDiscountValue());
        if (request.getMinOrderValue() != null) coupon.setMinOrderValue(request.getMinOrderValue());
        if (request.getMaxDiscount() != null) coupon.setMaxDiscount(request.getMaxDiscount());
        if (request.getValidFrom() != null) coupon.setValidFrom(request.getValidFrom());
        if (request.getValidUntil() != null) coupon.setValidUntil(request.getValidUntil());
        if (request.getUsageLimit() != null) coupon.setUsageLimit(request.getUsageLimit());
        if (request.getActive() != null) coupon.setActive(request.getActive());

        Coupon updated = couponRepository.save(coupon);
        return EntityMapper.toCouponDto(updated);
    }

    public void deleteCoupon(String id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "id", id));
        couponRepository.delete(coupon);
    }
}
