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
import com.example.demo.domain.representative.repository.RepresentativePlaylistRepository;
import com.example.demo.domain.song.entity.Song;
import com.example.demo.domain.song.repository.SongRepository;
import com.example.demo.domain.song.util.DurationFormatUtil;
import com.example.demo.domain.user.entity.Users;
import com.example.demo.domain.user.repository.UsersRepository;
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
    private final PlaylistMyPageService playlistMyPageService;

    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    @Transactional
    public void scheduledShuffle() {
        // 1. 대표 플레이리스트 보유한 유저만 조회
        List<String> userIds = representativePlaylistRepository.findUserIdsWithRepPlaylist();
        List<Long> playlistIds = representativePlaylistRepository.findAllPlaylistIdsInOrder(userIds);

        int n = userIds.size();

        // 2. 대표 플리 없는 유저 존재 시 → 제외되고 셔플 자체를 수행하지 않음
        if (n == 0 || playlistIds.size() < n) {
            log.warn("대표 플레이리스트 정보 부족 또는 유저-플리 매핑 불일치 (userIds={}, playlistIds={})", n, playlistIds.size());
            return;
        }

        // 3. position 중복 없이 유효한 셔플 매트릭스 생성
        List<List<Integer>> validMatrix = findValidShuffleMatrix(n);
        if (validMatrix == null) {
            log.error("position 중복 없는 유효 매트릭스 없음");
            return;
        }

        // 4. 유저별로 셔플된 playlistId 할당 및 카드 저장
        for (int i = 0; i < n; i++) {
            String userId = userIds.get(i);
            List<Long> assigned = new ArrayList<>();
            for (int pos = 0; pos < n - 1; pos++) {
                int playlistIndex = validMatrix.get(i).get(pos);
                assigned.add(playlistIds.get(playlistIndex));
            }
            try {
                assignShuffledCards(userId, assigned);
            } catch (Exception e) {
                log.error("셔플 실패: userId={}, error={}", userId, e.getMessage(), e);
            }
        }
    }

    private List<List<Integer>> findValidShuffleMatrix(int n) {
        List<List<Integer>> result = new ArrayList<>();
        Set<Integer>[] usedInPosition = new Set[n - 1];
        for (int i = 0; i < n - 1; i++) usedInPosition[i] = new HashSet<>();

        backtrackShuffle(0, n, new ArrayList<>(), usedInPosition, result);
        return result.isEmpty() ? null : result;
    }

    private boolean backtrackShuffle(int userIdx, int n, List<List<Integer>> assignment, Set<Integer>[] used, List<List<Integer>> result) {
        if (userIdx == n) {
            result.addAll(new ArrayList<>(assignment));
            return true;
        }

        List<Integer> candidates = new ArrayList<>();
        for (int i = 0; i < n; i++) if (i != userIdx) candidates.add(i);

        for (List<Integer> perm : permutations(candidates)) {
            boolean valid = true;
            for (int pos = 0; pos < perm.size(); pos++) {
                if (used[pos].contains(perm.get(pos))) {
                    valid = false;
                    break;
                }
            }
            if (!valid) continue;

            for (int pos = 0; pos < perm.size(); pos++) used[pos].add(perm.get(pos));
            assignment.add(perm);

            if (backtrackShuffle(userIdx + 1, n, assignment, used, result)) return true;

            assignment.remove(assignment.size() - 1);
            for (int pos = 0; pos < perm.size(); pos++) used[pos].remove(perm.get(pos));
        }
        return false;
    }

    private List<List<Integer>> permutations(List<Integer> list) {
        List<List<Integer>> result = new ArrayList<>();
        permute(list, 0, result);
        return result;
    }

    private void permute(List<Integer> arr, int k, List<List<Integer>> out) {
        if (k == arr.size()) {
            out.add(new ArrayList<>(arr));
        } else {
            for (int i = k; i < arr.size(); i++) {
                Collections.swap(arr, i, k);
                permute(arr, k + 1, out);
                Collections.swap(arr, i, k);
            }
        }
    }

    // 카드 생성 및 저장 로직 (실제 구현 필요)
    private void assignShuffledCards(String userId, List<Long> assignedPlaylistIds) {
        // 기존 카드 삭제
        browseSnapshotRepository.deleteByUserId(userId);

        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음: " + userId));

        Map<Long, List<CdItemResponse>> cdMap = getCdMap(assignedPlaylistIds);

        List<BrowsePlaylistCard> cards = new ArrayList<>();

        for (int i = 0; i < assignedPlaylistIds.size(); i++) {
            Long playlistId = assignedPlaylistIds.get(i);
            Playlist playlist = playlistRepository.findById(playlistId)
                    .orElseThrow(() -> new IllegalArgumentException("플레이리스트 없음: " + playlistId));

            List<Song> songs = songRepository.findByPlaylistId(playlistId);

            String songsJson;
            try {
                songsJson = objectMapper.writeValueAsString(songs.stream().map(SongDto::from).toList());
            } catch (Exception e) {
                log.warn("곡 JSON 변환 실패: playlistId={}, userId={}", playlistId, userId, e);
                continue;
            }

            String cdJson;
            try {
                cdJson = objectMapper.writeValueAsString(cdMap.getOrDefault(playlistId, List.of()));
            } catch (Exception e) {
                log.warn("CD JSON 변환 실패: playlistId={}, userId={}", playlistId, userId, e);
                continue;
            }

            BrowsePlaylistCard card = BrowsePlaylistCard.builder()
                    .userId(userId)
                    .playlistId(playlistId)
                    .position(i)
                    .playlistTitle(playlist.getName())
                    .genre(playlist.getGenre().name())
                    .creatorId(playlist.getUsers().getId())
                    .creatorName(playlist.getUsers().getUsername())
                    .songsJson(songsJson)
                    .cdItemsJson(cdJson)
                    .isRepresentative(true)
                    .shareUrl(playlistMyPageService.sharePlaylist(userId))
                    .totalTime(DurationFormatUtil.formatToHumanReadable(
                            songs.stream().mapToLong(Song::getYoutubeLength).sum()
                    ))
                    .build();

            cards.add(card);
        }

        browseSnapshotRepository.saveAll(cards);
        log.info("카드 저장 완료: userId={}, 개수={}", userId, cards.size());
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
