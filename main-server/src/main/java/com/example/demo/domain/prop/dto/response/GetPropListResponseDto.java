package com.example.demo.domain.prop.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record GetPropListResponseDto (List<PropResponse> props) {

}
