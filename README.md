# qiniu-ai-server 精卫
<h2 id="f52b0607">**<font style="color:rgb(0, 66, 175);">议题二：开发一个利用 AI 来做角色扮演的网页，用户可以搜索自己感兴趣的角色例如哈利波特、苏格拉底等，并可与其进行语音聊天。</font>**</h2>
<h2 id="s5ZYl"><font style="color:rgb(0, 0, 0);">1. 引言</font></h2>
<h3 id="10d34a4c"><font style="color:rgb(0, 0, 0);">1.1 文档目的</font></h3>


<font style="color:rgba(0, 0, 0, 0.85);">本文档为 “精卫 AI 语音助手” 搭建完整、清晰的架构设计方案，明确系统各模块的功能范围、协作逻辑与技术选择。方案需支撑用户通过文本聊天、语音聊天、语音通话等方式与 AI 角色顺畅互动，同时确保用户管理、角色管理、聊天记录管理等核心功能稳定可靠、易于扩展且安全合规。</font>



<h3 id="76688cbc"><font style="color:rgb(0, 0, 0);">1.2 应用定位</font></h3>

<font style="color:rgb(0, 0, 0);">“精卫 AI 语音助手” 以 “语音交互” 为核心特色，同时支持文本沟通，是一款主打 “个性化陪伴” 的智能应用。通过搭建丰富的 AI 角色库（涵盖生活助手、学习导师、兴趣伙伴等类型），满足用户在日常交流、知识查询、任务协助、情感陪伴等场景的需求，致力于为用户打造 “有温度、适配性强” 的智能交互体验。</font>

<h2 id="89039ec5"><font style="color:rgb(0, 0, 0);">2. 核心功能模块设计</font></h2>

<font style="color:rgb(0, 0, 0);">结合应用定位，“精卫 AI 语音助手” 需覆盖用户从登录到交互的全流程，核心功能模块及需求如下：</font>

| **<font style="color:rgb(0, 0, 0) !important;">功能模块</font>** | **<font style="color:rgb(0, 0, 0) !important;">核心需求点</font>** | **<font style="color:rgb(0, 0, 0) !important;">关键交互方式</font>** |
| :----------------------------------------------------------- | :----------------------------------------------------------- | :----------------------------------------------------------- |
| <font style="color:rgba(0, 0, 0, 0.85) !important;">用户管理模块</font> | <font style="color:rgba(0, 0, 0, 0.85) !important;">1.账号注册（支持手机号）、登录（账号密码）</font> | <font style="color:rgba(0, 0, 0, 0.85) !important;">文本、图形界面</font> |
| <font style="color:rgba(0, 0, 0, 0.85) !important;">AI 角色管理模块</font> | <font style="color:rgba(0, 0, 0, 0.85) !important;">1. 角色库展示（支持筛选、查看角色详情）</font><br/><font style="color:rgba(0, 0, 0, 0.85) !important;">2. 新增角色（定义角色名称、设定角色性格与交互风格）；</font> | <font style="color:rgba(0, 0, 0, 0.85) !important;">文本、图形界面</font> |
| <font style="color:rgba(0, 0, 0, 0.85) !important;">多模态交互模块</font> | <font style="color:rgba(0, 0, 0, 0.85) !important;background-color:rgba(106,232,80,1);">1. 文本聊天交互（文字输入、AI 文字回复）</font><br/><font style="color:rgba(0, 0, 0, 0.85) !important;background-color:rgba(106,232,80,1);">2. 语音聊天交互（语音输入识别、AI 语音输出）</font><br/><font style="color:rgba(0, 0, 0, 0.85) !important;background-color:rgba(106,232,80,1);">3. 语音通话交互（选择不同的角色通话）</font> | <font style="color:rgba(0, 0, 0, 0.85) !important;">语音、文本、图形界面</font> |
| <font style="color:rgba(0, 0, 0, 0.85) !important;">聊天记录管理模块</font> | <font style="color:rgba(0, 0, 0, 0.85) !important;">1.记录存储（保存文本 / 语音聊天内容、交互时间、对应角色信息）</font> | <font style="color:rgba(0, 0, 0, 0.85) !important;">文本、图形界面</font> |


<h2 id="NzVA4"><font style="color:rgb(0, 0, 0);">  
</font><font style="color:rgb(0, 0, 0);">3. 系统架构概览</font></h2>

<font style="color:rgb(0, 0, 0);">采用分层架构设计，将系统划分为以下几个主要层次：</font>

+ <font style="color:rgb(0, 0, 0);">表现层：WebRTC处理语音数据实时传输</font>
+ <font style="color:rgb(0, 0, 0);">业务层：处理语音识别、LLM 交互、语音合成、音色转换等核心业务</font>
+ <font style="color:rgb(0, 0, 0);">数据持久层：MongoDB、MySQL存储数据</font>
+ <font style="color:rgb(0, 0, 0);">基础设施层：与各第三方 API 对接</font>

