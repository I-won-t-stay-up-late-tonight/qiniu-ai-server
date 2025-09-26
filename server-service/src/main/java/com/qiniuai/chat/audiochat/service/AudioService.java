package com.qiniuai.chat.audiochat.service;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.qiniuai.chat.audiochat.entity.AudioChunk;
import com.qiniuai.chat.audiochat.entity.Task;
import com.qiniuai.chat.audiochat.repository.ChatAudioChunkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 音频处理服务，负责音频分片管理和合并
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AudioService {

    private final ChatAudioChunkRepository audioChunkRepository;
    private final RabbitMQService rabbitMQService;
    private final RedisService redisService;

    @Value("${app.audio.temp-dir}")
    private String tempAudioDir;
    
    @Value("${app.audio.chunk-size}")
    private int chunkSize;
    
    @Value("${app.cache.conversation-status-expire-minutes}")
    private long conversationStatusExpireMinutes;

    /**
     * 保存音频分片
     */
    public void saveAudioChunk(String conversationId, String userId, int chunkIndex, 
                             int totalChunks, byte[] audioData, boolean isLastChunk) {
        try {
            // 音频数据Base64编码存储
            String audioDataBase64 = Base64.encode(audioData);
            
            // 创建音频分片实体
            AudioChunk chunk = AudioChunk.builder()
                    .id(IdUtil.simpleUUID())
                    .conversationId(conversationId)
                    .userId(userId)
                    .chunkIndex(chunkIndex)
                    .totalChunks(totalChunks)
                    .audioData(audioDataBase64)
                    .isLastChunk(isLastChunk)
                    .createTime(System.currentTimeMillis())
                    .build();
            
            // 保存分片
            audioChunkRepository.save(chunk);
            log.info("保存音频分片，对话ID: {}, 分片索引: {}, 总分片数: {}", 
                    conversationId, chunkIndex, totalChunks);
            
            // 更新Redis中的分片计数
            String chunkCountKey = redisService.getAudioChunkCountKey(conversationId);
            Long count = redisService.increment(chunkCountKey, 1);
            redisService.expire(chunkCountKey, conversationStatusExpireMinutes, java.util.concurrent.TimeUnit.MINUTES);
            
            // 如果是最后一个分片或所有分片都已收到，触发合并
            if (isLastChunk || (count != null && count >= totalChunks)) {
                log.info("所有分片已接收，准备合并，对话ID: {}", conversationId);
                
                // 创建合并任务
                Map<String, Object> data = new HashMap<>();
                data.put("conversationId", conversationId);
                data.put("userId", userId);
                
                Task task = Task.builder()
                        .conversationId(conversationId)
                        .userId(userId)
                        .data(data)
                        .build();
                
                // 发送合并任务到消息队列
                rabbitMQService.sendAudioMergeTask(task);
            }
        } catch (Exception e) {
            log.error("保存音频分片失败，对话ID: {}", conversationId, e);
            throw new RuntimeException("保存音频分片失败", e);
        }
    }

    /**
     * 合并音频分片
     */
    @Async("asrExecutor")
    public CompletableFuture<String> mergeAudioChunks(String conversationId, String userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("开始合并音频分片，对话ID: {}", conversationId);
                
                // 获取该对话的所有分片
                List<AudioChunk> chunks = audioChunkRepository
                        .findByConversationIdAndUserIdOrderByChunkIndexAsc(conversationId, userId);
                
                if (chunks.isEmpty()) {
                    log.error("没有找到音频分片，对话ID: {}", conversationId);
                    throw new RuntimeException("没有找到音频分片");
                }
                
                // 创建临时文件
                String fileName = conversationId + "_" + System.currentTimeMillis() + ".mp3";
                Path tempDir = Paths.get(tempAudioDir);
                if (!Files.exists(tempDir)) {
                    Files.createDirectories(tempDir);
                }
                Path tempFilePath = tempDir.resolve(fileName);
                
                // 合并分片
                for (AudioChunk chunk : chunks) {
                    byte[] audioData = Base64.decode(chunk.getAudioData());
                    Files.write(tempFilePath, audioData, java.nio.file.StandardOpenOption.CREATE, 
                            java.nio.file.StandardOpenOption.APPEND);
                }
                
                log.info("音频分片合并完成，对话ID: {}, 保存路径: {}, 大小: {}KB", 
                        conversationId, tempFilePath, FileUtil.size(new File(tempFilePath.toString())) / 1024);
                
                // 删除已合并的分片
                audioChunkRepository.deleteByConversationId(conversationId);
                
                // 删除Redis中的分片计数
                redisService.delete(redisService.getAudioChunkCountKey(conversationId));
                
                return tempFilePath.toString();
            } catch (Exception e) {
                log.error("合并音频分片失败，对话ID: {}", conversationId, e);
                throw new RuntimeException("合并音频分片失败", e);
            }
        });
    }

    /**
     * 清理过期的音频文件
     */
    public void cleanExpiredAudioFiles(long expireMillis) {
        try {
            Path tempDir = Paths.get(tempAudioDir);
            if (!Files.exists(tempDir)) {
                return;
            }
            
            File[] files = tempDir.toFile().listFiles();
            if (files == null) {
                return;
            }
            
            long currentTime = System.currentTimeMillis();
            int deletedCount = 0;
            
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".mp3")) {
                    long lastModified = file.lastModified();
                    if (currentTime - lastModified > expireMillis) {
                        if (file.delete()) {
                            deletedCount++;
                            log.debug("删除过期音频文件: {}", file.getAbsolutePath());
                        }
                    }
                }
            }
            
            if (deletedCount > 0) {
                log.info("清理过期音频文件完成，共删除 {} 个文件", deletedCount);
            }
        } catch (Exception e) {
            log.error("清理过期音频文件异常", e);
        }
    }
}
    