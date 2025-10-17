package com.example.demo.domain.playlist.dto.playlistdto;

import com.example.demo.domain.cd.dto.response.CdResponse;
import com.example.demo.domain.playlist.dto.PlaylistGenre;
import com.example.demo.domain.playlist.dto.SongDto;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.user.entity.Users;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "메인 페이지 - 플레이리스트 상세 응답 DTO")
public record MainPlaylistDetailResponse(

        @Schema(description = "플레이리스트 ID", example = "101")
        Long playlistId,

        @Schema(description = "플레이리스트 이름", example = "집중할 때 듣는 음악")
        String playlistName,

        @Schema(description = "공개 여부", example = "true")
        boolean isPublic,

        @Schema(description = "플레이리스트에 포함된 곡 목록")
        List<SongDto> songs,

        @Schema(description = "플레이리스트 장르", example = "JAZZ")
        PlaylistGenre genre,

        @Schema(description = "플레이리스트에 포함된 CD 아이템 목록")
        CdResponse cdResponse,

        @Schema(description = "플레이리스트 제작자 ID", example = "user-1234")
        String creatorId,

        @Schema(description = "플레이리스트 제작자 닉네임", example = "기타치는-은하-1234")
        String creatorNickname,

        @Schema(description = "플레이리스트 제작자 프로필 이미지", example = "https://cdn.example.com/profile/user-uuid-1234.jpg")
        String creatorProfileImageUrl

) {
    public static MainPlaylistDetailResponse from(Playlist playlist, List<SongDto> songs, CdResponse cdResponse) {
        Users creator = playlist.getUsers();
        return new MainPlaylistDetailResponse(
                playlist.getId(),
                playlist.getName(),
                playlist.isPublic(),
                songs,
                playlist.getGenre(),
                cdResponse,
                creator.getId(),
                creator.getUsername(),
                creator.getProfileUrl() // 또는 getProfileImageUrl()
        );
    }
}
