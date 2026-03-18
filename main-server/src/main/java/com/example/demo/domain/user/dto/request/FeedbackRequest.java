package com.example.demo.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "의견/제안 설문 요청")
public record FeedbackRequest(

        @Schema(description = "개인정보 수집 및 이용 동의 여부", example = "true")
        Boolean privacyConsent,

        @Schema(description = "서비스 만족도(1~5)", example = "4")
        Integer satisfaction,

        @Schema(description = "연락처", example = "010-1234-5678")
        String phoneNumber,

        @Schema(description = "의견/제안 내용", example = "UI는 깔끔한데 입력창 높이가 조금 더 유동적이면 좋겠습니다.")
        String opinion
) {
}
