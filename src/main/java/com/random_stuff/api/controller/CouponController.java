package com.random_stuff.api.controller;
 
import com.random_stuff.api.dto.ApiResponse;
import com.random_stuff.api.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
 
import java.math.BigDecimal;
import java.util.Map;
 
@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;
 
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateCoupon(@RequestBody Map<String, Object> request) {
        String code = (String) request.get("code");
        BigDecimal orderTotal = new BigDecimal(request.get("orderTotal").toString());
 
        Map<String, Object> result = couponService.validateCoupon(code, orderTotal);
 
        if ((Boolean) result.get("valid")) {
            return ResponseEntity.ok(ApiResponse.success(result));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error((String) result.get("message")));
        }
    }
}