package com.example.demo.domain.playlist.service.carousel;

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
import com.example.demo.domain.playlist.repository.query.PlaylistCarouselQueryRepository;
import com.example.demo.domain.playlist.repository.command.PlaylistRepository;
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

    private final PlaylistCarouselQueryRepository carouselRepository;
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
        return getAnchorCarousel(
                shareCode,
                meId,
                sort,
                anchorId,
                limit,
                new FeedCarouselStrategy()
        );
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
        return getCarouselMore(
                shareCode,
                meId,
                sort,
                direction,
                cursor,
                limit,
                new FeedCarouselStrategy()
        );
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
        return getAnchorCarousel(
                shareCode,
                meId,
                sort,
                anchorId,
                limit,
                new LikedCarouselStrategy()
        );
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
        return getCarouselMore(
                shareCode,
                meId,
                sort,
                direction,
                cursor,
                limit,
                new LikedCarouselStrategy()
        );
    }

    private BiCursorPageResponse<PlaylistCoverResponse, Long> getAnchorCarousel(
            String shareCode,
            String meId,
            PlaylistSortOption sort,
            Long anchorId,
            int limit,
            CarouselStrategy strategy
    ) {
        OwnerContext context = resolveOwnerContext(shareCode, meId);

        Playlist anchor = strategy.findAnchor(
                carouselRepository,
                context.ownerId(),
                anchorId,
                context.includePrivate()
        ).orElseThrow(() -> new PlaylistException(PlaylistErrorCode.PLAYLIST_NOT_FOUND));

        PlaylistCursor anchorCursor = toCursor(anchor, sort);

        SliceResult prevSlice = fetchSlice(
                context,
                sort,
                limit,
                CarouselDirection.PREV,
                anchorCursor,
                strategy
        );

        SliceResult nextSlice = fetchSlice(
                context,
                sort,
                limit,
                CarouselDirection.NEXT,
                anchorCursor,
                strategy
        );

        List<Playlist> merged = mergeAroundAnchor(
                prevSlice.page(),
                anchor,
                nextSlice.page()
        );

        Long prevCursor = resolvePrevCursor(prevSlice.page(), prevSlice.hasMore());
        Long nextCursor = resolveNextCursor(nextSlice.page(), nextSlice.hasMore());

        return toBiCursorResponse(
                meId,
                merged,
                prevCursor,
                nextCursor,
                prevSlice.hasMore(),
                nextSlice.hasMore()
        );
    }

    private BiCursorPageResponse<PlaylistCoverResponse, Long> getCarouselMore(
            String shareCode,
            String meId,
            PlaylistSortOption sort,
            CarouselDirection direction,
            Long cursor,
            int limit,
            CarouselStrategy strategy
    ) {
        CarouselDirection resolvedDirection =
                cursor == null ? CarouselDirection.NEXT : direction;

        OwnerContext context = resolveOwnerContext(shareCode, meId);
        PlaylistCursor decodedCursor = decodeCursor(cursor, sort);

        SliceResult slice = fetchSlice(
                context,
                sort,
                limit,
                resolvedDirection,
                decodedCursor,
                strategy
        );

        CursorState cursorState = toCursorState(resolvedDirection, slice);

        return toBiCursorResponse(
                meId,
                slice.page(),
                cursorState.prevCursor(),
                cursorState.nextCursor(),
                cursorState.hasPrev(),
                cursorState.hasNext()
        );
    }

    private SliceResult fetchSlice(
            OwnerContext context,
            PlaylistSortOption sort,
            int limit,
            CarouselDirection direction,
            PlaylistCursor cursor,
            CarouselStrategy strategy
    ) {
        List<Playlist> fetched = strategy.findCarousel(
                carouselRepository,
                context.ownerId(),
                cursor,
                limit,
                sort,
                context.includePrivate(),
                direction
        );

        boolean hasMore = fetched.size() > limit;
        List<Playlist> page = hasMore ? new ArrayList<>(fetched.subList(0, limit)) : new ArrayList<>(fetched);

        if (direction == CarouselDirection.PREV) {
            Collections.reverse(page);
        }

        return new SliceResult(page, hasMore);
    }

    private List<Playlist> mergeAroundAnchor(
            List<Playlist> prevPage,
            Playlist anchor,
            List<Playlist> nextPage
    ) {
        List<Playlist> merged = new ArrayList<>(prevPage.size() + 1 + nextPage.size());
        merged.addAll(prevPage);
        merged.add(anchor);
        merged.addAll(nextPage);
        return merged;
    }

    private CursorState toCursorState(CarouselDirection direction, SliceResult slice) {
        return direction == CarouselDirection.PREV
                ? new CursorState(
                resolvePrevCursor(slice.page(), slice.hasMore()),
                null,
                slice.hasMore(),
                false
        )
                : new CursorState(
                null,
                resolveNextCursor(slice.page(), slice.hasMore()),
                false,
                slice.hasMore()
        );
    }

    private Long resolvePrevCursor(List<Playlist> page, boolean hasPrev) {
        return hasPrev && !page.isEmpty()
                ? page.get(0).getId()
                : null;
    }

    private Long resolveNextCursor(List<Playlist> page, boolean hasNext) {
        return hasNext && !page.isEmpty()
                ? page.get(page.size() - 1).getId()
                : null;
    }

    private OwnerContext resolveOwnerContext(String shareCode, String meId) {
        Users owner = usersRepository.findByShareCode(shareCode)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        return new OwnerContext(owner.getId(), owner.getId().equals(meId));
    }

    private PlaylistCursor decodeCursor(Long cursorId, PlaylistSortOption sort) {
        if (cursorId == null) {
            return null;
        }

        Playlist pivot = playlistRepository.findById(cursorId)
                .orElseThrow(() -> new PlaylistException(PlaylistErrorCode.PLAYLIST_NOT_FOUND));

        return toCursor(pivot, sort);
    }

    private PlaylistCursor toCursor(Playlist playlist, PlaylistSortOption sort) {
        return switch (sort) {
            case RECENT -> new PlaylistCursor(playlist.getId(), null);
            case POPULAR -> new PlaylistCursor(playlist.getId(), playlist.getVisitCount());
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
        List<Long> playlistIds = page.stream()
                .map(Playlist::getId)
                .toList();

        Set<Long> likedSet = playlistIds.isEmpty()
                ? Collections.emptySet()
                : new HashSet<>(likesRepository.findLikedPlaylistIdsIn(meId, playlistIds));

        CdItemsByPlaylist cdItemsByPlaylist = playlistIds.isEmpty()
                ? CdItemsByPlaylist.empty()
                : cdService.findCdItemsByPlaylistIdsIn(playlistIds);

        List<PlaylistCoverResponse> content = page.stream()
                .map(playlist -> PlaylistCoverResponse.from(
                        playlist,
                        cdItemsByPlaylist.cdItemsOf(playlist.getId()),
                        likedSet.contains(playlist.getId())
                ))
                .toList();

        return new BiCursorPageResponse<>(
                content,
                prevCursor,
                nextCursor,
                content.size(),
                hasPrev,
                hasNext
        );
    }

    private record OwnerContext(String ownerId, boolean includePrivate) {}
    private record SliceResult(List<Playlist> page, boolean hasMore) {}
    private record CursorState(Long prevCursor, Long nextCursor, boolean hasPrev, boolean hasNext) {}

    private interface CarouselStrategy {
        java.util.Optional<Playlist> findAnchor(
                PlaylistCarouselQueryRepository repository,
                String ownerId,
                Long anchorId,
                boolean includePrivate
        );

        List<Playlist> findCarousel(
                PlaylistCarouselQueryRepository repository,
                String ownerId,
                PlaylistCursor cursor,
                int limit,
                PlaylistSortOption sort,
                boolean includePrivate,
                CarouselDirection direction
        );
    }

    private static class FeedCarouselStrategy implements CarouselStrategy {
        @Override
        public java.util.Optional<Playlist> findAnchor(
                PlaylistCarouselQueryRepository repository,
                String ownerId,
                Long anchorId,
                boolean includePrivate
        ) {
            return repository.findFeedAnchor(ownerId, anchorId, includePrivate);
        }

        @Override
        public List<Playlist> findCarousel(
                PlaylistCarouselQueryRepository repository,
                String ownerId,
                PlaylistCursor cursor,
                int limit,
                PlaylistSortOption sort,
                boolean includePrivate,
                CarouselDirection direction
        ) {
            return repository.findFeedCarousel(
                    ownerId, cursor, limit, sort, includePrivate, direction
            );
        }
    }

    private static class LikedCarouselStrategy implements CarouselStrategy {
        @Override
        public java.util.Optional<Playlist> findAnchor(
                PlaylistCarouselQueryRepository repository,
                String ownerId,
                Long anchorId,
                boolean includePrivate
        ) {
            return repository.findLikedAnchor(ownerId, anchorId, includePrivate);
        }

        @Override
        public List<Playlist> findCarousel(
                PlaylistCarouselQueryRepository repository,
                String ownerId,
                PlaylistCursor cursor,
                int limit,
                PlaylistSortOption sort,
                boolean includePrivate,
                CarouselDirection direction
        ) {
            return repository.findLikedCarousel(
                    ownerId, cursor, limit, sort, includePrivate, direction
            );
        }
    }
}