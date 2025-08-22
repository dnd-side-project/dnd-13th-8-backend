package com.example.demo.entity.repository;

import com.example.demo.entity.Chat;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ChatRepository {

    private final DynamoDbEnhancedClient enhancedClient;

    @Value("${aws.dynamodb.table}")
    private String tableName;

    private DynamoDbTable<Chat> table() {
        return enhancedClient.table(tableName, TableSchema.fromBean(Chat.class));
    }

    public void save(Chat chat) {
        table().putItem(chat);
    }

    // 최근 limit개
    public List<Chat> queryRecent(String roomId, String before, int limit) {
        DynamoDbTable<Chat> t = table();

        QueryConditional queryConditional;
        if (before == null) {
            queryConditional = QueryConditional.keyEqualTo(Key.builder().partitionValue(roomId).build());
        } else {
            queryConditional = QueryConditional.sortLessThanOrEqualTo(
                    Key.builder().partitionValue(roomId).sortValue(before).build()
            );
        }

        QueryEnhancedRequest queryEnhancedRequest = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .scanIndexForward(false) // 최신순 정렬
                .limit(limit)
                .build();

        List<Chat> result = new ArrayList<>();
        t.query(queryEnhancedRequest).stream()
                .flatMap(p -> p.items().stream())
                .forEach(result::add);

        return result;
    }
}
