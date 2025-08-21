package com.example.demo.entity.repository;

import com.example.demo.entity.Chat;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
@RequiredArgsConstructor
public class ChatRepository {

    private final DynamoDbEnhancedClient enhancedClient;

    @Value("${aws.dynamodb.table}")
    private String tableName;

    public void save(Chat chat) {
        DynamoDbTable<Chat> table = enhancedClient.table(tableName, TableSchema.fromBean(Chat.class));
        table.putItem(chat);
    }
}
