package com.example.demo.domain.cd.repository.projection;

import lombok.Getter;

@Getter
public class CdItemView {

    private final Long cdId;
    private final Long playlistId;
    private final Long propId;

    private final Long xCoordinate;
    private final Long yCoordinate;
    private final Long height;
    private final Long width;
    private final Long scale;
    private final Long angle;

    private final String theme;
    private final String imageKey;

    public CdItemView(
            Long cdId,
            Long playlistId,
            Long propId,
            Long xCoordinate,
            Long yCoordinate,
            Long height,
            Long width,
            Long scale,
            Long angle,
            String theme,
            String imageKey
    ) {
        this.cdId = cdId;
        this.playlistId = playlistId;
        this.propId = propId;
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
        this.height = height;
        this.width = width;
        this.scale = scale;
        this.angle = angle;
        this.theme = theme;
        this.imageKey = imageKey;
    }
}
