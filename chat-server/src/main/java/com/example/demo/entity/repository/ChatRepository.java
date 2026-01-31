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
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class ChatRepository {

    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbClient dynamoDbClient;
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

        Expression filter = Expression.builder()
                .expression("sentAt <> :meta")
                .putExpressionValue(":meta", AttributeValue.builder().s("META").build())
                .build();

        QueryEnhancedRequest.Builder qb = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(
                        Key.builder().partitionValue(roomId).build()
                ))
                .filterExpression(filter)
                .scanIndexForward(false)
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

        DynamoDbIndex<Chat> idx = t.index("GSI1");

        QueryEnhancedRequest req = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(
                        Key.builder()
                                .partitionValue(roomId)
                                .sortValue(messageId)
                                .build()
                ))
                .limit(1)
                .build();

        Page<Chat> first = idx.query(req).stream().findFirst().orElse(null);
        if (first == null || first.items().isEmpty()) return Optional.empty();
        return Optional.of(first.items().get(0));
    }

    // PK(roomId) + SK(sentAt)로 삭제
    public boolean deleteByPk(String roomId, String sentAt) {
        DynamoDbTable<Chat> t = table();
        Key key = Key.builder().partitionValue(roomId).sortValue(sentAt).build();

        Chat deleted = t.deleteItem(r -> r.key(key));
        return deleted != null; // 존재했으면 삭제된 엔티티 반환
    }

    public void incrementRoomCount(String roomId, int delta) {

        Map<String, AttributeValue> key = Map.of(
                "roomId", AttributeValue.builder().s(roomId).build(),
                "sentAt", AttributeValue.builder().s("META").build()
        );

        Map<String, AttributeValue> values = Map.of(
                ":t", AttributeValue.builder().s("META").build(),
                ":zero", AttributeValue.builder().n("0").build(),
                ":d", AttributeValue.builder().n(String.valueOf(delta)).build()
        );

        UpdateItemRequest req = UpdateItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .updateExpression(
                        "SET itemType = :t, messageCount = if_not_exists(messageCount, :zero) + :d"
                )
                .expressionAttributeValues(values)
                .build();

        dynamoDbClient.updateItem(req);
    }

    public int countByRoomId(String roomId) {
        DynamoDbTable<Chat> t = table();

        Key key = Key.builder()
                .partitionValue(roomId)
                .sortValue("META")
                .build();

        Chat meta = t.getItem(r -> r.key(key));

        if (meta == null || meta.getMessageCount() == null) {
            return 0;
        }

        return meta.getMessageCount();
    }


    public void deleteAllByRoomId(String roomId) {
        DynamoDbTable<Chat> t = table();

        Map<String, AttributeValue> lek = null;
        do {
            QueryEnhancedRequest req = QueryEnhancedRequest.builder()
                    .queryConditional(QueryConditional.keyEqualTo(
                            Key.builder().partitionValue(roomId).build()))
                    .attributesToProject("roomId", "sentAt")
                    .limit(1000)
                    .exclusiveStartKey(lek)
                    .build();

            Page<Chat> page = t.query(req).stream().findFirst().orElse(null);
            if (page == null) break;

            List<Chat> chats = page.items();
            for (int i = 0; i < chats.size(); i += 25) {
                int end = Math.min(i + 25, chats.size());

                WriteBatch.Builder<Chat> wb = WriteBatch.builder(Chat.class)
                        .mappedTableResource(t);

                for (int j = i; j < end; j++) {
                    Chat c = chats.get(j);
                    wb.addDeleteItem(b -> b.key(
                            Key.builder()
                                    .partitionValue(c.getRoomId())
                                    .sortValue(c.getSentAt())
                                    .build()
                    ));
                }

                BatchWriteItemEnhancedRequest batchReq = BatchWriteItemEnhancedRequest.builder()
                        .writeBatches(wb.build())
                        .build();

                enhancedClient.batchWriteItem(batchReq);
            }

            lek = page.lastEvaluatedKey();
        } while (lek != null && !lek.isEmpty());
    }
}
