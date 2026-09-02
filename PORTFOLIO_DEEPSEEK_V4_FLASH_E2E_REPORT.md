# DeepSeek V4 Flash 纵向验证报告

最终状态：`DEEPSEEK_V4_FLASH_E2E_PASS`

## Migration

- 起始分支为 `main`，起始 HEAD 为 `bc97b4d18ab259a0898a72d62c300468761be934`，工作区 clean，remote 为空。
- Provider 迁移提交 `e1b82038e385813d3e4178c8056b8169be15f4ae` 保持冻结；确定性 Demo 凭据提交为 `bc97b4d18ab259a0898a72d62c300468761be934`。
- 本轮未修改业务代码、冷却时间、数据库 schema、前端依赖或 lockfile，也未创建 fix commit。
- 六个生成入口仍为 `/ai/chat`、`/ai/study-plan`、`/ai/wrong-agent`、`/ai/lesson-prepare`、`/ai/course-summary`、`/ai/data-agent`。本轮真实 Provider 纵向验证只覆盖学生 `/ai/wrong-agent` 与教师 `/ai/data-agent` 的课程健康度入口；其余入口由既有确定性请求级合同测试覆盖。

## Configuration

- 实际环境：macOS 26.6.2、OpenJDK 17.0.19、Maven 3.9.16、Node.js 26.7.0、npm 11.19.0、MySQL 8.4.11、Redis 8.10.1。
- 运行端口：学生端 `3001`，教师/管理端 `3000`，后端 `8080`，隔离 MySQL `3307`；Redis 使用独立 DB 14。
- DeepSeek 目标仅为 `https://api.deepseek.com`，模型为 `deepseek-v4-flash`，思考模式启用，reasoning effort 为 high，最大输出 token 配置为 4096，连接/读取超时为 5 秒/120 秒，重试为 0。
- API Key、Demo 密码和临时 JWT 仅由仓库外受限文件或进程环境注入，未写入源码、配置、报告、截图或日志。
- 在启动真实 Provider 阶段前，以项目兼容的 BCrypt 实现验证管理员、教师、学生三名合成用户均匹配本轮 Demo 密码；未输出密码或哈希。

## Endpoint Coverage

- 学生：通过 `http://127.0.0.1:3001/login` 登录，进入 AI 错题 Agent；上下文同时包含一条作业错题和一条考试错题。
- 教师：通过 `http://127.0.0.1:3000/login` 登录，在 AI 数据整理中对 `Portfolio Demo Course` 执行课程健康度诊断。
- 管理员：通过同一管理端登录，仅访问本地 `/ai/usage` 和统计页面，没有触发生成型接口。
- 教师 ownership canary 使用有效第二名合成教师、用户角色关系和该教师所属课程。主教师的课程选择器、生成上下文、生成结果与历史均不包含 canary。
- 三角色会话按角色分离或经可见退出后重新登录；测试结束时浏览器标签页关闭，内存中的密码引用清除，浏览器运行时重置。

## Real Call Budget

- 真实 Provider attempt：2。
- 真实 Provider success：2。
- 真实 Provider failure：0。
- retry：0；两次日志均为 `attempt=1`。
- 两次成功都满足客户端成功门：HTTP 200、单一 choice、`finish_reason=stop`、非空 content、无 tool calls、合法完整 usage 且 total 等于 prompt 与 completion 之和。只有通过该门的结果才写入历史。
- Provider success 达到 2 后未再触发任何生成请求；管理员统计只读取数据库聚合。

## Student Wrong Agent

- 第一次生成开始前，真实 Provider attempt、历史数和 token 均为 0。
- 第一次真实生成成功，返回非空错题分析；数据库写入一条归属于 `Portfolio Demo Student 01` 的 `wrong-agent` 历史。
- 浏览器检测到第一次 loading 结束后立即再次提交同一操作。第一次成功检测时间为 `2026-08-28T14:53:23.600Z`，第二次点击时间为 `2026-08-28T14:53:26.845Z`，间隔 3.245 秒。
- 第二次请求在 15 秒服务端冷却窗口内被拒绝，页面显示明确冷却提示；Provider attempt 保持 1，历史保持 1，token 未增加。
- Attempt 1 证据保存在仓库外临时目录。Attempt 1 的产品链路成功，但第二次操作约 98 秒后才触发，超过冷却窗口，属于执行时序 BLOCKED；Attempt 2 使用响应完成后即时重复策略通过。

## Teacher Generation

