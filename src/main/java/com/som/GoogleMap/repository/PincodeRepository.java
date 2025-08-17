package com.som.GoogleMap.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.som.GoogleMap.entity.PincodeInfo;

public interface PincodeRepository extends JpaRepository<PincodeInfo, Long> {
    Optional<PincodeInfo> findByPincode(String pincode);
}
