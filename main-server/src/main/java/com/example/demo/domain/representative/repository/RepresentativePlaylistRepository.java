package com.example.demo.domain.representative.repository;


import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.representative.entity.RepresentativePlaylist;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RepresentativePlaylistRepository extends JpaRepository<RepresentativePlaylist, Long>,
        RepresentativePlaylistRepositoryCustom {

    Optional<RepresentativePlaylist> findByUser_Id(String usersId);

//    @Query("""
//    SELECT rp.playlist.id
//    FROM RepresentativePlaylist rp
//    """)
//    List<Long> findAllPlaylistIds();


    boolean existsByPlaylist_Id(Long playlistId);

    @Query("""
    SELECT DISTINCT r.user.id
    FROM RepresentativePlaylist r
    WHERE r.user IS NOT NULL
""")
    List<String> findUserIdsWithRepPlaylist();


    @Query("""
    SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
    FROM RepresentativePlaylist r
    WHERE r.user.id = :userId AND r.playlist.id = :playlistId
""")
    boolean isRepresentativePlaylist(@Param("userId") String userId, @Param("playlistId") Long playlistId);

    void deleteByPlaylist_Id(Long playlistId);

    @Query(value = """
    SELECT r.playlist_id
    FROM representative_playlist r
    WHERE r.user_id IN (:userIds)
    ORDER BY FIELD(r.user_id, :userIds)
""", nativeQuery = true)
    List<Long> findAllPlaylistIdsInOrder(@Param("userIds") List<String> userIds);


    @Query("SELECT r FROM RepresentativePlaylist r WHERE r.user.id IN :userIds")
    List<RepresentativePlaylist> findByUserIds(@Param("userIds") List<String> userIds);

}


