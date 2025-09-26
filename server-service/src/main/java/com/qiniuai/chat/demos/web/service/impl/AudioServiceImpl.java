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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName audioServiceImpl
 * @Description TODO
 * @Author IFundo
 * @Date 00:06 2025/9/23
 * @Version 1.0
 */

@Slf4j
@Service
public class AudioServiceImpl implements AudioService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private ConversationMapper conversationMapper;

    @Autowired
    private ConversationRoleRelationMapper conversationRoleRelationMapper;

    // 实例和常量
    private static final String ASR_MODEL = "qwen3-asr-flash";
    private static final String ASR_OPTIONS_KEY = "asr_options";
    private static final String DEFAULT_ERROR_MSG = "解析失败";
    @Value("${ali.audio.asr.api-key}")  // 从配置文件注入
    private String apiKey;

    //会话模型
    private final MultiModalConversation conversationClient;
    // 创建ObjectMapper实例
    private ObjectMapper objectMapper = new ObjectMapper();

    // 初始化客户端
    public AudioServiceImpl() {
        this.conversationClient = new MultiModalConversation();
    }


    @Override
    public String audio2text(MultipartFile audio) {
        try {
            // 1. 处理音频文件
            byte[] audioBytes = audio.getBytes();
            String base64Audio = Base64.getEncoder().encodeToString(audioBytes);
            String audioContentType = audio.getContentType();
            String audioDataUrl = "data:" + audioContentType + ";base64," + base64Audio;

            // 2. 构建请求参数
            MultiModalConversationParam param = buildConversationParam(audioDataUrl);

            // 3. 调用API
            MultiModalConversationResult result = conversationClient.call(param);

            // 4. 处理结果
            String resJson = JsonUtils.toJson(result);
            String text = jsonTextExtractor(resJson);

            return text != null ? text : DEFAULT_ERROR_MSG;

        } catch (IOException e) {
            log.error("音频文件处理异常", e);
            throw new RuntimeException("音频文件处理失败: " + e.getMessage());
        } catch (NoApiKeyException e) {
            log.error("API Key配置异常", e);
            throw new RuntimeException("语音识别服务未配置API Key");
        } catch (UploadFileException e) {
            log.error("音频上传异常", e);
            throw new RuntimeException("音频上传失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("语音识别处理异常", e);
            throw new RuntimeException("语音识别处理失败");
        }
    }

    /**
     * 构建对话参数
     */
    private MultiModalConversationParam buildConversationParam(String audioDataUrl) {
        // 构建用户消息
        MultiModalMessage userMessage = MultiModalMessage.builder()
                .role(Role.USER.getValue())
                .content(Collections.singletonList(
                        Collections.singletonMap("audio", audioDataUrl)
                ))
                .build();

        // 构建系统消息
        MultiModalMessage sysMessage = MultiModalMessage.builder()
                .role(Role.SYSTEM.getValue())
                .content(Collections.singletonList(
                        Collections.singletonMap("text", "")
                ))
                .build();

        // 构建ASR配置
        Map<String, Object> asrOptions = buildAsrOptions();

        // 构建请求参数
        return MultiModalConversationParam.builder()
                .apiKey(apiKey)
                .model(ASR_MODEL)
                .message(userMessage)
                .message(sysMessage)
                .parameter(ASR_OPTIONS_KEY, asrOptions)
                .build();
    }

    /**
     * 构建ASR识别选项
     */
    private Map<String, Object> buildAsrOptions() {
        Map<String, Object> asrOptions = new HashMap<>();
        asrOptions.put("enable_lid", true);
        asrOptions.put("enable_itn", false);
        // 可根据需要添加更多配置
        return asrOptions;
    }

    @Override
    public String text2audio(String content){
        String MODEL = "qwen3-tts-flash";
        MultiModalConversation conv = new MultiModalConversation();
        MultiModalConversationParam param = MultiModalConversationParam.builder()
                .apiKey(apiKey)
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
            log.info("用户输入：" + content);
            log.info("模型输出：" + modelOutput);
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
    @Async
    public void batchSaveMessageToDb(long conversationId, List<DbMessage> dbMessages) {
        // 统一设置conversationId和sendTime（若未设置）
        dbMessages.forEach(msg -> {
            msg.setConversationId(conversationId);
            if (msg.getSendTime() == null) {
                msg.setSendTime(LocalDateTime.now()); // 或改用数据库生成时间
            }
        });
        messageMapper.batchInsertMessage(dbMessages); // 批量插入Mapper方法
    }

    private List<Message> loadDbHistoryToLocalMessages(long conversationId) {
        // 1. 查询对话绑定的角色（仅1次）
        com.qiniuai.chat.demos.web.entity.pojo.Role currentRole = conversationRoleRelationMapper.selectByConversationId(conversationId);
        // 2. 查历史消息
        List<DbMessage> dbHistoryMessages = messageMapper.selectByConversationId(conversationId);
        // 优化点1：预先计算集合容量，避免动态扩容
        int initialCapacity = 1 + (dbHistoryMessages != null ? dbHistoryMessages.size() : 0);
        List<Message> messages = new ArrayList<>(initialCapacity);
        // 3. 添加系统提示
        messages.add(createMessage(Role.SYSTEM, currentRole.toString()));
        // 4. 添加历史消息（减少循环内临时变量）
        if (dbHistoryMessages != null && !dbHistoryMessages.isEmpty()) {
            Role msgRole;
            String content;
            for (DbMessage dbMsg : dbHistoryMessages) {
                // 优化点2：减少循环内的临时变量创建和字符串处理
                msgRole = Role.valueOf(dbMsg.getRole().toUpperCase());
                content = dbMsg.getContent();
                messages.add(createMessage(msgRole, content));
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
                .apiKey(apiKey)
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
