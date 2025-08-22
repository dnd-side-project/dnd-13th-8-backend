package com.example.demo.domain.playlist.dto.search;

public record UserSearchDto(
        String userId,
        String username
) implements SearchItem {
    @Override
    public String getType() {
        return "USER";
    }
}

