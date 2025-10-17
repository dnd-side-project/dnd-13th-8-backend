package com.example.demo.domain.cd.dto.request;

import java.util.List;

public record SaveCdRequest(List<CdItemRequest> cdItems){
}
