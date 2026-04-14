package com.example.demo.domain.playlist.service.feed;

import com.example.common.error.code.PlaylistErrorCode;
import com.example.common.error.code.UserErrorCode;
import com.example.common.error.exception.PlaylistException;
import com.example.common.error.exception.UserException;
import com.example.demo.domain.cd.dto.response.CdItemsByPlaylist;
import com.example.demo.domain.cd.service.CdService;

import com.example.demo.domain.like.repository.LikesRepository;
import com.example.demo.domain.playlist.dto.common.PlaylistCoverResponse;
import com.example.demo.domain.playlist.dto.common.PlaylistSortOption;
import com.example.demo.domain.playlist.dto.feed.PlaylistCursor;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.repository.query.PlaylistFeedQueryRepository;
import com.example.demo.domain.playlist.repository.command.PlaylistRepository;
import com.example.demo.domain.user.entity.Users;
import com.example.demo.domain.user.repository.UsersRepository;
import com.example.demo.global.paging.CursorPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@RequiredArgsConstructor
@Service
public class PlaylistFeedServiceImpl implements PlaylistFeedService {

    private final PlaylistFeedQueryRepository playlistFeedQueryRepository;
    private final UsersRepository usersRepository;
    private final LikesRepository likesRepository;
    private final CdService cdService;
    private final PlaylistRepository playlistRepository;

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<PlaylistCoverResponse, Long> getPlaylistsSorted(
            String shareCode,
            String meId,
            PlaylistSortOption sortOption,
            Long cursor,
            int limit
    ) {
        return getPlaylistPage(
                shareCode,
                meId,
                sortOption,
                cursor,
                limit,
                context -> decodedCursor -> playlistFeedQueryRepository.findFeedPlaylists(
                        context.ownerId(),
                        decodedCursor,
                        limit,
                        sortOption,
                        context.includePrivate()
                ),
                context -> playlistFeedQueryRepository.countFeedPlaylists(
                        context.ownerId(),
                        context.includePrivate()
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<PlaylistCoverResponse, Long> getLikedPlaylistsSorted(
            String shareCode,
            String meId,
            PlaylistSortOption sortOption,
            Long cursor,
            int limit
    ) {
        return getPlaylistPage(
                shareCode,
                meId,
                sortOption,
                cursor,
                limit,
                context -> decodedCursor -> playlistFeedQueryRepository.findLikedPlaylists(
                        context.ownerId(),
                        sortOption,
                        decodedCursor,
                        limit,
                        context.includePrivate()
                ),
                context -> likesRepository.countLikedPlaylists(
                        context.ownerId(),
                        context.includePrivate()
                )
        );
    }

    private CursorPageResponse<PlaylistCoverResponse, Long> getPlaylistPage(
            String shareCode,
            String meId,
            PlaylistSortOption sortOption,
            Long cursor,
            int limit,
            Function<OwnerContext, Function<PlaylistCursor, List<Playlist>>> fetcherFactory,
            Function<OwnerContext, Long> countFetcher
    ) {
        OwnerContext context = resolveOwnerContext(shareCode, meId);
        PlaylistCursor decodedCursor = decodeCursor(cursor, sortOption);

        List<Playlist> fetched = fetcherFactory.apply(context).apply(decodedCursor);
        boolean hasNext = fetched.size() > limit;
        List<Playlist> page = hasNext ? fetched.subList(0, limit) : fetched;

        List<Long> playlistIds = page.stream()
                .map(Playlist::getId)
                .toList();

        Set<Long> likedPlaylistIds = new HashSet<>(
                likesRepository.findLikedPlaylistIdsIn(meId, playlistIds)
        );

        CdItemsByPlaylist cdItemsByPlaylist = cdService.findCdItemsByPlaylistIdsIn(playlistIds);

        List<PlaylistCoverResponse> content = page.stream()
                .map(playlist -> PlaylistCoverResponse.from(
                        playlist,
                        cdItemsByPlaylist.cdItemsOf(playlist.getId()),
                        likedPlaylistIds.contains(playlist.getId())
                ))
                .toList();

        Long nextCursor = hasNext && !page.isEmpty()
                ? page.get(page.size() - 1).getId()
                : null;

        return new CursorPageResponse<>(
                content,
                nextCursor,
                content.size(),
                hasNext,
                countFetcher.apply(context)
        );
    }

    private OwnerContext resolveOwnerContext(String shareCode, String meId) {
        Users owner = usersRepository.findByShareCode(shareCode)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        return new OwnerContext(owner.getId(), owner.getId().equals(meId));
    }

    private PlaylistCursor decodeCursor(Long cursor, PlaylistSortOption sortOption) {
        if (cursor == null) {
            return null;
        }

        Playlist pivot = playlistRepository.findById(cursor)
                .orElseThrow(() -> new PlaylistException(PlaylistErrorCode.PLAYLIST_NOT_FOUND));

        return switch (sortOption) {
            case RECENT -> new PlaylistCursor(pivot.getId(), null);
            case POPULAR -> new PlaylistCursor(pivot.getId(), pivot.getVisitCount());
        };
    }

    private record OwnerContext(String ownerId, boolean includePrivate) {
    }
}
