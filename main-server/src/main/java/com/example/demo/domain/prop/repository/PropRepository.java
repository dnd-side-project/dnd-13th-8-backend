package com.example.demo.domain.prop.repository;

import com.example.demo.domain.prop.entity.Prop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PropRepository extends JpaRepository<Prop, Long> {

    public List<Prop> findAllByUser_Id(Long userId);
}