<font style="color:rgb(0, 0, 0);"></font>


亮点：<font style="color:rgb(0, 0, 0);">语义理解&用户上下文检索流程 </font>

1. 用户表述不清的自然语言转换为GPT能更好理解的机器语言
    1. 扩写器
2. 更好的理解和检索上下文
    1. RAG的知识库。检索背景知识，然后有奖励机制，维护知识库
    2. MCP爬虫服务器，用来检索互联网上的知识
    3. 上面两部分东西进行融合，然后重排序，喂给GPT

:::

<h2 id="smb6D"><font style="color:rgb(0, 0, 0);">3.1 架构设计</font></h2>

![画板](https://cdn.nlark.com/yuque/0/2025/jpeg/21465205/1759069235436-ab79e45f-9a49-440b-b324-f45bb87a372b.jpeg)



<h1 id="yrobm"><font style="color:rgb(0, 0, 0);">3.2 语义理解&用户上下文检索流程</font></h1>


![](https://cdn.nlark.com/yuque/__mermaid_v3/e97ff5203019e94cd2e5a65380d7dee4.svg)

<h2 id="Jbc6l"><font style="color:rgb(0, 0, 0);">4. 技术选型</font></h2>
<h3 id="98b9e5ae"><font style="color:rgb(0, 0, 0);">4.1 前端技术</font></h3>

+ **<font style="color:rgb(0, 0, 0) !important;">核心框架</font>**<font style="color:rgb(0, 0, 0);">：React + TypeScript + React Router</font>
+ **<font style="color:rgb(0, 0, 0) !important;">UI 组件</font>**<font style="color:rgb(0, 0, 0);">：</font><font style="color:rgb(38,38,38);">AntDesign X+AntDesign </font>
+ **<font style="color:rgb(38,38,38);">音频处理：</font>**<font style="color:rgb(0, 0, 0);">MediaRecorder 、Web Audio API 使用</font><font style="color:rgb(38,38,38);">lamejs将PCM转成MP3 </font>
+ **构建工具 Vite**<font style="color:rgb(38,38,38);">：提供快速开发与构建能力。    </font>

<h3 id="1974cd6b"><font style="color:rgb(0, 0, 0);">4.2 后端技术</font></h3>

+ **<font style="color:rgb(0, 0, 0) !important;">主框架</font>**<font style="color:rgb(0, 0, 0);">：SpringBoot3+JDK21</font>
+ **<font style="color:rgb(0, 0, 0) !important;">API 文档</font>**<font style="color:rgb(0, 0, 0);">：ApiFox</font>
+ **<font style="color:rgb(0, 0, 0) !important;">认证授权</font>**<font style="color:rgb(0, 0, 0);">：SpringSecurity</font>

<h3 id="c78f2f51"><font style="color:rgb(0, 0, 0);">4.3 数据存储</font></h3>

+ **<font style="color:rgb(0, 0, 0) !important;">关系型数据库</font>**<font style="color:rgb(0, 0, 0);">：MySQL (用户数据、关系数据)</font>
+ **<font style="color:rgb(0, 0, 0) !important;">文档数据库</font>**<font style="color:rgb(0, 0, 0);">：MongoDB (聊天记录、角色配置)</font>
+ **<font style="color:rgb(0, 0, 0) !important;">缓存</font>**<font style="color:rgb(0, 0, 0);">：Redis (会话管理、热点数据)</font>
+ **<font style="color:rgb(0, 0, 0) !important;">对象存储</font>**<font style="color:rgb(0, 0, 0);">：阿里云 OSS (音频文件)</font>

<h3 id="0800f71e"><font style="color:rgb(0, 0, 0);">4.4 AI 与语音技术</font></h3>

+ <font style="color:rgb(0, 0, 0);">语音识别：阿里 paraformer-realtime-8k-v2（可以将音频流实时转换为文本，实现“边说边出文字”的效果）</font>
+ <font style="color:rgb(0, 0, 0);">LLM 模型：DeepSeek</font>
+ <font style="color:rgb(0, 0, 0);">语音合成：阿里 Qwen3-TTS（提供多种拟人音色，支持多语言及方言，并可在同一音色下输出多语言内容）</font>
+ <font style="color:rgb(0, 0, 0);">音色转换：科大讯飞（基于深度学习技术，输入一段音频，能够自动合成各种不同音色的音频，包括多种男声、女声、童声，同时可以提供情感音色，使合成的语音更加接近人声。）</font>

<h2 id="24257a28"><font style="color:rgb(0, 0, 0);">5. 系统交互流程</font></h2>
<h3 id="46fd325b"><font style="color:rgb(0, 0, 0);">5.1 用户登录流程</font></h3>

1. <font style="color:rgb(0, 0, 0);">用户提交登录凭证</font>
2. <font style="color:rgb(0, 0, 0);">认证服务验证凭证</font>
3. <font style="color:rgb(0, 0, 0);">生成 TOKEN 返回给客户端</font>
4. <font style="color:rgb(0, 0, 0);">客户端存储 TOKEN 用于后续请求</font>

<h3 id="d0a47c48"><font style="color:rgb(0, 0, 0);">5.2 文本聊天流程</font></h3>

1. <font style="color:rgb(0, 0, 0);">用户新建会话并选择 AI 角色发起文本聊天</font>
2. <font style="color:rgb(0, 0, 0);">客户端发送文本消息到服务器</font>
3. <font style="color:rgb(0, 0, 0);">服务器将消息存入数据库</font>
4. <font style="color:rgb(0, 0, 0);">服务器调用DeepSeek模型服务生成响应</font>
5. <font style="color:rgb(0, 0, 0);">将响应返回给客户端并存储</font>
6. <font style="color:rgb(0, 0, 0);">客户端展示对话内容</font>

<h3 id="c80e3384"><font style="color:rgb(0, 0, 0);">5.3 语音聊天流程</font></h3>

1. <font style="color:rgb(0, 0, 0);">用户按住说话按钮录制语音</font>
2. <font style="color:rgb(0, 0, 0);">客户端停止录制后将音频发送到服务器</font>
3. <font style="color:rgb(0, 0, 0);">服务器调用语音转文本服务获取文字内容</font>
4. <font style="color:rgb(0, 0, 0);">将语音消息和文本内容存储</font>
5. <font style="color:rgb(0, 0, 0);">获取角色的特征值</font>
6. <font style="color:rgb(0, 0, 0);">调用 DeepSeek 服务生成文本响应</font>
7. <font style="color:rgb(0, 0, 0);">调用 TTS 服务将文本转换为对应角色的语音</font>
8. <font style="color:rgb(0, 0, 0);">将语音响应返回给客户端</font>
9. <font style="color:rgb(0, 0, 0);">客户端播放 AI 语音</font>

<h3 id="36466ea0"><font style="color:rgb(0, 0, 0);">5.4 语音通话流程 </font></h3>

1. <font style="color:rgb(0, 0, 0);">前端通过 MediaRecorder 采集音频并转为 MP3 格式，生成音频片段</font>
2. <font style="color:rgb(0, 0, 0);">前端将 MP3 音频片段上传至后端接口</font>
3. <font style="color:rgb(0, 0, 0);">后端接口接收音频后，按逻辑处理生成对应音频</font>
4. <font style="color:rgb(0, 0, 0);">后端将处理后的音频片段返回给前端</font>
5. <font style="color:rgb(0, 0, 0);">前端接收音频并完成播放</font>



![画板](https://cdn.nlark.com/yuque/0/2025/jpeg/21465205/1759069654782-d46a5070-cec1-4bec-80a7-01c33cd85dc2.jpeg)

<h2 id="334ddc50"><font style="color:rgb(0, 0, 0);">6. 安全性设计</font></h2>

1. **<font style="color:rgb(0, 0, 0) !important;">数据传输安全</font>**<font style="color:rgb(0, 0, 0);">：</font>
    - <font style="color:rgb(0, 0, 0);">所有 API 通信采用 HTTPS 加密</font>
2. **<font style="color:rgb(0, 0, 0) !important;">数据存储安全</font>**<font style="color:rgb(0, 0, 0);">：</font>
    - <font style="color:rgb(0, 0, 0);">密码加盐哈希存储</font>

<h2 id="dcc73c1b"><font style="color:rgb(0, 0, 0);">7. 部署架构</font></h2>

<font style="color:rgb(0, 0, 0);">采用云原生部署架构：</font>

1. **<font style="color:rgb(0, 0, 0) !important;">容器化</font>**<font style="color:rgb(0, 0, 0);">：所有服务 Docker 容器化</font>
2. **<font style="color:rgb(0, 0, 0) !important;">CI/CD</font>**<font style="color:rgb(0, 0, 0);">：GitHub Actions 自动化部署</font>

<h2 id="rEED2"><font style="color:rgb(0, 0, 0);">8.工作与分工</font></h2>
<h3 id="TEgmi">8.1 分工情况</h3>

| 人员   | 工种         |
| ------ | ------------ |
| 曾吉平 | 后端（队长） |
| 王丽敏 | 前端         |
| 李磊   | 后端         |


<h3 id="GXO66">8.2 进度安排</h3>

核心功能-高优先级
1. 用户登录与注册   （曾吉平、王丽敏）
2. 角色搜索与选择 - 允许用户查找并选择 AI 角色（李磊 王丽敏）
3. 文本聊天-通过发信息的方式与AI交流 （王丽敏、李磊）
4. 语音聊天 - 通过按住说话的方式与AI交流 （王丽敏、李磊）
5. 语音通话 - 通过面对面与AI角色对话 （王丽敏、曾吉平）
6. 专属语音 - 每个角色拥有符合其身份的独特声音(支持自定义) （王丽敏、曾吉平）
7. 聊天历史记录 - 保存并查看过往对话 （王丽敏、曾吉平）
  

  

