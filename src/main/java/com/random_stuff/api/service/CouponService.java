package com.random_stuff.api.service;
 
import com.random_stuff.api.entity.Coupon;
import com.random_stuff.api.exception.BadRequestException;
import com.random_stuff.api.exception.ResourceNotFoundException;
import com.random_stuff.api.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
 
@Service
@RequiredArgsConstructor
public class CouponService {
 
    private final CouponRepository couponRepository;
 
    @Transactional(readOnly = true)
    public Map<String, Object> validateCoupon(String code, BigDecimal orderTotal) {
        Map<String, Object> result = new HashMap<>();
 
        Coupon coupon = couponRepository.findByCodeIgnoreCase(code).orElse(null);
 
        if (coupon == null) {
            result.put("valid", false);
            result.put("message", "Invalid coupon code");
            return result;
        }
 
        if (!coupon.isValid()) {
            result.put("valid", false);
            result.put("message", "Coupon has expired or is no longer available");
            return result;
        }
 
        if (coupon.getMinOrder() != null && orderTotal.compareTo(coupon.getMinOrder()) < 0) {
            result.put("valid", false);
            result.put("message", "Minimum order of ₵" + coupon.getMinOrder() + " required");
            return result;
        }
 
        BigDecimal discount = coupon.calculateDiscount(orderTotal);
 
        result.put("valid", true);
        result.put("code", coupon.getCode());
        result.put("type", coupon.getType().name().toLowerCase());
        result.put("discount", discount);
        result.put("discountValue", coupon.getDiscount());
 
        return result;
    }
 
    @Transactional
    public BigDecimal applyCoupon(String code, BigDecimal orderTotal) {
        Coupon coupon = couponRepository.findByCodeIgnoreCase(code)
            .orElseThrow(() -> new ResourceNotFoundException("Coupon", "code", code));
 
        if (!coupon.isValid()) {
            throw new BadRequestException("Coupon has expired or is no longer available");
        }
 
        if (coupon.getMinOrder() != null && orderTotal.compareTo(coupon.getMinOrder()) < 0) {
            throw new BadRequestException("Minimum order of " + coupon.getMinOrder() + " required");
        }
 
        BigDecimal discount = coupon.calculateDiscount(orderTotal);
        coupon.incrementUsage();
        couponRepository.save(coupon);
 
        return discount;
    }
}