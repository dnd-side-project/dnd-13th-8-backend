package com.example.demo.domain.browse.service;

import com.example.common.error.code.UserErrorCode;
import com.example.common.error.exception.UserException;
import com.example.demo.domain.browse.dto.BrowsePlaylistDto;
import com.example.demo.domain.browse.dto.BrowsePlaylistMapper;
import com.example.demo.domain.browse.dto.BrowseResponse;
import com.example.demo.domain.cd.dto.response.CdItemResponse;
import com.example.demo.domain.cd.dto.response.CdListResponseDto;
import com.example.demo.domain.cd.dto.response.CdResponse;
import com.example.demo.domain.cd.service.CdService;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.recommendation.entity.UserPlaylistHistory;
import com.example.demo.domain.recommendation.repository.UserPlaylistHistoryRepository;
import com.example.demo.domain.representative.entity.RepresentativePlaylist;
import com.example.demo.domain.representative.repository.RepresentativePlaylistRepository;
import com.example.demo.domain.song.entity.Song;
import com.example.demo.domain.song.repository.SongRepository;
import com.example.demo.domain.song.util.DurationFormatUtil;
import com.example.demo.domain.user.entity.Users;
import com.example.demo.domain.user.repository.UsersRepository;
import com.example.demo.global.r2.R2Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
public class BrowsePlaylistServiceImpl implements BrowsePlaylistService {

    private static final String REDIS_KEY_PREFIX = "BROWSE_SHUFFLED:";

    private final StringRedisTemplate redisTemplate;
    private final RepresentativePlaylistRepository representativePlaylistRepository;
    private final UsersRepository usersRepository;
    private final CdService cdService;
    private final R2Service r2Service;
    private final SongRepository songRepository;
    private final UserPlaylistHistoryRepository userPlaylistHistoryRepository;

    @Override
    public BrowseResponse getShuffledPlaylists(String userId, int cursor, int size) {
        List<Long> shuffledIds = getShuffledPlaylistIdsFromRedis(userId);
        List<Long> pagedIds = getPagedPlaylistIds(shuffledIds, cursor, size);

        if (pagedIds.isEmpty()) {
            return new BrowseResponse(List.of(), -1, false);
        }

        List<RepresentativePlaylist> representativePlaylists = getValidPlaylistsWithSongs(pagedIds);
        Map<Long, CdItemResponse> cdMap = getCdMap(pagedIds);
        Users user = getUser(userId);

        List<BrowsePlaylistDto> results = new ArrayList<>();
        for (RepresentativePlaylist rep : representativePlaylists) {
            Playlist playlist = rep.getPlaylist();

            //  Playlist ID 기준으로 곡 리스트 조회
            List<Song> songs = songRepository.findByPlaylistId(playlist.getId());

            CdItemResponse cdItem = cdMap.get(playlist.getId());
            String shareUrl = generateShareUrl(user.getId());
            String totalTime = calculateTotalDuration(songs);

            BrowsePlaylistDto dto = BrowsePlaylistMapper.toDto(
                    playlist,
                    songs,       // 외부에서 조회한 곡 리스트 전달
                    cdItem,
                    shareUrl,
                    totalTime
            );

            playlist.increaseVisitCount(); // 조회수 증가
            UserPlaylistHistory history = UserPlaylistHistory.of(user, playlist);
            userPlaylistHistoryRepository.save(history); // 재생 기록 저장
            results.add(dto);
        }

        int nextCursor = (cursor + size < shuffledIds.size()) ? cursor + size : -1;
        boolean hasNext = nextCursor != -1;

        return new BrowseResponse(results, nextCursor, hasNext);
    }

    private List<Long> getShuffledPlaylistIdsFromRedis(String userId) {
        String key = REDIS_KEY_PREFIX + userId;
        List<String> raw = redisTemplate.opsForList().range(key, 0, -1);
        if (raw == null) return List.of();
        return raw.stream().map(Long::parseLong).toList();
    }

    private List<Long> getPagedPlaylistIds(List<Long> ids, int cursor, int size) {
        int end = Math.min(cursor + size, ids.size());
        return ids.subList(cursor, end);
    }

    private List<RepresentativePlaylist> getValidPlaylistsWithSongs(List<Long> ids) {
        return representativePlaylistRepository.findAllById(ids);
    }

    private Users getUser(String userId) {
        return usersRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    }

    private Map<Long, CdItemResponse> getCdMap(List<Long> playlistIds) {
        CdListResponseDto cdList = cdService.getAllCdByPlaylistIdList(playlistIds);
        Map<Long, CdItemResponse> result = new HashMap<>();

        for (CdResponse cdResponse : cdList.cds()) {
            Long playlistId = cdResponse.playlistId();
            CdItemResponse cdItem = null;

            if (cdResponse.cdItems() != null && !cdResponse.cdItems().isEmpty()) {
                cdItem = cdResponse.cdItems().get(0); // presigned 포함되어 있다고 가정
            }

            result.put(playlistId, cdItem);
        }

        return result;
    }

    private String calculateTotalDuration(List<Song> songs) {
        long total = 0L;
        for (Song song : songs) {
            total += song.getYoutubeLength();
        }
        return DurationFormatUtil.formatToHumanReadable(total);
    }

    private String generateShareUrl(String userId) {
        return "https://deulak.com/share/" + userId;
    }
}
