package com.qiniuai.chat.audiochat.consumer;

import com.qiniuai.chat.audiochat.entity.Task;
import com.qiniuai.chat.audiochat.service.AudioService;
import com.qiniuai.chat.audiochat.service.RabbitMQService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 音频合并任务消费者，处理音频分片合并
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AudioMergeConsumer {

    private final AudioService audioService;
    private final RabbitMQService rabbitMQService;

    @RabbitListener(queues = "${rabbitmq.queues.audio-merge}")
    public void handleAudioMergeTask(Task task) {
        log.info("接收音频合并任务，任务ID: {}, 对话ID: {}", task.getId(), task.getConversationId());
        
        try {
            // 更新任务状态
            task.setStatus("PROCESSING");
            
            // 获取任务数据
            Map<String, Object> data = task.getData();
            String conversationId = (String) data.get("conversationId");
            String userId = (String) data.get("userId");
            
            // 执行音频合并
            CompletableFuture<String> mergeFuture = audioService.mergeAudioChunks(conversationId, userId);
            
            // 合并完成后发送ASR任务
            mergeFuture.whenComplete((audioFilePath, ex) -> {
                if (ex != null) {
                    log.error("音频合并失败，任务ID: {}, 对话ID: {}", task.getId(), conversationId, ex);
                    task.setStatus("FAILED");
                    task.setErrorMessage(ex.getMessage());
                } else {
                    log.info("音频合并成功，任务ID: {}, 对话ID: {}, 文件路径: {}", 
                            task.getId(), conversationId, audioFilePath);
                    
                    // 构建ASR任务
                    Map<String, Object> asrData = new HashMap<>();
                    asrData.put("audioFilePath", audioFilePath);
                    
                    Task asrTask = Task.builder()
                            .conversationId(conversationId)
                            .userId(userId)
                            .roleId(task.getRoleId())
                            .data(asrData)
                            .build();
                    
                    // 发送ASR任务
                    rabbitMQService.sendAsrTask(asrTask);
                    
                    // 更新任务状态
                    task.setStatus("COMPLETED");
                }
            });
            
        } catch (Exception e) {
            log.error("处理音频合并任务异常，任务ID: {}, 对话ID: {}", task.getId(), task.getConversationId(), e);
            task.setStatus("FAILED");
            task.setErrorMessage(e.getMessage());
        }
    }
}
    