package com.example.demo.domain.cd.repository.projection;

public record CdItemView (Long cdId, Long playlistId ,Long propId,
                          Long xCoordinate, Long yCoordinate, Long zCoordinate, Long angle,
                          String imageKey){
}
