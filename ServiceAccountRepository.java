package com.bank.capp.repository;

import com.bank.capp.models.ServiceAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceAccountRepository extends JpaRepository<ServiceAccount, Long> {

    Optional<ServiceAccount> findByServiceName(String serviceName);

    Optional<ServiceAccount> findByApiKeyHash(String apiKeyHash);

    List<ServiceAccount> findAllByActive(boolean active);

    boolean existsByServiceName(String serviceName);
}