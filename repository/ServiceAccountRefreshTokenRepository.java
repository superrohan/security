package com.bank.capp.repository;

import com.bank.capp.models.ServiceAccount;
import com.bank.capp.models.ServiceAccountRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceAccountRefreshTokenRepository extends JpaRepository<ServiceAccountRefreshToken, Long> {
    
    Optional<ServiceAccountRefreshToken> findByToken(String token);
    
    Optional<ServiceAccountRefreshToken> findByServiceAccount(ServiceAccount serviceAccount);
    
    @Modifying
    @Query("DELETE FROM ServiceAccountRefreshToken rt WHERE rt.serviceAccount = :serviceAccount")
    void deleteByServiceAccount(ServiceAccount serviceAccount);
    
    @Modifying
    @Query("DELETE FROM ServiceAccountRefreshToken rt WHERE rt.expiryDate < CURRENT_TIMESTAMP")
    void deleteExpiredTokens();
}
