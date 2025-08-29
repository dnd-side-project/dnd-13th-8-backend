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

    boolean existsByPlaylist_Id(Long playlistId);


    @Query("""
    SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
    FROM RepresentativePlaylist r
    WHERE r.user.id = :userId AND r.playlist.id = :playlistId
""")
    boolean isRepresentativePlaylist(@Param("userId") String userId, @Param("playlistId") Long playlistId);

    void deleteByPlaylist_Id(Long playlistId);


    @Query("""
    SELECT r.playlist.id
    FROM RepresentativePlaylist r
    WHERE r.user.id <> :excludedUserId
""")
    List<Long> findAllPlaylistIdsExcludingUser(@Param("excludedUserId") String excludedUserId);

    @Query("""
    SELECT COUNT(r)
    FROM RepresentativePlaylist r
""")
    Long countByAllUserId();


}


