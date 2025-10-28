package com.example.demo.domain.playlist.event;

import com.example.demo.global.redis.PlaylistEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PlaylistEventHandler {

    private final PlaylistEventPublisher playlistEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePlaylistDeletedEvent(PlaylistDeleteEvent event) {
        playlistEventPublisher.publishPlaylistDelete(event.playlistId());
    }
}
