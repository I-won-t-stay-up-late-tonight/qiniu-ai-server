package com.qiniuai.chat.audiochat.service;

import com.qiniuai.chat.audiochat.entity.ASRResultDTO;
import com.qiniuai.chat.audiochat.entity.TtsRequest;
import com.qiniuai.chat.web.entity.pojo.Role;
import com.qiniuai.chat.web.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Service
public class VoiceCallService {
    @Autowired
    private ASRService asrService;

    @Autowired
    private DeepSeekService deepSeekService;

    @Autowired
    private QwenTtsService qwenTtsService;
    @Autowired
    private RoleService roleService;


    public ResponseEntity<byte[]> voiceCall(MultipartFile file, Long roleId) throws Exception {
        // 1. 并行执行：ASR识别 + 查角色信息（无依赖，同时启动）
        // 任务1：ASR语音识别（耗时较长）
        CompletableFuture<ASRResultDTO> asrFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return asrService.recognizeAudio(file);
            } catch (Exception e) {
                throw new CompletionException("ASR识别失败", e);
            }
        });

        // 任务2：查角色信息（耗时短，并行执行）
        CompletableFuture<Role> roleFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return roleService.getRoleById(roleId);
            } catch (Exception e) {
                throw new CompletionException("查询角色失败", e);
            }
        });

        // 2. 等待两个并行任务完成
        ASRResultDTO asrResultDTO = asrFuture.get();
        Role role = roleFuture.get();

        // 3. 后续串行流程
        String promptWord = role.toString();
        String modelResult = deepSeekService.callDeepSeekModel(asrResultDTO.getFullText(), promptWord);

        String voice = role.getVoice();
        TtsRequest ttsRequest = new TtsRequest();
        ttsRequest.setVoice(voice);
        ttsRequest.setTextList(new String[]{modelResult});
        ResponseEntity<byte[]> responseEntity = qwenTtsService.synthesizeAndDownload(ttsRequest);

        return responseEntity;
    }
}
