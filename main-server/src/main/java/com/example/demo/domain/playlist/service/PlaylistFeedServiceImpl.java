package com.example.demo.domain.playlist.service;

import com.example.common.error.code.UserErrorCode;
import com.example.common.error.exception.UserException;
import com.example.demo.domain.cd.service.CdService;

import com.example.demo.domain.like.repository.LikesRepository;
import com.example.demo.domain.playlist.dto.common.PlaylistCoverResponse;
import com.example.demo.domain.playlist.dto.common.PlaylistSortOption;
import com.example.demo.domain.playlist.dto.feed.PlaylistCursor;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.repository.PlaylistFeedRepository;
import com.example.demo.domain.playlist.repository.PlaylistRepository;
import com.example.demo.domain.user.entity.Users;
import com.example.demo.domain.user.repository.UsersRepository;
import com.example.demo.global.paging.CursorPageResponse;
import com.example.demo.global.paging.PlaylistCursorCodec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class PlaylistFeedServiceImpl implements PlaylistFeedService{

    private final PlaylistFeedRepository playlistFeedRepository;
    private final UsersRepository usersRepository;
    private final LikesRepository likesRepository;
    private final CdService cdService;
    private final PlaylistRepository playlistRepository;

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<PlaylistCoverResponse, String> getPlaylistsSorted(
            String shareCode,
            String meId,
            PlaylistSortOption sortOption,
            String opaqueCursor,
            int limit
    ) {
        Users owner = usersRepository.findByShareCode(shareCode)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        String ownerId = owner.getId();
        boolean includePrivate = ownerId.equals(meId);

        PlaylistCursor decodedCursor = PlaylistCursorCodec.decode(opaqueCursor, sortOption);

        List<Playlist> fetched = playlistFeedRepository.findFeedPlaylists(
                ownerId,
                decodedCursor,
                limit,
                sortOption,
                includePrivate
        );

        boolean hasNext = fetched.size() > limit;

        List<Playlist> page = hasNext ? fetched.subList(0, limit) : fetched;

        List<Long> playlistIds = page.stream()
                .map(Playlist::getId)
                .toList();

        Set<Long> likedSet = new HashSet<>(likesRepository.findLikedPlaylistIdsIn(meId, playlistIds));

        List<PlaylistCoverResponse> content = page.stream()
                .map(p -> PlaylistCoverResponse.from(
                        p,
                        cdService.getOnlyCdByPlaylistId(p.getId()),
                        likedSet.contains(p.getId())
                ))
                .toList();

        String nextCursor = null;
        if (hasNext && !page.isEmpty()) {
            Playlist last = page.get(page.size() - 1);
            PlaylistCursor next = switch (sortOption) {
                case RECENT -> new PlaylistCursor(last.getId(), null);
                case POPULAR -> new PlaylistCursor(last.getId(), last.getVisitCount());
            };
            nextCursor = PlaylistCursorCodec.encode(sortOption, next);
        }

        long totalCount = playlistRepository.countByUsers_Id(ownerId);

        return new CursorPageResponse<>(
                content,
                nextCursor,
                content.size(),
                hasNext,
                totalCount
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<PlaylistCoverResponse, String> getLikedPlaylistsSorted(
            String shareCode,
            String meId,
            PlaylistSortOption sortOption,
            String opaqueCursor,
            int limit
    ) {

        Users owner = usersRepository.findByShareCode(shareCode)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        String ownerId = owner.getId();
        boolean includePrivate = ownerId.equals(meId);

        PlaylistCursor decodedCursor = PlaylistCursorCodec.decode(opaqueCursor, sortOption);

        List<Playlist> fetched = playlistFeedRepository.findLikedPlaylists(
                ownerId,
                sortOption,
                decodedCursor,
                limit,
                includePrivate
        );

        boolean hasNext = fetched.size() > limit;
        List<Playlist> page = hasNext ? fetched.subList(0, limit) : fetched;

        List<Long> playlistIds = page.stream()
                .map(Playlist::getId)
                .toList();

        Set<Long> likedSetByMe = new HashSet<>(
                likesRepository.findLikedPlaylistIdsIn(meId, playlistIds)
        );

        List<PlaylistCoverResponse> content = page.stream()
                .map(p -> PlaylistCoverResponse.from(
                        p,
                        cdService.getOnlyCdByPlaylistId(p.getId()),
                        likedSetByMe.contains(p.getId())
                ))
                .toList();

        String nextCursor = null;
        if (hasNext && !page.isEmpty()) {
            Playlist last = page.get(page.size() - 1);
            PlaylistCursor next = switch (sortOption) {
                case RECENT -> new PlaylistCursor(last.getId(), null);
                case POPULAR -> new PlaylistCursor(last.getId(), last.getVisitCount());
            };
            nextCursor = PlaylistCursorCodec.encode(sortOption, next);
        }

        long totalCount = likesRepository.countByUsers_Id(ownerId);

        return new CursorPageResponse<>(
                content,
                nextCursor,
                content.size(),
                hasNext,
                totalCount
        );
    }
}
