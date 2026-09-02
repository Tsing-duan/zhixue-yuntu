# 面试代码讲解索引

建议从下面 5 个点中按面试官兴趣选 2～3 个讲。每个点都包含真实实现边界，避免把已验证的局部治理扩写成全站能力。

## 1. Provider 响应合同：DeepSeekClient

**文件路径**

- [`DeepSeekClient.java`](../learn_server-master/ape-system/src/main/java/com/ape/apesystem/service/ai/DeepSeekClient.java)
- [`AiGenerationResult.java`](../learn_server-master/ape-system/src/main/java/com/ape/apesystem/service/ai/AiGenerationResult.java)
- [`DeepSeekClientContractTest.java`](../learn_server-master/ape-admin/src/test/java/com/ape/apeadmin/ai/DeepSeekClientContractTest.java)

**解决的问题**：把六个生成入口对外部 Provider 的请求、成功定义、错误分类和敏感字段边界收敛到一个 client，避免 Controller 各自解析和误记失败结果。

**核心设计与取舍**

- 正常环境只允许官方 HTTPS host；测试环境只额外允许 loopback fake server。
- 单次请求、零自动重试。只有 HTTP 200、唯一 choice、`finish_reason=stop`、无工具调用、非空 content、完整且自洽 usage 才返回成功。
- 内部结果只保存 content 和 usage，没有 `reasoning_content` 字段；错误映射为分类结果与安全中文提示。
- 选择显式合同而不是“尽量解析”，是为了让历史和用量统计只接受完整生成；代价是部分可恢复响应也会 fail closed。

**可能追问**

1. 为什么不自动 retry？——生成请求可能昂贵且非幂等；当前更重视可预测调用预算。若以后增加，需要请求幂等键、退避和可观测性一起设计。
2. 为什么 `finish_reason=length` 不落历史？——截断内容不是完整教学建议，写成成功会污染历史与统计；应在有证据后调整 max tokens，再重新请求。
3. 如何证明推理内容没有泄露？——业务结果类型不承载该字段；响应、数据库和日志均做过匹配检查。这个结论不要求假设每次原始响应都一定含该字段。

## 2. 错题 Agent：上下文、额度、冷却与历史

**文件路径**

- [`AiController.java`](../learn_server-master/ape-admin/src/main/java/com/ape/apeadmin/controller/ai/AiController.java)
- [`AiServiceImpl.java`](../learn_server-master/ape-system/src/main/java/com/ape/apesystem/service/impl/AiServiceImpl.java)
- [`ApeAiHistory.java`](../learn_server-master/ape-system/src/main/java/com/ape/apesystem/domain/ApeAiHistory.java)
- [`AiGenerationEndpointContractTest.java`](../learn_server-master/ape-admin/src/test/java/com/ape/apeadmin/ai/AiGenerationEndpointContractTest.java)

**解决的问题**：从当前学生自己的作业和考试错题构造可解释上下文，同时在真实 Provider 之前完成身份、额度和频控，成功后留下可核对的历史与 token。

**核心设计与取舍**

- 服务端以当前 principal 查询本人错题；客户端传入的题目 ID 只能在本人记录集合中筛选。
- 六个入口先走统一请求校验。每日成功历史提供调用数、实际 token 和最近成功时间；JVM 内存 guard 预留进行中的短期冷却。
- 上下文与角色 Prompt 在 `AiServiceImpl` 分区组合；只有完整成功才保存回复、上下文快照和 prompt/completion token。
- 数据库历史能跨重启恢复最近成功冷却，但当前 JVM guard 不是分布式锁，多实例并发仍是边界。

**可能追问**

1. 为什么冷却要“数据库 + 内存”两层？——数据库覆盖最近成功并能跨重启；内存 guard 能在首个请求尚未写历史时拦住同进程并发重复提交。
2. 如何避免学生请求别人的错题？——身份来自 Shiro principal，查询先固定当前 userId，再对可选 ID 做本人集合内过滤。
3. token 额度可靠吗？——最终统计用 Provider usage；请求前只用字符数做保守预留，不把近似值冒充真实 tokenizer 结果。

## 3. 教学资源 ownership：ResourceAuthorizationService

**文件路径**

- [`ResourceAuthorizationService.java`](../learn_server-master/ape-admin/src/main/java/com/ape/apeadmin/security/ResourceAuthorizationService.java)
- [`ApeTaskController.java`](../learn_server-master/ape-admin/src/main/java/com/ape/apeadmin/controller/task/ApeTaskController.java)
- [`ResourceAuthorizationServiceTest.java`](../learn_server-master/ape-admin/src/test/java/com/ape/apeadmin/security/ResourceAuthorizationServiceTest.java)

**解决的问题**：仅判断“教师角色”不能阻止教师通过替换资源 ID 操作别人的课程；需要继续验证具体资源归属，关闭教学主链路 IDOR。

**核心设计与取舍**

