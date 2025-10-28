package com.example.demo.entity.repository;

import com.example.demo.dto.ChatSlice;
import com.example.demo.entity.Chat;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.*;

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

    public Optional<Chat> findOneByMessageId(String roomId, String messageId) {
        DynamoDbTable<Chat> t = table();

        Expression filter = Expression.builder()
                .expression("messageId = :mid")
                .putExpressionValue(":mid", AttributeValue.builder().s(messageId).build())
                .build();

        QueryEnhancedRequest req = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(
                        Key.builder().partitionValue(roomId).build()))
                .filterExpression(filter)
                .build();

        for (Page<Chat> page : t.query(req)) {
            for (Chat c : page.items()) {
                if (messageId.equals(c.getMessageId())) {
                    return Optional.of(c);
                }
            }
        }
        return Optional.empty();
    }

    // PK(roomId) + SK(sentAt)로 삭제
    public boolean deleteByPk(String roomId, String sentAt) {
        DynamoDbTable<Chat> t = table();
        Key key = Key.builder().partitionValue(roomId).sortValue(sentAt).build();

        Chat deleted = t.deleteItem(r -> r.key(key));
        return deleted != null; // 존재했으면 삭제된 엔티티 반환
    }

    public int countByRoomId(String roomId) {
        int total = 0;
        Map<String, AttributeValue> lek = null;

        do {
            var req = QueryEnhancedRequest.builder()
                    .queryConditional(QueryConditional.keyEqualTo(
                            Key.builder().partitionValue(roomId).build()))
                    .attributesToProject("roomId", "sentAt") // 키만
                    .limit(1000)                              // 페이지 크기
                    .exclusiveStartKey(lek)
                    .build();

            Page<Chat> first = table().query(req).stream().findFirst().orElse(null);
            if (first == null) break;

            total += first.items().size();
            lek = first.lastEvaluatedKey();
        } while (lek != null && !lek.isEmpty());

        return total;
    }

    public void deleteAllByRoomId(String roomId) {
        DynamoDbTable<Chat> t = table();

        Map<String, AttributeValue> lek = null;
        do {
            QueryEnhancedRequest.Builder qb = QueryEnhancedRequest.builder()
                    .queryConditional(QueryConditional.keyEqualTo(Key.builder().partitionValue(roomId).build()))
                    .attributesToProject("roomId", "sentAt") // 키만 읽어서 I/O 절감
                    .limit(1000)                              // 페이지 크기 (튜닝 가능)
                    .exclusiveStartKey(lek);

            Page<Chat> page = t.query(qb.build()).stream().findFirst().orElse(null);
            if (page == null) break;

            // 25개씩 BatchWrite(Delete)
            List<WriteBatch> batches = new ArrayList<>();
            List<WriteBatch.Builder<Chat>> builders = new ArrayList<>();
            WriteBatch.Builder<Chat> curr = WriteBatch.builder(Chat.class).mappedTableResource(t);

            int cnt = 0;
            for (Chat c : page.items()) {
                curr.addDeleteItem(Key.builder().partitionValue(c.getRoomId()).sortValue(c.getSentAt()).build());
                cnt++;
                if (cnt % 25 == 0) {
                    builders.add(curr);
                    curr = WriteBatch.builder(Chat.class).mappedTableResource(t);
                }
            }
            if (cnt % 25 != 0) builders.add(curr);

            for (WriteBatch.Builder<Chat> b : builders) {
                batches.add(b.build());
            }
            if (!batches.isEmpty()) {
                BatchWriteItemEnhancedRequest req = BatchWriteItemEnhancedRequest.builder()
                        .writeBatches(batches)
                        .build();
                enhancedClient.batchWriteItem(req);
            }

            lek = page.lastEvaluatedKey();
        } while (lek != null && !lek.isEmpty());
    }
}
