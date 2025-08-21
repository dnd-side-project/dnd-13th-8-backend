package com.example.demo.domain.prop.repository;

import com.example.demo.domain.prop.entity.Prop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PropRepository extends JpaRepository<Prop, Long> {

    List<Prop> findAllByUsersId(String userId);

    Optional<Prop> findByIdAndUsersId(Long propId, String userId);

}
