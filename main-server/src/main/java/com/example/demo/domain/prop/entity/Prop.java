package com.example.demo.domain.prop.entity;

import com.example.demo.domain.user.entity.Users;
import jakarta.persistence.*;

@Entity
public class Prop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false )
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users users;

    @Column(name = "image_url")
    private String imageUrl;

}
