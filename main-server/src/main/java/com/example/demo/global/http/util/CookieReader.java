package com.example.demo.global.http.util;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class CookieReader {

    public Optional<String> read(HttpServletRequest req, String name) {
        if (req == null || name == null || name.isBlank() || req.getCookies() == null) {
            return Optional.empty();
        }
        for (var c : req.getCookies()) {
            if (name.equals(c.getName()) && c.getValue() != null && !c.getValue().isBlank()) {
                return Optional.of(c.getValue());
            }
        }
        return Optional.empty();
    }
}
