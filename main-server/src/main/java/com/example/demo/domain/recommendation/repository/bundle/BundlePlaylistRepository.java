package com.example.demo.domain.recommendation.repository.bundle;

import com.example.demo.domain.recommendation.entity.bundle.BundlePlaylist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BundlePlaylistRepository extends JpaRepository<BundlePlaylist, Long> {
    @Query("""
    select bp
    from BundlePlaylist bp
    join fetch bp.playlist p
    where bp.bundle.id = :bundleId
      and p.isPublic = true
    order by bp.orderIndex asc, bp.id asc
""")
    List<BundlePlaylist> findByBundleIdWithPlaylist(Long bundleId);

    @Query("""
    select bp
    from BundlePlaylist bp
    join fetch bp.playlist p
    join fetch bp.bundle b
    order by b.id asc, bp.orderIndex asc, bp.id asc
""")
    List<BundlePlaylist> findAllWithBundleAndPlaylist();

    @Query("""
    select bp
    from BundlePlaylist bp
    join fetch bp.playlist p
    join fetch p.users u
    join fetch bp.bundle b
    where b.id in :bundleIds
    AND p.isPublic = true
    order by b.id asc, bp.orderIndex asc
""")
    List<BundlePlaylist> findByBundleIdsWithPlaylistAndUser(List<Long> bundleIds);
}
