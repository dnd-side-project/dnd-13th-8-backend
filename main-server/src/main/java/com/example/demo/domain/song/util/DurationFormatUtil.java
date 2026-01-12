package com.example.demo.domain.song.util;


import java.time.Duration;

public class DurationFormatUtil {

    /**
     * 초 단위(Long) → "MM:SS" 또는 "HH:MM:SS" 형식 문자열로 변환
     * @param totalSeconds 총 초
     * @return 포맷된 문자열 (예: 03:22 또는 01:03:22)
     */
    public static String formatToHumanReadable(Long totalSeconds) {
        if (totalSeconds == null || totalSeconds <= 0) return "00:00";

        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    /**
     * "MM:SS" 또는 "HH:MM:SS" 문자열 → 총 초(Long)로 변환
     * @param duration 문자열 포맷의 시간 (예: "03:22" 또는 "01:03:22")
     * @return 초 단위
     */
    public static Long parseToSeconds(String duration) {
        if (duration == null || duration.isBlank()) return 0L;

        String[] parts = duration.split(":");
        try {
            if (parts.length == 3) {
                return Long.parseLong(parts[0]) * 3600 +
                        Long.parseLong(parts[1]) * 60 +
                        Long.parseLong(parts[2]);
            } else if (parts.length == 2) {
                return Long.parseLong(parts[0]) * 60 +
                        Long.parseLong(parts[1]);
            } else {
                return 0L;
            }
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    public static String formatDuration(String isoDuration) {
        Duration duration = Duration.parse(isoDuration);
        long minutes = duration.toMinutes();
        long seconds = duration.minusMinutes(minutes).getSeconds();
        return String.format("%02d:%02d", minutes, seconds);
    }
}
