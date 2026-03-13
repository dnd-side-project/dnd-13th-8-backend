package com.example.demo.domain.recommendation.repository.bundle;

import com.example.demo.domain.recommendation.entity.bundle.Bundle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BundleRepository extends JpaRepository<Bundle, Long> {
}
