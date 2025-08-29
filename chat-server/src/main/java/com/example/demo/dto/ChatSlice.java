package com.example.demo.dto;

import com.example.demo.entity.Chat;

import java.util.List;

public record ChatSlice(List<Chat> items, String nextCursor) {
}
