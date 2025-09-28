<h1 id="oeroe">议题选择：</h1>

**<font style="color:rgb(0, 66, 175);">议题二：开发一个利用 AI 来做角色扮演的网页，用户可以搜索自己感兴趣的角色例如哈利波特、苏格拉底等，并可与其进行语音聊天。</font>**

**<font style="color:rgb(47, 47, 47);">请回答：</font>**

1. 你计划将这个网页面向什么类型的用户？这些类型的用户他们面临什么样的痛点，你设想的用户故事是什么样呢？
2. 你认为这个网页需要哪些功能？这些功能各自的优先级是什么？你计划本次开发哪些功能？
3. 你计划采纳哪家公司的哪个 LLM 模型能力？你对比了哪些，你为什么选择用该 LLM 模型？
4. 你期望 AI 角色除了语音聊天外还应该有哪些技能？

**<font style="color:rgb(136, 136, 136);">请开发以上网页，包括实现 3 个以上 AI 角色应该具备的技能。要求不能调用第三方的 Agent 能力，只需允许调用 LLM 模型、语音识别以及 TTS 能力。针对以上 1-4 点，请把你的思考整理成文档，作为作品的说明一并提交。</font>**



<h3 id="VFyyc"><font style="color:rgb(0, 0, 0);">用户定位分析</font></h3>
<font style="color:rgba(0, 0, 0, 0.85);">当代年轻人面临着高强度的工作压力，时常需要一个能随时倾诉、轻松互动的出口。针对这一需求，精卫 AI 语音助手可面向年轻用户群体，通过文字聊天、语音聊天、语音通话等多样化方式，为他们提供便捷、无负担的交流体验，帮助缓解工作带来的心理压力，成为日常放松、调节情绪的贴心陪伴。</font>

<h3 id="3b0cc656"><font style="color:rgb(0, 0, 0);">痛点分析</font></h3>
+ <font style="color:rgb(0, 0, 0);">年轻人在情绪低谷时，往往不需要复杂的解决方案，而是 “当下有人倾听”，但现有渠道难以满足 “随时找、立刻应” 的即时需求，导致负面情绪堆积。</font>
+ <font style="color:rgb(0, 0, 0);">年轻人对 “情绪隐私” 需求强烈，希望在交流中无需伪装、不用顾虑对方感受，可自由表达脆弱、焦虑甚至抱怨，但现实社交关系中 “有所保留” 成为常态，导致真实情绪被压抑。</font>
+ <font style="color:rgb(0, 0, 0);">现有渠道（如社交软件群聊、兴趣社群）多为 “群体性互动”，难以精准匹配个人当下的情绪状态和陪伴需求；而传统 AI 工具多偏向功能性（如日程提醒），缺乏 “懂情绪” 的适配性交流，无法提供 “按需陪伴”。</font>

<h3 id="c9e1eaee"><font style="color:rgb(0, 0, 0);">用户故事</font></h3>
<font style="color:rgb(0, 0, 0);">周四晚 9 点半，小王同学抱着电脑包在地铁上。刚加完班，客户临时改了广告创意，明天就要初稿，她盯着手机里 “团队群” 的未读消息，连叹气都没力气 —— 想跟朋友说 “好累”，怕对方嫌她抱怨；跟爸妈说，又怕他们担心。</font>

<font style="color:rgb(0, 0, 0);">她点开精卫 AI语音助手，发了条语音：“今天好难啊，加班到现在，客户还改需求，感觉快扛不住了。” 很快，温和的声音传来：“加班到这么晚，还得惦记明天的初稿，肯定又累又慌吧？要不要说说，客户改的地方让你最头疼的是啥？”</font>

<font style="color:rgb(0, 0, 0);">小王同学对着手机絮叨了 5 分钟，从 “改创意要重找素材” 说到 “怕赶不上进度”，精卫 AI语音助手偶尔回应：“要重新调整这么多，确实很消耗精力”“你愿意这么认真对待，已经很棒啦”。</font>

