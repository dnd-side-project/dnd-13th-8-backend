package com.example.demo.domain.cd.dto.request;

public record CdItemRequest(Long propId,
                        Long xCoordinate, Long yCoordinate, Long height, Long width, Long scale, Long angle) {
}
