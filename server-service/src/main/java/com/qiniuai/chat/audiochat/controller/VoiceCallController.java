package com.qiniuai.chat.audiochat.controller;

import com.qiniuai.chat.audiochat.service.VoiceCallService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/voice")
@RequiredArgsConstructor
@Slf4j
public class VoiceCallController {

    private final VoiceCallService voiceCallService;

    @PostMapping("/call")
    public ResponseEntity<byte[]> call(@RequestParam("file") MultipartFile file,
                                       @RequestParam("roleId") Long roleId
    ) throws Exception {
        return voiceCallService.voiceCall(file, roleId);
    }
}
