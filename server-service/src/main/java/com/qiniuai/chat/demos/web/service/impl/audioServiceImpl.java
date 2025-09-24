package com.qiniuai.chat.demos.web.service.impl;

import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.aigc.multimodalconversation.AudioParameters;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.alibaba.dashscope.utils.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.qiniuai.chat.demos.web.entity.pojo.DbMessage;
import com.qiniuai.chat.demos.web.mapper.ConversationMapper;
import com.qiniuai.chat.demos.web.mapper.ConversationRoleRelationMapper;
import com.qiniuai.chat.demos.web.mapper.MessageMapper;
import com.qiniuai.chat.demos.web.service.AudioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @ClassName audioServiceImpl
 * @Description TODO
 * @Author IFundo
 * @Date 00:06 2025/9/23
 * @Version 1.0
 */

@Service
public class audioServiceImpl implements AudioService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private ConversationMapper conversationMapper;

    @Autowired
    private ConversationRoleRelationMapper conversationRoleRelationMapper;

    @Override
    public String audio2text(MultipartFile audio) {

        byte[] audioBytes = new byte[0];
        try {
            audioBytes = audio.getBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String base64Audio = Base64.getEncoder().encodeToString(audioBytes);

        // 构建包含Base64音频频的内容（格式：data:audio/[格式];base64,[编码内容]）
        String audioContentType = audio.getContentType(); // 获取音频MIME类型（如audio/mpeg）
        String audioDataUrl = "data:" + audioContentType + ";base64," + base64Audio;

        MultiModalConversation conv = new MultiModalConversation();
        MultiModalMessage userMessage = MultiModalMessage.builder()
                .role(Role.USER.getValue())
                .content(Arrays.asList(
                        Collections.singletonMap("audio", audioDataUrl) // 使用转换后的音频数据
                ))
                .build();

        MultiModalMessage sysMessage = MultiModalMessage.builder().role(Role.SYSTEM.getValue())
                // 此处用于配置定制化识别的Context
                .content(Arrays.asList(Collections.singletonMap("text", "")))
                .build();

        Map<String, Object> asrOptions = new HashMap<>();
        asrOptions.put("enable_lid", true);
        asrOptions.put("enable_itn", false);
        // asrOptions.put("language", "zh"); // 可选，若已知音频的语种，可通过该参数指定待识别语种，以提升识别准确率

        MultiModalConversationParam param = MultiModalConversationParam.builder()
                // 若没有配置环境变量，请用百炼API Key将下行替换为：.apiKey("sk-xxx")
                .apiKey("sk-95513ded49764ba6a533d427797b6f20")
                .model("qwen3-asr-flash")
                .message(userMessage)
                .message(sysMessage)
                .parameter("asr_options", asrOptions)
                .build();

        MultiModalConversationResult result = null;
        try {
            result = conv.call(param);
        } catch (NoApiKeyException e) {
            throw new RuntimeException(e);
        } catch (UploadFileException e) {
            throw new RuntimeException(e);
        }
        String resJson = JsonUtils.toJson(result);
        String test = jsonTextExtractor(resJson);
        return test != null ? test : "解析失败";

    }

    @Override
    public String text2audio(String content){
        String MODEL = "qwen3-tts-flash";
        MultiModalConversation conv = new MultiModalConversation();
        MultiModalConversationParam param = MultiModalConversationParam.builder()
                .apiKey("sk-95513ded49764ba6a533d427797b6f20")
                .model(MODEL)
                .text(content)
                .voice(AudioParameters.Voice.CHERRY)
                .languageType("Chinese") // 建议与文本语种一致，以获得正确的发音和自然的语调。
                .build();
        MultiModalConversationResult result = null;
        String audioUrl = null;
        try {
            result = conv.call(param);
            audioUrl = result.getOutput().getAudio().getUrl();
        } catch (NoApiKeyException e) {
            throw new RuntimeException(e);
        } catch (UploadFileException e) {
            throw new RuntimeException(e);
        }

        System.out.println("audioUrl = " + audioUrl);

        // 下载音频文件到本地
//        try (InputStream in = new URL(audioUrl).openStream();
//             FileOutputStream out = new FileOutputStream("downloaded_audio.wav")) {
//            byte[] buffer = new byte[1024];
//            int bytesRead;
//            while ((bytesRead = in.read(buffer)) != -1) {
//                out.write(buffer, 0, bytesRead);
//            }
//            System.out.println("\n音频文件已下载到本地: downloaded_audio.wav");
//        } catch (Exception e) {
//            System.out.println("\n下载音频文件时出错: " + e.getMessage());
//        }
        return audioUrl != null ? audioUrl : null;
    }

    /*
     * @Date 21:33 2025/9/23
     * @Description //TODO 提升效率
     * @Author IFundo
     * @conversationId 会话id
     * @content 发送的内容
     *
     */
    @Override
    public String chat(String content, long conversationId) {
        String modelOutput = null;

        try {
            List<Message> messages = loadDbHistoryToLocalMessages(conversationId);
            messages.add(createMessage(Role.USER, content));
            GenerationParam param = createGenerationParam(messages);
            GenerationResult result = callGenerationWithMessages(param);
            modelOutput = result.getOutput().getChoices().get(0).getMessage().getContent();
            System.out.println("用户输入：" + content);
            System.out.println("模型输出：" + modelOutput);
            messages.add(result.getOutput().getChoices().get(0).getMessage());
            saveMessageToDb(conversationId, Role.USER, content);
            saveMessageToDb(conversationId, Role.ASSISTANT, modelOutput);
        } catch (ApiException | NoApiKeyException | InputRequiredException e) {
            e.printStackTrace();
        }
        return modelOutput;
    }

    private void saveMessageToDb(long conversationId, Role role, String content) {
        DbMessage dbMsg = new DbMessage();
        dbMsg.setConversationId(conversationId);
        dbMsg.setRole(role.name()); // 枚举转字符串，匹配数据库字段
        dbMsg.setContent(content);
        dbMsg.setSendTime(LocalDateTime.now());
        messageMapper.insertMessage(dbMsg); // 调用Mapper的插入方法
    }

    private List<Message> loadDbHistoryToLocalMessages(long conversationId) {

        // 第一步：准备好历史消息和消息模型队列
        List<DbMessage> dbHistoryMessages = messageMapper.selectByConversationId(conversationId);
        List<Message> messages = new ArrayList<>();

        // 第二步：处理历史消息（若有）
        if (!dbHistoryMessages.isEmpty()) {
            // 将数据库历史消息转换为模型需要的 Message 格式
            for (DbMessage dbMsg : dbHistoryMessages) {
                Role msgRole = Role.valueOf(dbMsg.getRole().toUpperCase());
                messages.add(createMessage(msgRole, dbMsg.getContent()));
            }

            // 第三步：检查历史消息中是否已包含系统提示（仅在有历史时才检查）
            if (!Role.SYSTEM.equals(messages.get(0).getRole())) {
                // 若没有系统提示，添加角色的系统限定词（从 currentRole 获取）
                com.qiniuai.chat.demos.web.entity.pojo.Role currentRole = conversationRoleRelationMapper.selectByConversationId(conversationId);
                messages.add(0, createMessage(Role.SYSTEM, currentRole.toString()));
            }
        } else {
            // 第四步：若没有历史消息，直接添加系统提示（此时 messages 为空，无需检查）
            com.qiniuai.chat.demos.web.entity.pojo.Role currentRole = conversationRoleRelationMapper.selectByConversationId(conversationId);
            messages.add(createMessage(Role.SYSTEM, currentRole.toString()));
        }

        return messages;
    }

    @Override
    public String audioChat(MultipartFile audio, long id) {
        String inputStr = audio2text(audio);
        if (inputStr == null){
            return "解析失败";
        }
        String resStr = chat(inputStr, id);
        String resUrl = text2audio(resStr);
        return resUrl;
    }

    private String jsonTextExtractor(String resJson){
        try {
            // 创建ObjectMapper实例
            ObjectMapper objectMapper = new ObjectMapper();
            // 解析JSON字符串为JsonNode
            JsonNode rootNode = objectMapper.readTree(resJson);

            // 按照JSON路径提取text内容
            String text = rootNode
                    .path("output")
                    .path("choices")
                    .get(0)  // 获取数组第一个元素
                    .path("message")
                    .path("content")
                    .get(0)  // 获取数组第一个元素
                    .path("text")
                    .asText();  // 转换为字符串

           return text;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private GenerationParam createGenerationParam(List<Message> messages) {
        return GenerationParam.builder()
                // 若没有配置环境变量，请用阿里云百炼API Key将下行替换为：.apiKey("sk-xxx")
                .apiKey("sk-95513ded49764ba6a533d427797b6f20")
                // 模型列表：https://help.aliyun.com/zh/model-studio/getting-started/models
                .model("qwen-plus")
                .messages(messages)
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .build();
    }
    private GenerationResult callGenerationWithMessages(GenerationParam param) throws ApiException, NoApiKeyException, InputRequiredException {
        Generation gen = new Generation();
        return gen.call(param);
    }
    private  Message createMessage(Role role, String content) {
        return Message.builder().role(role.getValue()).content(content).build();
    }

}
