package com.qiniuai.chat.audiochat.service;

import com.qiniuai.chat.audiochat.entity.ASRResultDTO;
import com.qiniuai.chat.audiochat.entity.TtsRequest;
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

    public ResponseEntity<byte[]> voiceCall(MultipartFile file) throws Exception {
        //1. 语音识别
        ASRResultDTO asrResultDTO = asrService.recognizeAudio(file);

        //2. 调用模型
        String modelResult = deepSeekService.callDeepSeekModel(asrResultDTO.getFullText());

        //3. 语音合成
        TtsRequest ttsRequest = new TtsRequest();
        ttsRequest.setTextList(new String[]{modelResult});
        ResponseEntity<byte[]> responseEntity = qwenTtsService.synthesizeAndDownload(ttsRequest);
        return responseEntity;
    }
}
