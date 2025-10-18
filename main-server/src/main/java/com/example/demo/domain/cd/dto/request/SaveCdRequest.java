package com.example.demo.domain.cd.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SaveCdRequest(@NotNull List<CdItemRequest> cdItems){
}
