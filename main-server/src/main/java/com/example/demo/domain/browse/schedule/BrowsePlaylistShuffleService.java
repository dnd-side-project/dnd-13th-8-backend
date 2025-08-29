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
import com.example.demo.domain.representative.repository.RepresentativePlaylistRepository;
import com.example.demo.domain.song.entity.Song;
import com.example.demo.domain.song.repository.SongRepository;
import com.example.demo.domain.song.util.DurationFormatUtil;
import com.example.demo.domain.user.entity.Users;
import com.example.demo.domain.user.repository.UsersRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.*;

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

//    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
//    @Transactional
//    public void scheduledShuffle() {
//
//        List<String> userIds = representativePlaylistRepository.findUserIdsWithRepPlaylist();
//        int userCount = userIds.size();
//
//        if (userCount == 0) {
//            log.warn("대표 플레이리스트가 있는 유저가 없습니다.");
//            return;
//        }
//
//        List<Long> playlistIds = representativePlaylistRepository.findAllPlaylistIdsInOrder(userIds);
//
//        if (playlistIds.size() != userCount) {
//            log.error("대표 플레이리스트 개수와 유저 수가 일치하지 않습니다. userCount={}, playlistCount={}", userCount, playlistIds.size());
//            return;
//        }
//
//        for (int i = 0; i < userCount; i++) {
//            String userId = userIds.get(i);
//
//            List<Long> assignedPlaylistIds = new ArrayList<>();
//            for (int j = 1; j < userCount; j++) {
//                int targetIndex = (i + j) % userCount;
//                assignedPlaylistIds.add(playlistIds.get(targetIndex));
//            }
//
//            try {
//                assignShuffledCards(userId, assignedPlaylistIds);
//            } catch (Exception e) {
//                log.error("셔플 실패: userId={}, error={}", userId, e.getMessage(), e);
//            }
//        }
//
//        log.info("BrowsePlaylistCard 셔플 완료 - 대상 유저 수: {}", userCount);
//    }
//
//    @Transactional
//    public void assignShuffledCards(String userId, List<Long> assignedPlaylistIds) {
//        browseSnapshotRepository.deleteByUserId(userId);
//
//        Users user = usersRepository.findById(userId)
//                .orElseThrow(() -> new IllegalArgumentException("유저 없음: " + userId));
//
//        Map<Long, List<CdItemResponse>> cdMap = getCdMap(assignedPlaylistIds);
//        List<BrowsePlaylistCard> cards = new ArrayList<>();
//
//        for (int i = 0; i < assignedPlaylistIds.size(); i++) {
//            Long playlistId = assignedPlaylistIds.get(i);
//            Playlist playlist = playlistRepository.findById(playlistId)
//                    .orElseThrow(() -> new IllegalArgumentException("플레이리스트 없음: " + playlistId));
//
//            List<Song> songs = songRepository.findByPlaylistId(playlistId);
//            String songsJson;
//            try {
//                songsJson = objectMapper.writeValueAsString(songs.stream().map(SongDto::from).toList());
//            } catch (JsonProcessingException e) {
//                log.warn("곡 JSON 변환 실패: playlistId={}, userId={}", playlistId, userId, e);
//                continue;
//            }
//
//            String cdJson;
//            try {
//                cdJson = objectMapper.writeValueAsString(cdMap.getOrDefault(playlistId, List.of()));
//            } catch (JsonProcessingException e) {
//                log.warn("CD JSON 변환 실패: playlistId={}, userId={}", playlistId, userId, e);
//                continue;
//            }
//
//            BrowsePlaylistCard card = BrowsePlaylistCard.builder()
//                    .userId(userId)
//                    .playlistId(playlistId)
//                    .position(i)
//                    .playlistTitle(playlist.getName())
//                    .genre(playlist.getGenre().name())
//                    .creatorId(playlist.getUsers().getId())
//                    .creatorName(playlist.getUsers().getUsername())
//                    .songsJson(songsJson)
//                    .cdItemsJson(cdJson)
//                    .isRepresentative(true)
//                    .shareUrl("https://deulak.com/share/" + user.getShareCode())
//                    .totalTime(DurationFormatUtil.formatToHumanReadable(
//                            songs.stream().mapToLong(Song::getYoutubeLength).sum()
//                    ))
//                    .build();
//
//            cards.add(card);
//        }
//
//        browseSnapshotRepository.saveAll(cards);
//        log.info("카드 저장 완료: userId={}, 개수={}", userId, cards.size());
//    }
//
//    private Map<Long, List<CdItemResponse>> getCdMap(List<Long> playlistIds) {
//        CdListResponseDto cdList = cdService.getAllCdByPlaylistIdList(playlistIds);
//        Map<Long, List<CdItemResponse>> result = new HashMap<>();
//        for (CdResponse cdResponse : cdList.cds()) {
//            result.put(
//                    cdResponse.playlistId(),
//                    cdResponse.cdItems() != null ? cdResponse.cdItems() : Collections.emptyList()
//            );
//        }
//        return result;
//    }
}