- 教师页面只提供其所属的 `Portfolio Demo Course`，第二名教师的 `DeepSeek Ownership Canary` 不可选。
- 教师真实执行一次课程健康度诊断，页面得到非空分析，数据库写入一条归属于 `Portfolio Demo Teacher 01` 的 `course-health` 历史。
- canary 名称在生成上下文、回复和已写历史中的匹配数均为 0。
- 教师完成后 Provider attempt 从 1 增至 2；无第三次生成。

## Usage

- 数据库共有 2 条成功历史，分别属于 2 名生成用户：学生 1 条、教师 1 条。
- prompt tokens：845；completion tokens：1594；total tokens：2439，且 `845 + 1594 = 2439`。
- 管理员页面显示近 7 天输入 845、输出 1.6k、总计 2.4k、成功生成次数 2 和“未配置估算”。
- 原始 `/ai/usage` 响应与数据库一致：`dbCallCount=2`、`weekCount=2`、相同 token 聚合、`estimatedCost=null`、`costEstimateStatus=UNCONFIGURED`。
- “调用数”在本报告中明确表示成功生成次数，不包含冷却拦截或 Provider 失败。

## Failure Handling

- 在真实调用前使用 loopback fake server 执行浏览器失败 UX：429 显示“AI 服务当前繁忙，请稍后再试”，timeout 显示“AI 服务响应超时，请稍后再试”。
- fake 429 一次、fake timeout 一次；每次仅一次 attempt，无 retry，loading 正常结束，未保存历史、未增加 token。
- 400、401、402、422、429、timeout、503/其他 5xx、响应格式错误、空 content、非 stop finish reason、tool calls 和 usage 不合法等分类由既有确定性测试覆盖；失败路径不保存历史。

## Network

- 真实运行日志中 DeepSeek attempt 2、SUCCESS 2、failure 0，唯一 Provider host 为 `api.deepseek.com`。
- 真实运行日志中 DashScope、Aliyun、Qwen 匹配数为 0；外部 DashScope/Aliyun 请求为 0。
- `reasoning_content` 在数据库历史、运行日志和对客户端响应的检查中匹配数为 0；不返回、不持久化、不记录。
- 实际流程未出现 HTTP 500、CORS 失败或未预期资源 404。
- 浏览器日志没有应用未处理异常；保留的输出仅为 Vue Devtools 提示、Vue Router 动态/重复路由警告以及 ECharts/Browserslist/Node 弃用警告。它们未阻断本轮纵向流程，但应在依赖治理阶段单独处理。

## Regression

- `mvn -pl ape-admin -am clean package`：63 项测试全部通过，Failures 0、Errors 0、Skipped 0；6/6 Reactor 模块均 SUCCESS，BUILD SUCCESS。
- 学生端 `npm run build`：成功，保留既有 Browserslist、包体积与 Node 弃用警告。
- 教师/管理端 `npm run build`：成功，保留既有 Browserslist 与包体积警告。
- 两套前端的 `package.json` 和 `package-lock.json` 无变化；未新增 `.npmrc`。
- 提交前 tracked 业务文件 diff 为 0；本轮仅生成报告和四张脱敏 E2E 截图。

## Screenshots

- `output/playwright/deepseek-v4-flash/01-student-wrong-agent.png`：学生已保存的真实错题分析结果。
- `output/playwright/deepseek-v4-flash/02-student-cooldown.png`：即时二次提交后的明确冷却提示。
- `output/playwright/deepseek-v4-flash/03-teacher-generation.png`：教师课程健康度真实生成结果。
- `output/playwright/deepseek-v4-flash/04-admin-usage.png`：管理员成功次数、token 与未配置费用状态。

## Cleanup

- 已停止本轮后端、两套前端、loopback fake provider 与隔离 MySQL；端口 3000、3001、8080、18081、3307 均释放。
- Redis DB 14 由清理前 2 个本轮键恢复为 0；未清理其他 Redis DB。
- 浏览器标签页关闭，Node 浏览器运行时重置。
- 临时 JWT、登录响应、日志、fake provider 文件和运行目录均已删除。
- 凭据目录及其中两个凭据文件已删除并复核不存在：`CREDENTIAL_FILES_REMOVED`。

## Remaining Risks

- 标准 `npm ci` 的 peer dependency 问题及已知前端依赖漏洞仍未处理；本阶段没有升级依赖。
- 本轮只对两个代表性生成入口执行真实 Provider E2E；其余四个入口只有确定性合同测试证据。
- Vue Router 存在动态路由弃用和重复命名警告，ECharts/Node/Browserslist 存在旧依赖警告。
- 文章、论坛 ownership、公开上传接口等次级安全风险仍按前序报告保留，不能宣称全系统授权已经闭合。
- 最终 README、架构图和正式 Demo 视频尚未完成。
