package com.example.demo.domain.cd.dto.request;

import java.util.List;

public record SaveCdRequestDto (List<CdItemRequest> cdItems){
}
