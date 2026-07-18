package com.splitwise.repository;

import com.splitwise.model.OtpEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<OtpEntity, Long> {

    Optional<OtpEntity> findTopByEmailAndOtpOrderByCreatedAtDesc(String email, String otp);

    Optional<OtpEntity> findTopByEmailAndOtpAndIsUsedFalseOrderByCreatedAtDesc(String email, String otp);

    void deleteByExpiryTimeBefore(LocalDateTime time);

}
