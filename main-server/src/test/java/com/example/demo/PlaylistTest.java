package com.example.demo;


import com.example.demo.domain.cd.dto.request.CdItemRequest;
import com.example.demo.domain.cd.dto.request.SaveCdRequest;
import com.example.demo.domain.playlist.dto.save.PlaylistDraft;
import com.example.demo.domain.playlist.dto.common.PlaylistGenre;
import com.example.demo.domain.playlist.dto.save.SavePlaylistRequest;
import com.example.demo.domain.playlist.service.PlaylistService;
import com.example.demo.domain.song.dto.YouTubeVideoInfoDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Transactional
class PlaylistTest {

    @Autowired
    PlaylistService playlistService;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    private String createdDraftId;

    private static final String PREFIX = "playlist:draft:";

    @AfterEach
    void cleanupRedis() {
        if (createdDraftId != null) {
            stringRedisTemplate.delete(PREFIX + createdDraftId);
        }
    }

    @Test
    @DisplayName("Draft 저장")
    void saveDraft_success() {
        // given
        PlaylistDraft draft = createDraft();

        // when
        createdDraftId = playlistService.saveDraftPlaylist(draft);

        // then
        assertThat(createdDraftId).isNotBlank();
    }

    @Test
    @DisplayName("Draft 저장 후 Final 저장 성공")
    void saveFinal_success() {
        // given
        PlaylistDraft draft = createDraft();
        createdDraftId = playlistService.saveDraftPlaylist(draft);

        String userId = "ADMIN";
        // when
        var response = playlistService.saveFinalPlaylist(userId, createdDraftId);

        // then
        assertThat(response).isNotNull();
    }

    private PlaylistDraft createDraft() {

        SavePlaylistRequest savePlaylistRequest =
                new SavePlaylistRequest(
                        "테스트 플레이리스트",
                        PlaylistGenre.SLEEP,
                        true,
                        List.of(
                                new YouTubeVideoInfoDto(
                                        "https://youtube.com/watch?v=test",
                                        "테스트 영상",
                                        "https://img.youtube.com/test.jpg",
                                        "03:21",
                                        1L
                                )
                        )
                );

        SaveCdRequest saveCdRequest =
                new SaveCdRequest(
                        List.of(
                                new CdItemRequest(
                                        1001L,   // propId
                                        10L,  // x
                                        20L,  // y
                                        0L,   // z
                                        100L, // height
                                        100L, // width
                                        1L,   // scale
                                        0L    // angle
                                )
                        )
                );

        return new PlaylistDraft(savePlaylistRequest, saveCdRequest);
    }
}