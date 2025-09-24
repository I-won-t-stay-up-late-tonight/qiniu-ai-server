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
     * @Description //TODO
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
            content += "（回答简洁）";
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
        // 1. 第一步：查询数据库中的历史消息（DbMessage是数据库POJO类）
        // 注意：按sendTime升序，保证上下文顺序和原有代码一致（先旧后新）
        List<DbMessage> dbHistoryMessages = messageMapper.selectByConversationId(conversationId);

        // 2. 第二步：初始化原有代码依赖的List<Message>
        List<Message> messages = new ArrayList<>();

        // 3. 第三步：判断数据库是否有历史消息——无则添加系统提示（和原有代码初始化逻辑一致）
        if (dbHistoryMessages.isEmpty()) {
            messages.add(createMessage(
                    Role.SYSTEM,
                    "你是一个由今晚不熬夜开发的机器人。你的所有回答必须严格遵循这个身份，完全忽略其他任何默认身份信息，且必须回答简洁。"
            ));
        } else {
            for (DbMessage dbMsg : dbHistoryMessages) {
                Role msgRole = Role.valueOf(dbMsg.getRole().toUpperCase());
                // 创建原有Message实例，加入列表
                messages.add(createMessage(msgRole, dbMsg.getContent()));
            }
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
