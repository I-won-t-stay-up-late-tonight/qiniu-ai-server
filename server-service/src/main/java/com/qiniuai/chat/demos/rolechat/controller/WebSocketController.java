package com.qiniuai.chat.demos.rolechat.controller;

import com.qiniuai.chat.demos.rolechat.entity.Conversation;
import com.qiniuai.chat.demos.rolechat.entity.Role;
import com.qiniuai.chat.demos.rolechat.repository.ChatConversationRepository;
import com.qiniuai.chat.demos.rolechat.repository.ChatRoleRepository;
import com.qiniuai.chat.demos.rolechat.service.AudioService;
import com.qiniuai.chat.demos.rolechat.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * WebSocket控制器，处理语音数据传输
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final AudioService audioService;
    private final ChatConversationRepository conversationRepository;
    private final ChatRoleRepository roleRepository;
    private final RedisService redisService;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${app.websocket.broker.destination}")
    private String brokerDestination;

    /**
     * 处理客户端连接，创建新对话
     */
    @MessageMapping("/voice-call/connect/{userId}/{roleId}")
    public void handleConnect(@DestinationVariable String userId, @DestinationVariable String roleId) {
        try {
            log.info("用户连接语音聊天，用户ID: {}, 角色ID: {}", userId, roleId);
            
            // 生成唯一对话ID
            String conversationId = "conv-" + UUID.randomUUID().toString().replaceAll("-", "");
            
            // 查询角色信息
            Optional<Role> roleOptional = roleRepository.findById(roleId);
            if (roleOptional.isEmpty()) {
                log.error("角色不存在，角色ID: {}", roleId);
                sendErrorMessage(userId, "角色不存在");
                return;
            }
            Role role = roleOptional.get();
            
            // 创建新对话
            Conversation conversation = Conversation.builder()
                    .id(conversationId)
                    .userId(userId)
                    .roleId(roleId)
                    .roleName(role.getName())
                    .title("与" + role.getName() + "的对话")
                    .status("ACTIVE")
                    .createTime(LocalDateTime.now())
                    .lastUpdateTime(LocalDateTime.now())
                    .lastMessageTime(LocalDateTime.now())
                    .duration(0)
                    .build();
            
            conversationRepository.save(conversation);
            log.info("创建新对话，对话ID: {}, 用户ID: {}, 角色ID: {}", conversationId, userId, roleId);
            
            // 发送欢迎消息
            sendWelcomeMessage(userId, conversationId, role);
            
        } catch (Exception e) {
            log.error("处理WebSocket连接失败，用户ID: {}", userId, e);
            sendErrorMessage(userId, "连接失败，请重试");
        }
    }

    /**
     * 接收客户端发送的音频分片
     */
    @MessageMapping("/voice-call/send/{userId}/{conversationId}")
    public void handleAudioChunk(@DestinationVariable String userId, 
                                @DestinationVariable String conversationId,
                                @Payload Map<String, Object> payload) {
        try {
            // 解析音频分片信息
            int chunkIndex = (Integer) payload.get("chunkIndex");
            int totalChunks = (Integer) payload.get("totalChunks");
            String audioDataBase64 = (String) payload.get("audioData");
            boolean isLastChunk = (Boolean) payload.get("isLastChunk");
            
            // Base64解码音频数据
            byte[] audioData = Base64.getDecoder().decode(audioDataBase64);
            
            log.info("收到音频分片，对话ID: {}, 用户ID: {}, 分片索引: {}/{}, 大小: {}KB",
                    conversationId, userId, chunkIndex, totalChunks, audioData.length / 1024);
            
            // 保存音频分片
            audioService.saveAudioChunk(conversationId, userId, chunkIndex, totalChunks, audioData, isLastChunk);
            
        } catch (Exception e) {
            log.error("处理音频分片失败，对话ID: {}, 用户ID: {}", conversationId, userId, e);
            sendErrorMessage(userId, "发送语音失败，请重试");
        }
    }

    /**
     * 发送欢迎消息
     */
    private void sendWelcomeMessage(String userId, String conversationId, Role role) {
        try {
            // 构建欢迎消息
            String welcomeMessage = role.getWelcomeMessage() != null && !role.getWelcomeMessage().isEmpty()
                    ? role.getWelcomeMessage()
                    : "你好！我是" + role.getName() + "，很高兴能和你聊天。有什么想知道的尽管问我吧！";
            
            // 发送欢迎消息到客户端
            messagingTemplate.convertAndSendToUser(
                    userId,
                    brokerDestination,
                    Map.of(
                            "type", "welcome",
                            "conversationId", conversationId,
                            "message", welcomeMessage,
                            "roleName", role.getName(),
                            "roleAvatar", role.getAvatarUrl()
                    )
            );
            log.info("发送欢迎消息，用户ID: {}, 对话ID: {}", userId, conversationId);
        } catch (Exception e) {
            log.error("发送欢迎消息失败，用户ID: {}", userId, e);
        }
    }

    /**
     * 发送错误消息
     */
    private void sendErrorMessage(String userId, String message) {
        try {
            messagingTemplate.convertAndSendToUser(
                    userId,
                    brokerDestination,
                    Map.of(
                            "type", "error",
                            "message", message
                    )
            );
        } catch (Exception e) {
            log.error("发送错误消息失败，用户ID: {}", userId, e);
        }
    }
}
    