package com.example.demo.domain.recommendation.repository.bundle;

import com.example.demo.domain.recommendation.entity.bundle.Bundle;
import com.example.demo.domain.recommendation.entity.bundle.BundleTimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BundleRepository extends JpaRepository<Bundle, Long> {

    List<Bundle> findByTimeSlotOrderByIdAsc(BundleTimeSlot timeSlot);
}
