package com.qiniuai.chat.web.service;

import com.qiniuai.chat.web.dto.XunfeiTtsRequest;

public interface IXunfeiTtsService {
    byte[] textToSpeech(XunfeiTtsRequest request) throws Exception;
}
