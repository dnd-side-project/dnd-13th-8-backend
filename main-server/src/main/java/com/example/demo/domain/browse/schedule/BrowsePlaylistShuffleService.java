package com.example.demo.domain.browse.schedule;

import com.example.demo.domain.browse.entity.BrowsePlaylistCard;
import com.example.demo.domain.browse.repository.BrowsePlaylistRepository;
import com.example.demo.domain.cd.dto.response.CdItemResponse;
import com.example.demo.domain.cd.dto.response.CdListResponseDto;
import com.example.demo.domain.cd.dto.response.CdResponse;
import com.example.demo.domain.cd.service.CdService;
import com.example.demo.domain.playlist.dto.SongDto;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.repository.PlaylistRepository;
import com.example.demo.domain.playlist.service.PlaylistMyPageService;
import com.example.demo.domain.representative.entity.RepresentativePlaylist;
import com.example.demo.domain.representative.repository.RepresentativePlaylistRepository;
import com.example.demo.domain.song.entity.Song;
import com.example.demo.domain.song.repository.SongRepository;
import com.example.demo.domain.song.util.DurationFormatUtil;
import com.example.demo.domain.user.entity.Users;
import com.example.demo.domain.user.repository.UsersRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.*;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class BrowsePlaylistShuffleService {

    private final UsersRepository usersRepository;
    private final PlaylistRepository playlistRepository;
    private final RepresentativePlaylistRepository representativePlaylistRepository;
    private final SongRepository songRepository;
    private final CdService cdService;
    private final BrowsePlaylistRepository browseSnapshotRepository;
    private final ObjectMapper objectMapper;
    private final PlaylistMyPageService playlistMyPageService;
    private final TransactionTemplate txTemplate;


    @PostConstruct
    public void scheduleOnceAfter3Min() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                txTemplate.execute(status -> {
                    scheduledShuffle();
                    return null;
                });
            }
        }, Duration.ofMinutes(1).toMillis());
    }



    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    @Transactional
    public void scheduledShuffle() {
        List<String> userIds = usersRepository.findAllUserIds();
        log.info(" 셔플 대상 유저 수: {}", userIds.size());

        for (String userId : userIds) {
            try {
                shuffleAndStore(userId);
            } catch (Exception e) {
                log.error(" 셔플 실패: userId={}", userId, e);
            }
        }

        log.info(" BrowseSnapshot 셔플 완료 - 전체 유저 대상");
    }

    @Transactional
    public void shuffleAndStore(String userId) {
        // 1. 기존 스냅샷 삭제
        browseSnapshotRepository.deleteByUserId(userId);

        // 2. 대표 플레이리스트 ID 셔플
        List<Long> original = representativePlaylistRepository.findAllPlaylistIdsExcludingUser(userId);
        if (original == null || original.isEmpty()) {
            log.warn(" 대표 플레이리스트가 비어있습니다. userId={}", userId);
            return;
        }

        Long limit = representativePlaylistRepository.countByAllUserId();

        List<Long> playlistIds = new ArrayList<>(original);
        long seed = System.nanoTime() ^ UUID.randomUUID().getMostSignificantBits();
        Collections.shuffle(playlistIds, new Random(seed));

        List<Long> selectedIds = playlistIds.stream()
                .limit(limit)
                .toList();

        log.info(" 셔플 완료: userId={}, selectedPlaylistIds={}", userId, selectedIds);

        // 3. CD Map 조회
        Map<Long, List<CdItemResponse>> cdItemsMap = getCdMap(selectedIds);

        // 4. 유저 조회
       usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음: " + userId));

        // 5. 카드 생성
        List<BrowsePlaylistCard> snapshots = new ArrayList<>();
        for (int i = 0; i < selectedIds.size(); i++) {
            Long playlistId = selectedIds.get(i);
            Playlist playlist = playlistRepository.findById(playlistId)
                    .orElseThrow(() -> new IllegalArgumentException("플레이리스트 없음: " + playlistId));

            List<Song> songs = songRepository.findByPlaylistId(playlistId);
            List<SongDto> songDtos = songs.stream().map(SongDto::from).toList();

            String songsJson;
            try {
                songsJson = objectMapper.writeValueAsString(songDtos);
            } catch (JsonProcessingException e) {
                log.warn(" 곡 JSON 변환 실패: playlistId={}, userId={}", playlistId, userId, e);
                continue;
            }

            String cdItemsJson;
            try {
                List<CdItemResponse> cdItems = cdItemsMap.getOrDefault(playlistId, Collections.emptyList());
                cdItemsJson = objectMapper.writeValueAsString(cdItems);
            } catch (JsonProcessingException e) {
                log.warn(" CD 아이템 JSON 변환 실패: playlistId={}, userId={}", playlistId, userId, e);
                continue;
            }

            long totalSec = songs.stream().mapToLong(Song::getYoutubeLength).sum();

            BrowsePlaylistCard snapshot = BrowsePlaylistCard.builder()
                    .userId(userId)
                    .playlistId(playlistId)
                    .position(i)
                    .playlistTitle(playlist.getName())
                    .genre(playlist.getGenre().name())
                    .creatorId(playlist.getUsers().getId())
                    .creatorName(playlist.getUsers().getUsername())
                    .songsJson(songsJson)
                    .cdItemsJson(cdItemsJson)
                    .isRepresentative(true)
                    .shareUrl(playlistMyPageService.sharePlaylist(userId))
                    .totalTime(DurationFormatUtil.formatToHumanReadable(totalSec))
                    .build();

            snapshots.add(snapshot);
        }

        // 6. 저장
        browseSnapshotRepository.saveAll(snapshots);
        log.info(" 저장 완료: userId={}, 저장된 스냅샷 개수={}", userId, snapshots.size());
    }

    private Map<Long, List<CdItemResponse>> getCdMap(List<Long> playlistIds) {
        CdListResponseDto cdList = cdService.getAllCdByPlaylistIdList(playlistIds);
        Map<Long, List<CdItemResponse>> result = new HashMap<>();
        for (CdResponse cdResponse : cdList.cds()) {
            result.put(
                    cdResponse.playlistId(),
                    cdResponse.cdItems() != null ? cdResponse.cdItems() : Collections.emptyList()
            );
        }
        return result;
    }
}
