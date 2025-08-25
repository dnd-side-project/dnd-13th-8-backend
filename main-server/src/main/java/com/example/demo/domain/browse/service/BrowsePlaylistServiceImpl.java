package com.example.demo.domain.browse.service;

import com.example.demo.domain.browse.dto.BrowsePlaylistDto;
import com.example.demo.domain.browse.dto.BrowseResponse;
import com.example.demo.domain.browse.entity.BrowsePlaylistCard;
import com.example.demo.domain.browse.repository.BrowsePlaylistRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
public class BrowsePlaylistServiceImpl implements BrowsePlaylistService {

    private final BrowsePlaylistRepository browsePlaylistRepository;

    // 구현
    @Override
    public BrowseResponse getShuffledPlaylists(String userId) {
        List<BrowsePlaylistCard> cards = browsePlaylistRepository.findByUserIdOrderByPosition(userId);
        List<BrowsePlaylistDto> dtos = cards.stream()
                .map(BrowsePlaylistDto::from)
                .toList();

        return new BrowseResponse(dtos);
    }

}