package com.qiniuai.chat.audiochat.consumer;

import com.qiniuai.chat.audiochat.entity.Role;
import com.qiniuai.chat.audiochat.entity.Task;
import com.qiniuai.chat.audiochat.repository.ChatRoleRepository;
import com.qiniuai.chat.audiochat.service.AliyunAsrService;
import com.qiniuai.chat.audiochat.service.RabbitMQService;
import com.qiniuai.chat.audiochat.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * ASR任务消费者，处理语音识别
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AsrTaskConsumer {

    private final AliyunAsrService aliyunAsrService;
    private final RabbitMQService rabbitMQService;
    private final ChatRoleRepository roleRepository;
    private final RedisService redisService;

    @RabbitListener(queues = "${rabbitmq.queues.asr-task}")
    public void handleAsrTask(Task task) {
        log.info("接收ASR任务，任务ID: {}, 对话ID: {}", task.getId(), task.getConversationId());
        
        try {
            // 更新任务状态
            task.setStatus("PROCESSING");
            
            // 执行ASR识别
            CompletableFuture<String> asrFuture = aliyunAsrService.processAsrTask(task);
            
            // 识别完成后发送LLM任务
            asrFuture.whenComplete((text, ex) -> {
                if (ex != null) {
                    log.error("ASR识别失败，任务ID: {}, 对话ID: {}", task.getId(), task.getConversationId(), ex);
                    task.setStatus("FAILED");
                    task.setErrorMessage(ex.getMessage());
                } else if (text == null || text.trim().isEmpty()) {
                    log.error("ASR识别结果为空，任务ID: {}, 对话ID: {}", task.getId(), task.getConversationId());
                    task.setStatus("FAILED");
                    task.setErrorMessage("识别结果为空");
                } else {
                    log.info("ASR识别成功，任务ID: {}, 对话ID: {}, 识别文本: {}", 
                            task.getId(), task.getConversationId(), text);
                    
                    // 获取角色信息（优先从缓存获取）
                    String roleId = task.getRoleId();
                    Role role = null;
                    String roleKey = redisService.getRoleKey(roleId);
                    
                    if (redisService.hasKey(roleKey)) {
                        role = redisService.get(roleKey, Role.class);
                    } else {
                        Optional<Role> roleOptional = roleRepository.findById(roleId);
                        if (roleOptional.isPresent()) {
                            role = roleOptional.get();
                            // 存入缓存
                            redisService.set(roleKey, role, 24, java.util.concurrent.TimeUnit.HOURS);
                        }
                    }
                    
                    if (role == null) {
                        log.error("角色不存在，任务ID: {}, 角色ID: {}", task.getId(), roleId);
                        task.setStatus("FAILED");
                        task.setErrorMessage("角色不存在");
                        return;
                    }
                    
                    // 构建LLM任务
                    Map<String, Object> llmData = new HashMap<>();
                    llmData.put("text", text);
                    llmData.put("systemPrompt", role.getSystemPrompt());
                    llmData.put("roleId", roleId);
                    
                    Task llmTask = Task.builder()
                            .conversationId(task.getConversationId())
                            .userId(task.getUserId())
                            .roleId(roleId)
                            .data(llmData)
                            .build();
                    
                    // 发送LLM任务
                    rabbitMQService.sendLlmTask(llmTask);
                    
                    // 更新任务状态
                    task.setStatus("COMPLETED");
                }
            });
            
        } catch (Exception e) {
            log.error("处理ASR任务异常，任务ID: {}, 对话ID: {}", task.getId(), task.getConversationId(), e);
            task.setStatus("FAILED");
            task.setErrorMessage(e.getMessage());
        }
    }
}
    