- ownership 最终收敛为课程 `teacherId` 是否等于当前教师 ID；章节、作业、考试和题目沿父资源回溯到课程。
- Controller 的角色注解回答“能否进入这类操作”，资源守卫回答“能否操作这个对象”。缺少身份、角色、资源或归属时统一 fail closed。
- 新建/编辑课程时由服务端覆盖教师 ID，不接受客户端转移 ownership。
- 守卫是显式调用而不是全局魔法；容易审计，但新增写入口必须主动接入。文章/论坛等不在本轮闭包内。

**可能追问**

1. 为什么不能只用 `@RequiresRoles("teacher")`？——角色粒度只到资源类型，无法区分同角色用户之间的横向越权。
2. 父链中间资源不存在怎么办？——拒绝而不是跳过校验，避免缺失关系导致 fallback allow。
3. 如何验证不是只隐藏了按钮？——浏览器以教师身份向另一名合成教师的 canary 课程发出真实写请求，服务端返回 403，数据库随后确认目标未变。

## 4. Shiro 方法级角色授权与 JWT 会话

**文件路径**

- [`ShiroConfig.java`](../learn_server-master/ape-framework/src/main/java/com/ape/apeframework/config/ShiroConfig.java)
- [`JwtFilter.java`](../learn_server-master/ape-framework/src/main/java/com/ape/apeframework/filter/JwtFilter.java)
- [`ShiroRealm.java`](../learn_server-master/ape-framework/src/main/java/com/ape/apeframework/config/ShiroRealm.java)
- [`JwtSecretConfig.java`](../learn_server-master/ape-framework/src/main/java/com/ape/apeframework/config/JwtSecretConfig.java)
- [`MethodAuthorizationRequestTest.java`](../learn_server-master/ape-admin/src/test/java/com/ape/apeadmin/security/MethodAuthorizationRequestTest.java)

**解决的问题**：既有系统有登录认证，但高风险 Controller 缺少一致的服务端角色门；同时源码固定 secret 不适合作品库。

**核心设计与取舍**

- 除明确白名单外，请求进入 JWT filter/realm；Shiro 关闭服务端 session，Redis 保存登录 token 与权限缓存。
- secret 从外部注入，缺失或少于 32 字符时启动失败；角色来自数据库关系与服务端 userType 的 canonical 映射。
- 方法级 `@RequiresRoles` 由 Shiro advisor 执行；管理员不默认继承教师/学生权限，需要跨角色时显式声明 OR。
- 401 表示身份无效，403 表示身份有效但权限不足；请求级测试验证注解确实经过真实 HTTP 链。
- 当前 realm 对来路 token 先解码 userId，再以 Redis 中缓存 token 判断会话有效；它没有逐请求比较来路 token 与缓存值，也不能表述为“严格验证每个 presented token 的签名”。这是后续安全债务。

**可能追问**

1. 认证和授权有什么区别？——认证建立“是谁”，角色注解决定“能否进入某类操作”，ownership 再决定“能否操作这个对象”。
2. 为什么 secret 缺失要阻止启动？——弱默认值会让部署看似成功但安全失效；fail-fast 更容易诊断，也避免生产静默降级。
3. 当前 token 校验还应如何加固？——对 presented token 本身做签名/过期验证，并与 Redis 当前 token 做常量时间等价判断；补伪造签名、旧 token 和轮换场景的请求级测试。

## 5. 用户写入与敏感字段边界

**文件路径**

- [`ApeUserController.java`](../learn_server-master/ape-admin/src/main/java/com/ape/apeadmin/controller/user/ApeUserController.java)
- [`ApeUser.java`](../learn_server-master/ape-system/src/main/java/com/ape/apesystem/domain/ApeUser.java)
- [`LoginController.java`](../learn_server-master/ape-admin/src/main/java/com/ape/apeadmin/controller/login/LoginController.java)
- [`ApeUserSensitiveJsonTest.java`](../learn_server-master/ape-admin/src/test/java/com/ape/apeadmin/security/ApeUserSensitiveJsonTest.java)

**解决的问题**：直接回传或整体更新用户实体会泄露认证材料，也可能让客户端修改身份、状态、角色等服务端字段。

**核心设计与取舍**

- 自助资料更新从当前 principal 取得 ID，再构造只包含允许资料字段的新实体，避免 `updateById(requestBody)` 式 mass assignment。
- `password` 为 JSON write-only，允许注册/改密输入但不出现在响应；`salt` 完全忽略 JSON 读写。
- 登录兼容旧 MD5 记录，验证成功后升级为 BCrypt 并清空 salt；公开注册拒绝创建管理员类型。
- 选择小范围白名单与序列化注解，避免为两个字段重构全部 DTO；边界是公开注册仍绑定完整 `ApeUser`，不能声称所有用户写入口都已 DTO 化。

**可能追问**

1. BCrypt hash 为什么也不能返回？——它仍是离线破解材料，客户端业务也不需要；最小暴露原则要求从响应合同移除。
2. write-only 与 ignore 有何不同？——write-only 允许反序列化输入但禁止序列化输出；ignore 对客户端读写都关闭，适合不应再由客户端提供的 salt。
3. 为什么没有立刻全量 DTO 重构？——本阶段目标是最小、可验证的高风险修复；白名单已保护自助资料入口，同时明确记录注册实体绑定这一剩余边界。
