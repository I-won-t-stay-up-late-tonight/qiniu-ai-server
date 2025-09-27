package com.qiniuai.chat.audiochat.controller;

import com.qiniuai.chat.audiochat.entity.ChatMessage;
import com.qiniuai.chat.audiochat.entity.ChatRequest;
import com.qiniuai.chat.audiochat.service.DeepSeekService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class DeepSeekController {

    @Autowired
    private DeepSeekService deepSeekService;

    @PostMapping("/conversation")
    public Mono<ResponseEntity<Map<String, Object>>> chatConversation() {
        List<ChatMessage> messages = new ArrayList<>();

        // Round 1
        messages.add(new ChatMessage("user", "9.11 and 9.8, which is greater?"));

        return deepSeekService.chatWithDeepSeek(new ArrayList<>(messages))
                .flatMap(round1Response -> {
                    // 添加第一轮响应到消息历史
                    messages.add(new ChatMessage("assistant", round1Response));

                    // Round 2
                    messages.add(new ChatMessage("user", "How many Rs are there in the word 'strawberry'?"));

                    return deepSeekService.chatWithDeepSeek(new ArrayList<>(messages))
                            .map(round2Response -> {
                                Map<String, Object> result = new HashMap<>();
                                result.put("round1", round1Response);
                                result.put("round2", round2Response);
                                return ResponseEntity.ok(result);
                            });
                })
                .onErrorResume(e -> {
                    Map<String, Object> error = new HashMap<>();
                    error.put("error", e.getMessage());
                    return Mono.just(ResponseEntity.status(500).body(error));
                });
    }

    @PostMapping("/single")
    public Mono<ResponseEntity<Map<String, Object>>> singleChat(@RequestBody ChatRequest request) {
        return deepSeekService.chatWithDeepSeek(request.getMessages())
                .map(response -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("response", response);
                    return ResponseEntity.ok(result);
                })
                .onErrorResume(e -> {
                    Map<String, Object> error = new HashMap<>();
                    error.put("error", e.getMessage());
                    return Mono.just(ResponseEntity.status(500).body(error));
                });
    }
}
