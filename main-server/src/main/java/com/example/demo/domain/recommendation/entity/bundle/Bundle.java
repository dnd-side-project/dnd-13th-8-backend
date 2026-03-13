package com.example.demo.domain.recommendation.entity.bundle;

import com.example.demo.global.time.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "bundle")
@NoArgsConstructor
public class Bundle extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "time_slot", nullable = false)
    private BundleTimeSlot timeSlot;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Builder
    public Bundle(
            BundleTimeSlot timeSlot,
            String title
    ) {
        this.timeSlot = timeSlot;
        this.title = title;
    }
}
