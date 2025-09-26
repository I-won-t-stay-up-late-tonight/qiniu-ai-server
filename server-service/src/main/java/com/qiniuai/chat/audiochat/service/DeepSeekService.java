package com.qiniuai.chat.audiochat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qiniuai.chat.audiochat.entity.ChatMessage;
import com.qiniuai.chat.audiochat.entity.ChatRequest;
import com.qiniuai.chat.audiochat.entity.StreamResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.List;

@Service
public class DeepSeekService {

    @Autowired
    private WebClient webClient;

    public Mono<String> chatWithDeepSeek(List<ChatMessage> messages) {
        ChatRequest request = new ChatRequest();
        request.setMessages(messages);

        StringBuilder reasoningContent = new StringBuilder();
        StringBuilder content = new StringBuilder();

        return webClient.post()
                .uri("/chat/completions")
                .bodyValue(request)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(String.class)
                .filter(line -> line.startsWith("data: "))
                .map(line -> line.substring(6))
                .takeUntil("[DONE]"::equals)
                .filter(data -> !data.equals("[DONE]"))
                .map(this::parseStreamData)
                .doOnNext(response -> {
                    if (response != null && response.getChoices() != null) {
                        response.getChoices().forEach(choice -> {
                            if (choice.getDelta() != null) {
                                if (choice.getDelta().getReasoning_content() != null) {
                                    reasoningContent.append(choice.getDelta().getReasoning_content());
                                }
                                if (choice.getDelta().getContent() != null) {
                                    content.append(choice.getDelta().getContent());
                                }
                            }
                        });
                    }
                })
                .then(Mono.fromCallable(() -> {
                    System.out.println("Reasoning: " + reasoningContent.toString());
                    System.out.println("Content: " + content.toString());
                    return content.toString();
                }));
    }

    private StreamResponse parseStreamData(String data) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(data, StreamResponse.class);
        } catch (Exception e) {
            System.err.println("Error parsing stream data: " + e.getMessage());
            return null;
        }
    }
}