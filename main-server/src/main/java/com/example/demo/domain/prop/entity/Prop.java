package com.example.demo.domain.prop.entity;

import com.example.demo.domain.user.entity.User;
import jakarta.persistence.*;

@Entity
public class Prop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false )
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "image_url")
    private String imageUrl;

}
