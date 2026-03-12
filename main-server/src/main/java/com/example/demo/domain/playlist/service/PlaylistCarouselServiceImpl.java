package com.example.demo.domain.playlist.service;

import com.example.common.error.code.PlaylistErrorCode;
import com.example.common.error.code.UserErrorCode;
import com.example.common.error.exception.PlaylistException;
import com.example.common.error.exception.UserException;
import com.example.demo.domain.cd.dto.response.CdItemsByPlaylist;
import com.example.demo.domain.cd.service.CdService;
import com.example.demo.domain.like.repository.LikesRepository;
import com.example.demo.domain.playlist.dto.common.PlaylistCoverResponse;
import com.example.demo.domain.playlist.dto.common.PlaylistSortOption;
import com.example.demo.domain.playlist.dto.feed.CarouselDirection;
import com.example.demo.domain.playlist.dto.feed.PlaylistCursor;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.repository.PlaylistCarouselRepository;
import com.example.demo.domain.playlist.repository.PlaylistRepository;
import com.example.demo.domain.user.entity.Users;
import com.example.demo.domain.user.repository.UsersRepository;
import com.example.demo.global.paging.BiCursorPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@RequiredArgsConstructor
@Service
public class PlaylistCarouselServiceImpl implements PlaylistCarouselService {

    private final PlaylistCarouselRepository carouselRepository;
    private final UsersRepository usersRepository;
    private final LikesRepository likesRepository;
    private final CdService cdService;
    private final PlaylistRepository playlistRepository;

    @Override
    @Transactional(readOnly = true)
    public BiCursorPageResponse<PlaylistCoverResponse, Long> getFeedCarousel(
            String shareCode,
            String meId,
            PlaylistSortOption sort,
            Long anchorId,
            int limit
    ) {
        Users owner = usersRepository.findByShareCode(shareCode)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        String ownerId = owner.getId();
        boolean includePrivate = ownerId.equals(meId);

        Playlist anchor = carouselRepository.findFeedAnchor(ownerId, anchorId, includePrivate)
                .orElseThrow(() -> new PlaylistException(PlaylistErrorCode.PLAYLIST_NOT_FOUND));

        PlaylistCursor anchorCursor = toCursor(anchor, sort);

        List<Playlist> prevFetched = carouselRepository.findFeedCarousel(
                ownerId, anchorCursor, limit, sort, includePrivate, CarouselDirection.PREV
        );
        List<Playlist> nextFetched = carouselRepository.findFeedCarousel(
                ownerId, anchorCursor, limit, sort, includePrivate, CarouselDirection.NEXT
        );

        boolean hasPrev = prevFetched.size() > limit;
        boolean hasNext = nextFetched.size() > limit;

        List<Playlist> prevPage = hasPrev ? prevFetched.subList(0, limit) : prevFetched;
        List<Playlist> nextPage = hasNext ? nextFetched.subList(0, limit) : nextFetched;

        List<Playlist> merged = new ArrayList<>(prevPage.size() + 1 + nextPage.size());
        Collections.reverse(prevPage);
        merged.addAll(prevPage);
        merged.add(anchor);
        merged.addAll(nextPage);

        Long prevCursor = hasPrev && !prevPage.isEmpty() ? prevPage.get(0).getId() : null;
        Long nextCursor = hasNext && !nextPage.isEmpty() ? nextPage.get(nextPage.size() - 1).getId() : null;

        return toBiCursorResponse(meId, merged, prevCursor, nextCursor, hasPrev, hasNext);
    }

    @Override
    @Transactional(readOnly = true)
    public BiCursorPageResponse<PlaylistCoverResponse, Long> getFeedCarouselMore(
            String shareCode,
            String meId,
            PlaylistSortOption sort,
            CarouselDirection direction,
            Long cursor,
            int limit
    ) {
        CarouselDirection resolvedDirection =
                cursor == null ? CarouselDirection.NEXT : direction;

        Users owner = usersRepository.findByShareCode(shareCode)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        String ownerId = owner.getId();
        boolean includePrivate = ownerId.equals(meId);

        PlaylistCursor decoded = decodeCursor(cursor, sort);

        List<Playlist> fetched = carouselRepository.findFeedCarousel(
                ownerId, decoded, limit, sort, includePrivate, resolvedDirection
        );

        boolean hasMore = fetched.size() > limit;
        List<Playlist> page = hasMore ? fetched.subList(0, limit) : fetched;

        if (resolvedDirection == CarouselDirection.PREV) {
            Collections.reverse(page);
        }

        Long prevCursor = null;
        Long nextCursor = null;
        boolean hasPrev = false;
        boolean hasNext = false;

        if (resolvedDirection == CarouselDirection.PREV) {
            hasPrev = hasMore;
            if (hasPrev && !page.isEmpty()) prevCursor = page.get(0).getId();
        } else {
            hasNext = hasMore;
            if (hasNext && !page.isEmpty()) nextCursor = page.get(page.size() - 1).getId();
        }

        return toBiCursorResponse(meId, page, prevCursor, nextCursor, hasPrev, hasNext);
    }

