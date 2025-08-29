package com.example.demo.entity.repository;

import com.example.demo.dto.ChatSlice;
import com.example.demo.entity.Chat;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ChatRepository {

    private final DynamoDbEnhancedClient enhancedClient;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${aws.dynamodb.table}")
    private String tableName;

    private DynamoDbTable<Chat> table() {
        return enhancedClient.table(tableName, TableSchema.fromBean(Chat.class));
    }

    public void save(Chat chat) {
        table().putItem(chat);
    }

    public ChatSlice queryRecentSlice(String roomId, String beforeCursorBase64, int pageSize) {
        DynamoDbTable<Chat> t = table();

        QueryEnhancedRequest.Builder qb = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(
                        Key.builder().partitionValue(roomId).build()
                ))
                .scanIndexForward(false) // 최신순 (DESC)
                .limit(pageSize);

        if (beforeCursorBase64 != null && !beforeCursorBase64.isBlank()) {
            qb.exclusiveStartKey(decodeLek(beforeCursorBase64));
        }

        QueryEnhancedRequest req = qb.build();


        PageIterable<Chat> pages = t.query(req);
        Page<Chat> first = pages.stream().findFirst().orElse(null);
        if (first == null) {
            return new ChatSlice(List.of(), null);
        }

        List<Chat> items = first.items();
        Map<String, AttributeValue> lek = first.lastEvaluatedKey();

        String nextCursor = (lek == null || lek.isEmpty()) ? null : encodeLek(lek);
        return new ChatSlice(items, nextCursor);
    }

    private String encodeLek(Map<String, AttributeValue> lek) {
        try {
            byte[] json = objectMapper.writeValueAsBytes(lek);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encode LEK", e);
        }
    }

    private Map<String, AttributeValue> decodeLek(String base64) {
        try {
            byte[] raw = Base64.getUrlDecoder().decode(base64);
            return objectMapper.readValue(raw, new TypeReference<>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid cursor", e);
        }
    }
}
