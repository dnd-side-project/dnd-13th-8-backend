package com.example.demo.domain.cd.repository;

import com.example.demo.domain.cd.entity.Cd;
import com.example.demo.domain.cd.repository.projection.CdItemView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CdRepository extends JpaRepository<Cd, Long> {
    @Query("""
        select
          c.id as cdId,
          p.id as propId,
          c.xCoordinate as xCoordinate,
          c.yCoordinate as yCoordinate,
          c.zCoordinate as zCoordinate,
          c.angle as angle,
          p.imageKey as imageKey
        from Cd c
          join c.prop p
        where c.playlist.id = :playlistId
    """)
    List<CdItemView> findAllWithImageKeys(Long playlistId);
}
