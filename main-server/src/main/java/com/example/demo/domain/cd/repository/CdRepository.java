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
          c.playlist.id as playlistId,
          p.id as propId,
          c.xCoordinate as xCoordinate,
          c.yCoordinate as yCoordinate,
          c.height as height,
          c.width as width,
          c.scale as scale,
          c.angle as angle,
          p.theme as theme,
          p.imageKey as imageKey
        from Cd c
          join c.prop p
        where c.playlist.id = :playlistId
    """)
    List<CdItemView> findAllByPlaylistWithImageKeys(Long playlistId);

    @Query("""
       select
        c.id as cdId,
        c.playlist.id as playlistId,
        p.id as propId,
        c.xCoordinate as xCoordinate,
        c.yCoordinate as yCoordinate,
        c.height as height,
        c.width as width,
        c.scale as scale,
        c.angle as angle,
        p.theme as theme,
        p.imageKey as imageKey
       from Cd c
        join c.prop p
       where c.playlist.id in :playlistIdList
    order by c.playlist.id asc, c.id asc
    """)
    List<CdItemView> findAllByPlaylistIdWithImageKeysIn(@Param("playlistIdList") List<Long> playlistIdList);

    void deleteByPlaylistId(Long playlistId);
}