    @Override
    @Transactional(readOnly = true)
    public BiCursorPageResponse<PlaylistCoverResponse, Long> getLikedCarousel(
            String shareCode,
            String meId,
            PlaylistSortOption sort,
            Long anchorId,
            int limit
    ) {
        Users owner = usersRepository.findByShareCode(shareCode)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        String ownerId = owner.getId();
        boolean includePrivate = ownerId.equals(meId);

        Playlist anchor = carouselRepository.findLikedAnchor(ownerId, anchorId, includePrivate)
                .orElseThrow(() -> new PlaylistException(PlaylistErrorCode.PLAYLIST_NOT_FOUND));

        PlaylistCursor anchorCursor = toCursor(anchor, sort);

        List<Playlist> prevFetched = carouselRepository.findLikedCarousel(
                ownerId, anchorCursor, limit, sort, includePrivate, CarouselDirection.PREV
        );
        List<Playlist> nextFetched = carouselRepository.findLikedCarousel(
                ownerId, anchorCursor, limit, sort, includePrivate, CarouselDirection.NEXT
        );

        boolean hasPrev = prevFetched.size() > limit;
        boolean hasNext = nextFetched.size() > limit;

        List<Playlist> prevPage = hasPrev ? prevFetched.subList(0, limit) : prevFetched;
        List<Playlist> nextPage = hasNext ? nextFetched.subList(0, limit) : nextFetched;

        List<Playlist> merged = new ArrayList<>(prevPage.size() + 1 + nextPage.size());
        Collections.reverse(prevPage);
        merged.addAll(prevPage);
        merged.add(anchor);
        merged.addAll(nextPage);

        Long prevCursor = hasPrev && !prevPage.isEmpty() ? prevPage.get(0).getId() : null;
        Long nextCursor = hasNext && !nextPage.isEmpty() ? nextPage.get(nextPage.size() - 1).getId() : null;

        return toBiCursorResponse(meId, merged, prevCursor, nextCursor, hasPrev, hasNext);
    }

    @Override
    @Transactional(readOnly = true)
    public BiCursorPageResponse<PlaylistCoverResponse, Long> getLikedCarouselMore(
            String shareCode,
            String meId,
            PlaylistSortOption sort,
            CarouselDirection direction,
            Long cursor,
            int limit
    ) {

        CarouselDirection resolvedDirection =
                cursor == null ? CarouselDirection.NEXT : direction;

        Users owner = usersRepository.findByShareCode(shareCode)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        String ownerId = owner.getId();
        boolean includePrivate = ownerId.equals(meId);

        PlaylistCursor decoded = decodeCursor(cursor, sort);

        List<Playlist> fetched = carouselRepository.findLikedCarousel(
                ownerId, decoded, limit, sort, includePrivate, resolvedDirection
        );

        boolean hasMore = fetched.size() > limit;
        List<Playlist> page = hasMore ? fetched.subList(0, limit) : fetched;

        if (resolvedDirection == CarouselDirection.PREV) {
            Collections.reverse(page);
        }

        Long prevCursor = null;
        Long nextCursor = null;
        boolean hasPrev = false;
        boolean hasNext = false;

        if (resolvedDirection == CarouselDirection.PREV) {
            hasPrev = hasMore;
            if (hasPrev && !page.isEmpty()) prevCursor = page.get(0).getId();
        } else {
            hasNext = hasMore;
            if (hasNext && !page.isEmpty()) nextCursor = page.get(page.size() - 1).getId();
        }

        return toBiCursorResponse(meId, page, prevCursor, nextCursor, hasPrev, hasNext);
    }

    private PlaylistCursor decodeCursor(Long cursorId, PlaylistSortOption sort) {
        if (cursorId == null) return null;

        Playlist pivot = playlistRepository.findById(cursorId)
                .orElseThrow(() -> new PlaylistException(PlaylistErrorCode.PLAYLIST_NOT_FOUND));

        return toCursor(pivot, sort);
    }

    private PlaylistCursor toCursor(Playlist p, PlaylistSortOption sort) {
        return switch (sort) {
            case RECENT -> new PlaylistCursor(p.getId(), null);
            case POPULAR -> new PlaylistCursor(p.getId(), p.getVisitCount());
        };
    }

    private BiCursorPageResponse<PlaylistCoverResponse, Long> toBiCursorResponse(
            String meId,
            List<Playlist> page,
            Long prevCursor,
            Long nextCursor,
            boolean hasPrev,
            boolean hasNext
    ) {
        List<Long> playlistIds = page.stream().map(Playlist::getId).toList();

        Set<Long> likedSet = playlistIds.isEmpty()
                ? Collections.emptySet()
                : new HashSet<>(likesRepository.findLikedPlaylistIdsIn(meId, playlistIds));

        CdItemsByPlaylist cdItemsByPlaylist = playlistIds.isEmpty()
                ? CdItemsByPlaylist.empty()
                : cdService.findCdItemsByPlaylistIdsIn(playlistIds);

        List<PlaylistCoverResponse> content = page.stream()
                .map(p -> PlaylistCoverResponse.from(
                        p,
                        cdItemsByPlaylist.cdItemsOf(p.getId()),
                        likedSet.contains(p.getId())
                ))
                .toList();

        return new BiCursorPageResponse<>(content, prevCursor, nextCursor, content.size(), hasPrev, hasNext);
    }
}