package com.example.demo.domain.cd.dto.request;

import java.util.List;

public record GetCdListRequestDto(List<Long> playlistIds) {
}