<font style="color:rgb(0, 0, 0);">地铁到站时，小王同学心里的闷堵散了大半。她对着精卫 AI语音助手说：“谢谢你听我说话，明天我再加油！” 走在回家的路上，她甚至想起了两个调整创意的小点子 —— 原来只是需要有人安安静静听她说说话，就有了继续的力气。</font>

<h2 id="U2wT5"><font style="color:rgb(0, 0, 0);">二、功能规划与优先级</font></h2>
<h3 id="2c62da87"><font style="color:rgb(0, 0, 0);">核心功能-高优先级</font></h3>
1. <font style="color:rgb(0, 0, 0);">用户登录与注册</font>
2. <font style="color:rgb(0, 0, 0);">角色搜索与选择 - 允许用户查找并选择 AI 角色</font>
3. <font style="color:rgb(0, 0, 0);">文本聊天-通过发信息的方式与AI交流</font>
4. <font style="color:rgb(0, 0, 0);">语音聊天 - 通过按住说话的方式与AI交流</font>
5. <font style="color:rgb(0, 0, 0);">语音通话 - 通过面对面与AI角色对话</font>
6. <font style="color:rgb(0, 0, 0);">专属语音 - 每个角色拥有符合其身份的独特声音(支持自定义)</font>
7. <font style="color:rgb(0, 0, 0);">聊天历史记录 - 保存并查看过往对话</font>

<h3 id="3c807ba9"><font style="color:rgb(0, 0, 0);">扩展功能</font></h3>
1. <font style="color:rgb(0, 0, 0);">向量数据库集成-根据过往历史记录推测用户画像</font>
2. <font style="color:rgb(0, 0, 0);">优化通话功能</font>
3. <font style="color:rgb(0, 0, 0);">选择特定场景进行对话</font>



