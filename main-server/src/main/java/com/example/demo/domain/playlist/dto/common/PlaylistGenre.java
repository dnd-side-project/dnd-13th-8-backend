package com.example.demo.domain.playlist.dto.common;

import lombok.Getter;

@Getter
public enum PlaylistGenre {
    STUDY("공부·집중"),
    SLEEP("수면·빗소리"),
    RELAX("릴랙스·휴식"),
    WORKOUT("운동·집중력 업"),
    DRIVE("출퇴근·드라이브"),
    PARTY("파티·모임"),
    MOOD("기분전환"),
    ROMANCE("로맨스"),
    KPOP("케이팝"),
    SAD("슬픔·위로");

    private final String displayName;

    PlaylistGenre(String displayName) {
        this.displayName = displayName;
    }
}
