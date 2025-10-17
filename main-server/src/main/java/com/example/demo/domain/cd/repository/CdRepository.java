package com.example.demo.domain.cd.repository;

import com.example.demo.domain.cd.entity.Cd;
import com.example.demo.domain.cd.repository.projection.CdItemView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CdRepository extends JpaRepository<Cd, Long> {
    @Query("""
    select new com.example.demo.domain.cd.repository.projection.CdItemView(
        c.id,
        c.playlist.id,
        p.id,
        c.xCoordinate,
        c.yCoordinate,
        c.zCoordinate,
        c.height,
        c.width,
        c.scale,
        c.angle,
        p.theme,
        p.imageKey
    )
    from Cd c
    join c.prop p
    where c.playlist.id = :playlistId
""")
    List<CdItemView> findAllByPlaylistWithImageKeys(@Param("playlistId") Long playlistId);

    @Query("""
    select new com.example.demo.domain.cd.repository.projection.CdItemView(
        c.id,
        c.playlist.id,
        p.id,
        c.xCoordinate,
        c.yCoordinate,
        c.zCoordinate,
        c.height,
        c.width,
        c.scale,
        c.angle,
        p.theme,
        p.imageKey
    )
    from Cd c
    join c.prop p
    where c.playlist.id in :playlistIdList
    order by c.playlist.id asc, c.id asc
""")
    List<CdItemView> findAllByPlaylistIdWithImageKeysIn(@Param("playlistIdList") List<Long> playlistIdList);

    void deleteByPlaylistId(Long playlistId);
}
