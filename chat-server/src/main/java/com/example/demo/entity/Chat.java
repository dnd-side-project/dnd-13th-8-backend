package com.example.demo.entity;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;
import lombok.*;

@DynamoDbBean
@Getter
@Setter
@NoArgsConstructor
public class Chat {

    private String roomId;        // 파티션 키
    private String sentAt;        // 정렬 키
    private String messageId;
    private String senderId;
    private String username;
    private String content;
    private String profileImage;

    private Integer messageCount;
    private String itemType;

    private boolean systemMessage;

    @DynamoDbPartitionKey
    @DynamoDbSecondaryPartitionKey(indexNames = "GSI1")
    public String getRoomId() {
        return roomId;
    }

    @DynamoDbSortKey
    public String getSentAt() {
        return sentAt;
    }

    @DynamoDbSecondarySortKey(indexNames = "GSI1")
    public String getMessageId() {
        return messageId;
    }

    @Builder
    public Chat(String roomId,
                String sentAt,
                String username,
                String messageId,
                String senderId,
                String content,
                String profileImage,
                boolean systemMessage) {
        this.roomId = roomId;
        this.sentAt = sentAt;
        this.username = username;
        this.messageId = messageId;
        this.senderId = senderId;
        this.content = content;
        this.profileImage = profileImage;
        this.systemMessage = systemMessage;
    }
}
