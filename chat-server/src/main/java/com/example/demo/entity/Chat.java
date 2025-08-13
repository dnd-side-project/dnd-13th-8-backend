package com.example.demo.entity;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;
import lombok.*;

@DynamoDbBean
@Getter
@NoArgsConstructor
public class Chat {

    private String roomId;        // 파티션 키
    private String sentAt;        // 정렬 키
    private String messageId;
    private String senderId;
    private String content;
    private boolean systemMessage;

    @DynamoDbPartitionKey
    public String getRoomId() {
        return roomId;
    }

    @DynamoDbSortKey
    public String getSentAt() {
        return sentAt;
    }

    @Builder
    public Chat(String sentAt, String messageId, String senderId, String content,
                boolean systemMessage) {
        this.sentAt = sentAt;
        this.messageId = messageId;
        this.senderId = senderId;
        this.content = content;
        this.systemMessage = systemMessage;
    }
}