<font style="background-color:rgba(137,244,115,1);">目前已完成全部核心功能的开发，并部署到服务器上，通过CICD持续集成，用户可以直接访问；  
</font><font style="background-color:rgba(137,244,115,1);">由于时间原因，扩展功能还未开发，后续考虑完善。  
</font>体验地址：  
[https://qiniu-ai-front.fengxianhub.top/](https://qiniu-ai-front.fengxianhub.top/)

<h2 id="4b73fa2a"><font style="color:rgb(0, 0, 0);">三、LLM 模型选择</font></h2>
<h3 id="72b03da4"><font style="color:rgb(0, 0, 0);">模型对比分析</font></h3>
| **<font style="color:rgb(0, 0, 0) !important;">模型</font>** | **<font style="color:rgb(0, 0, 0) !important;">优势</font>** | **<font style="color:rgb(0, 0, 0) !important;">劣势</font>** |
| :--- | :--- | :--- |
| <font style="color:rgba(0, 0, 0, 0.85) !important;">DeepSeek</font> | <font style="color:rgba(0, 0, 0, 0.85) !important;">角色扮演能力强，中文支持优秀，响应速度快</font> | <font style="color:rgba(0, 0, 0, 0.85) !important;">多轮对话一致性略逊于 GPT-4</font> |
| <font style="color:rgba(0, 0, 0, 0.85) !important;">GPT-4</font> | <font style="color:rgba(0, 0, 0, 0.85) !important;">综合能力最强，角色一致性好</font> | <font style="color:rgba(0, 0, 0, 0.85) !important;">成本高，国内访问不稳定</font> |
| <font style="color:rgba(0, 0, 0, 0.85) !important;">文心一言</font> | <font style="color:rgba(0, 0, 0, 0.85) !important;">中文理解优秀，知识更新及时</font> | <font style="color:rgba(0, 0, 0, 0.85) !important;">角色模仿能力一般</font> |
| <font style="color:rgba(0, 0, 0, 0.85) !important;">Llama 3</font> | <font style="color:rgba(0, 0, 0, 0.85) !important;">开源免费，可本地部署</font> | <font style="color:rgba(0, 0, 0, 0.85) !important;">需要自行优化，技术门槛高</font> |


通过对比各大厂商，我们决定选择deepseek模型，理由如下：

1. <font style="color:rgb(0, 0, 0);">角色扮演能力较为出色，能保持角色特征的一致性</font>
2. <font style="color:rgb(0, 0, 0);">理解与生成能力较强，适合各类角色对话</font>
3. <font style="color:rgb(0, 0, 0);">API 调用稳定，适合实时语音对话场景</font>
4. <font style="color:rgb(0, 0, 0);">性价比高，API 调用成本适中</font>
5. <font style="color:rgb(0, 0, 0);">在国内访问稳定，延迟低，适合实时交互</font>

<h2 id="4033d204"><font style="color:rgb(0, 0, 0);">四、AI 角色应该具备的技能</font></h2>
1. <font style="color:rgb(0, 0, 0);">文字聊天</font>
2. <font style="color:rgb(0, 0, 0);">语音聊天</font>
3. <font style="color:rgb(0, 0, 0);">语音通话</font>
4. <font style="color:rgb(0, 0, 0);">根据上下文生成图片</font>

<h2 id="670b1a81"><font style="color:rgb(0, 0, 0);">五、完整技术方案</font></h2>
<h3 id="adf5fad7"><font style="color:rgb(0, 0, 0);">技术架构</font></h3>
<font style="color:rgb(0, 0, 0);">采用分层架构设计：</font>

+ <font style="color:rgb(0, 0, 0);">表现层：WebRTC处理语音数据实时传输</font>
+ <font style="color:rgb(0, 0, 0);">业务层：处理语音识别、LLM 交互、语音合成、音色转换等核心业务</font>
+ <font style="color:rgb(0, 0, 0);">数据层：MongoDB、MySQL存储数据</font>
+ <font style="color:rgb(0, 0, 0);">集成层：与各第三方 API 对接</font>
+ <font style="color:rgb(0, 0, 0);">缓存层：Redis 缓存token、角色信息和会话状态</font>

<h4 id="Cjwxf">第三方服务技术选型</h4>
+ <font style="color:rgb(0, 0, 0);">语音识别：阿里 paraformer-realtime-8k-v2（可以将音频流实时转换为文本，实现“边说边出文字”的效果）</font>
+ <font style="color:rgb(0, 0, 0);">LLM 模型：DeepSeek</font>
+ <font style="color:rgb(0, 0, 0);">语音合成：阿里 Qwen3-TTS（提供多种拟人音色，支持多语言及方言，并可在同一音色下输出多语言内容）</font>
+ <font style="color:rgb(0, 0, 0);">音色转换：科大讯飞（基于深度学习技术，输入一段音频，能够自动合成各种不同音色的音频，包括多种男声、女声、童声，同时可以提供情感音色，使合成的语音更加接近人声。）</font>

**<font style="color:rgb(0, 0, 0) !important;">核心业务流程梳理</font>**<font style="color:rgb(0, 0, 0);">：</font>

+ <font style="color:rgb(0, 0, 0);">前端通过 WebRTC 发送语音数据</font>
+ <font style="color:rgb(0, 0, 0);">后端接收并合并音频分片</font>
+ <font style="color:rgb(0, 0, 0);">调用阿里云 ASR 将语音转为文本</font>
+ <font style="color:rgb(0, 0, 0);">获取到角色对应的特征值、提示词</font>
+ <font style="color:rgb(0, 0, 0);">使用 DeepSeek LLM 生成角色回应</font>
+ <font style="color:rgb(0, 0, 0);">通过阿里云 TTS  将文本转为语音</font>
+ <font style="color:rgb(0, 0, 0);">调用科大讯飞音色转换，使合成的语音更加接近人声</font>
+ <font style="color:rgb(0, 0, 0);">将最终语音返回给用户</font>

