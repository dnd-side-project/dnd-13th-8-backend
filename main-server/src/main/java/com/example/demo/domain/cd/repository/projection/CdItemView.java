package com.example.demo.domain.cd.repository.projection;

public record CdItemView (Long cdId, Long playlistId ,Long propId, String theme,
                          Long xCoordinate, Long yCoordinate, Long height, Long width, Long scale, Long angle,
                          String imageKey){
}
