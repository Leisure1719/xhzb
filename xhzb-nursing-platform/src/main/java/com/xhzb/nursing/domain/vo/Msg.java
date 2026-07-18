package com.xhzb.nursing.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Msg {
    MessageType messageType;
    String text;
    Map<String, Object> metadata;
    private LocalDateTime createTime;

    public Msg(Message message) {
        this.messageType = message.getMessageType();
        this.text = message.getText();
        this.metadata = message.getMetadata();
        createTime = LocalDateTime.now();
    }

    public Message toMessage() {
        // 根据消息类型分发，创建对应的 Message 实现类
        return switch (messageType) {
            // 系统消息：用于设置 AI 的行为和上下文
            case SYSTEM -> new SystemMessage(text);
            // 用户消息：封装用户输入的文本、媒体和元数据
            case USER -> UserMessage.builder()
                    .text(text)           // 用户输入的文本内容
                    .media(List.of())     // 媒体文件列表（当前为空）
                    .metadata(metadata)   // 附加的元数据信息
                    .build();
            // 助手消息：AI 返回的响应，包含内容、属性和媒体
            case ASSISTANT -> AssistantMessage.builder()
                    .content(text)        // AI 生成的响应内容
                    .properties(metadata) // 响应的属性信息
                    .media(List.of())     // 媒体文件列表（当前为空）
                    .build();
            // 不支持的消息类型，抛出异常
            default -> throw new IllegalArgumentException("Unsupported message type: " + messageType);
        };
    }
}