package com.qiniuai.chat.audiochat.service;

import com.qiniuai.chat.audiochat.entity.ASRResultDTO;
import com.qiniuai.chat.audiochat.entity.TtsRequest;
import com.qiniuai.chat.web.entity.pojo.Role;
import com.qiniuai.chat.web.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
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


    public ResponseEntity<byte[]> voiceCall(MultipartFile file,
                                            Long roleId
    ) throws Exception {
        //1. 语音识别
        ASRResultDTO asrResultDTO = asrService.recognizeAudio(file);
        // 拿到角色的提示词
        Role role = roleService.getRoleById(roleId);
        String promptWord = role.toString();
        //2. 调用模型
        String modelResult = deepSeekService.callDeepSeekModel(asrResultDTO.getFullText(), promptWord);
        // 拿到角色设定的音色
        String voice = role.getVoice();
        //3. 语音合成
        TtsRequest ttsRequest = new TtsRequest();
        ttsRequest.setVoice(voice);
        ttsRequest.setTextList(new String[]{modelResult});
        ResponseEntity<byte[]> responseEntity = qwenTtsService.synthesizeAndDownload(ttsRequest);
        return responseEntity;
    }
}
