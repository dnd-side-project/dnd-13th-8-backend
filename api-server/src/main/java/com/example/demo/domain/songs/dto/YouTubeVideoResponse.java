package com.example.demo.domain.songs.dto;

import java.util.List;

public record YouTubeVideoResponse(
        List<Item> items
) {
    public record Item(
            String id,
            Snippet snippet,
            ContentDetails contentDetails
    ) {}

    public record Snippet(
            String title,
            Thumbnails thumbnails
    ) {}

    public record Thumbnails(
            Thumbnail high
    ) {}

    public record Thumbnail(
            String url
    ) {}

    public record ContentDetails(
            String duration // ISO 8601 형식: PT3M25S
    ) {}
}
