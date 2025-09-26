package com.qiniuai.chat.audiochat.entity;

import lombok.Data;

import java.util.List;

@Data
public class StreamResponse {
    private String id;
    private String object;
    private long created;
    private String model;
    private List<Choice> choices;

    @Data
    public static class Choice {
        private int index;
        private Delta delta;
        private Object logprobs;
        private String finish_reason;
    }

    @Data
    public static class Delta {
        private String reasoning_content;
        private String content;
        private String role;
    }
}