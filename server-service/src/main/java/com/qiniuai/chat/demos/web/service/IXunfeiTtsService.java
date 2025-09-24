package com.qiniuai.chat.demos.web.service;

import com.qiniuai.chat.demos.web.dto.XunfeiTtsRequest;

public interface IXunfeiTtsService {
    byte[] textToSpeech(XunfeiTtsRequest request) throws Exception;
}
