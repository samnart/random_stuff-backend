package com.random_stuff.api.repository;
 
import com.random_stuff.api.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
 
import java.util.Optional;
 
@Repository
public interface CouponRepository extends JpaRepository<Coupon, String> {
 
    Optional<Coupon> findByCodeIgnoreCase(String code);
 
    boolean existsByCodeIgnoreCase(String code);
}