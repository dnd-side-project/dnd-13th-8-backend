package com.example.demo.domain.browse.dto;


import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "셔플된 브라우즈 카드 목록의 커서 정보")
public record BrowsePlaylistCursor(

        @Schema(
                description = """
            사용자의 셔플 카드 목록 중 몇 번째 그룹에 속한 카드인지 나타냅니다. 
            이 값은 하루에 한 번 자동으로 섞이며, 각 사용자는 position 0부터 시작하는 카드 목록을 갖습니다.
            position이 낮을수록 먼저 노출됩니다.
            """,
                example = "2"
        )
        int position,

        @Schema(
                description = """
            BrowsePlaylistCard의 고유 ID입니다. 
            같은 position 안에서도 여러 카드가 있을 수 있기 때문에, 이 값으로 정확한 커서 위치를 지정합니다.
            """,
                example = "3"
        )
        Long cardId

) {}
