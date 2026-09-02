# 智学云途

三角色在线教学平台 + DeepSeek AI 教学辅助 + 工程治理。

> 基于既有 Vue 2 + Spring Boot 在线教学系统接手后续开发与重构，完成 AI 能力迁移、业务与安全治理、可复现运行和招聘作品化交付。本仓库不是“全部从零独立开发”。

## 开源来源与许可

本项目基于 [WuJunJie521/RuoYi](https://github.com/WuJunJie521/RuoYi) 继续开发。上游 RuoYi 代码采用 MIT License，原始版权声明为 `Copyright (c) 2018 RuoYi`；本仓库完整保留该声明和许可文本。

智学云途的教学业务扩展、DeepSeek AI 纵切、安全治理、可复现验证和作品化整理由 Tsing-duan 后续完成，开发过程中使用 Codex 与 CC-DS 辅助。本仓库同样以 [MIT License](LICENSE) 发布；新 Git 历史不表示上游代码由当前维护者从零原创。

## 项目概览

平台面向学生、教师和管理员：学生完成课程、作业、考试与错题复盘；教师维护教学内容并查看课程数据；管理员管理用户、权限与 AI 用量。AI 纵切把真实教学上下文交给 DeepSeek，并对响应、历史、token、额度与冷却进行服务端治理。

## 我负责的部分

### 既有系统基础

- Vue 2 学生端与教师/管理端双前端。
- Spring Boot 多模块后端，以及 MySQL、Redis 基础设施。
- 用户、角色、菜单、课程、章节、作业、考试/题库、文章/论坛、消息与上传等教学系统能力。
- 基础 JWT、Shiro 与 Redis 认证链路。

### 后续主导开发 / 重构

- 将六个生成入口迁移到单一 DeepSeek V4 Flash Provider；补齐成功响应门、错误分类、超时、零重试和安全日志。
- 完成学生错题 Agent、教师课程健康度/AI 数据能力、AI 历史与 token 聚合。
- 增加分角色每日调用/token 额度、输入长度限制与 Provider 前冷却拦截。
- 将 JWT secret 外部化；实施服务端 RBAC、教学资源 ownership/IDOR 防护和正确的 HTTP 401/403 语义。
- 修复学生提交与资料更新的 mass assignment；禁止 `password` / `salt` 出现在用户 API 响应。
- 建立脱敏 Demo fixture、空库初始化、三角色浏览器 E2E 和真实 Provider 纵向验证。
- 修复两套前端标准 `npm ci` 可复现性，移除错误/未使用依赖并将最终 audit 的 critical 数量降为 0。
- 整理招聘作品库、验证报告、架构说明和演示材料。

## 核心能力

### 1. 三角色完整业务闭环

- **学生：**课程 → 作业 → 考试 → 作业/考试双来源错题。
- **教师：**课程 → 章节 → 作业/考试 → 课程与考试统计。
- **管理员：**用户/权限 → 平台运营 → AI 成功次数与 token 用量。

角色边界由服务端执行，前端菜单隐藏不是授权依据；管理员也不会默认继承教师或学生权限。

### 2. AI 教学纵切

```text
错题/课程上下文 → 额度与冷却 → DeepSeek V4 Flash
→ 响应合同校验 → AI History → Token Usage → 前端
```

- 六个生成入口共享一个 Provider client；学生错题 Agent 和教师课程健康度完成了真实 Provider E2E，其余四个入口由确定性请求级合同测试覆盖。
- 真实 E2E：2 次成功、0 次 Provider failure、0 次 retry；两次响应均为完整 `finish_reason=stop`。
- 内部结果类型不映射 `reasoning_content`；现有真实 E2E 的客户端响应、数据库和日志检查均为 0 匹配。
- 学生即时二次提交在 Provider 前被 15 秒冷却拦截，历史和 token 均不增加。
- Provider 失败不会写入“成功生成”历史，也不会增加成功 token 统计。

详见 [AI 纵切图](docs/ai-vertical-slice.md)。

### 3. 服务端安全治理

- JWT、Redis 会话状态和 Shiro filter/realm 构成现有认证链；方法级注解执行服务端角色授权。
- `ResourceAuthorizationService` 沿课程—章节—作业/考试—题目父级关系做 fail-closed ownership 校验。
- 修复教师替换资源 ID 操作他人课程、学生伪造提交身份/分值、资料 mass assignment 等路径。
- `password` 使用只写序列化策略，`salt` 完全禁止客户端序列化/反序列化。
- 未认证、无角色和资源不归属分别通过真实请求链返回明确的 401/403 类结果。

### 4. 可复现工程交付

- 两份 SQL 可从空 MySQL 库建立 36 张表、三角色合成账号和最小教学闭包。
- Redis、后端真实启动、三角色经实际登录 Controller 取得 JWT 与最小 API 闭包均已验证。
- 后端 Maven 测试、6 个 Reactor 模块、两套前端标准 `npm ci` / build 均有可重复结果。
- 三角色浏览器 E2E 覆盖正常流程、401/403、ownership canary、缺失媒体安全空状态和 console/network 检查。

## 技术栈

| 层 | 技术 |
| --- | --- |
| Frontend | Vue 2、Element UI、Axios、ECharts |
| Backend | Spring Boot、MyBatis-Plus、Shiro、JWT、Redis、MySQL |
| AI | DeepSeek V4 Flash、OpenAI-compatible Chat Completions、Hutool HTTP |
| Testing / Verification | Maven、JUnit、MockMvc、Browser E2E、npm audit / dependency triage |

## 可验证证据

| 项目 | 结果 | 证据 |
| --- | --- | --- |
| 后端测试 | 63 / 63 | [DeepSeek E2E 报告](PORTFOLIO_DEEPSEEK_V4_FLASH_E2E_REPORT.md#regression) |
| Maven modules | 6 / 6 | [依赖回归报告](PORTFOLIO_DEPENDENCY_AUDIT_REPORT.md#regression) |
| 学生端 `npm ci` / build | PASS | [依赖可复现性](PORTFOLIO_DEPENDENCY_AUDIT_REPORT.md#reproducibility) |
| 管理端 `npm ci` / build | PASS | [依赖可复现性](PORTFOLIO_DEPENDENCY_AUDIT_REPORT.md#reproducibility) |
| Critical dependencies | 0 | [漏洞裁决](PORTFOLIO_DEPENDENCY_AUDIT_REPORT.md#vulnerability-triage) |
| 空库初始化 | PASS | [运行基线](PORTFOLIO_RUNTIME_BASELINE_REPORT.md#fresh-database-bootstrap) |
| 三角色 JWT / RBAC | PASS | [真实授权 smoke](PORTFOLIO_RUNTIME_BASELINE_REPORT.md#three-role-authorization-smoke) |
| Browser E2E | PASS | [三角色浏览器 E2E](PORTFOLIO_BROWSER_E2E_REPORT.md) |
| DeepSeek real E2E | PASS | [真实调用预算与结果](PORTFOLIO_DEEPSEEK_V4_FLASH_E2E_REPORT.md#real-call-budget) |

DeepSeek 真实纵向结果：success 2、failure 0、retry 0；学生 cooldown 请求在 Provider 前被拦截。以上是隔离环境验收结果，不代表线上生产流量或性能指标。

公开快照发布前对最终 Demo SQL 完成了 36 张表、三角色关系和最小教学数据闭包的静态检查；本轮未再导入 MySQL，真实空库运行证据以上表链接的运行基线报告为准。

## 3 分钟 Demo

| 时间 | 展示内容 |
| --- | --- |
| 00:00–00:25 | 用本页和[主架构图](docs/architecture.md)说明三角色、贡献边界与技术结构。 |
| 00:25–01:10 | 学生端：已选课程 → 作业/考试双来源错题 → AI 错题 Agent。 |
| 01:10–01:45 | 教师端：自己的课程 → 课程健康度 → AI 分析结果。 |
| 01:45–02:10 | 管理端：成功生成次数、prompt/completion/total token 与未配置费用状态。 |
| 02:10–02:40 | 教师 ownership canary 请求被服务端拒绝，目标课程保持不变。 |
| 02:40–03:00 | 展示 63/63 测试、6/6 Maven、双前端 build 和真实 E2E 证据。 |

逐秒操作与异常预案见 [招聘 Demo 脚本](docs/recruiter-demo-script.md)。

## 本地 Demo

已验证环境：Java 17、Maven 3.9、MySQL 8.4、Redis，以及 Node.js 26 / npm 11。仓库尚未锁定 Node 版本，因此其他版本需要重新验证。

1. 启动本地 MySQL 和 Redis。当前后端开发配置使用 `localhost:3306/online_learn`、MySQL `root` 用户和 `127.0.0.1:6379` Redis；先创建名为 `online_learn` 的全新专用 Demo 库。
2. 依次向该库导入两份 SQL。首份脚本包含删表现有结构，禁止用于已有业务库：

   ```sh
   mysql -u root -p -e 'CREATE DATABASE `online_learn` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;'
   mysql -u root -p online_learn < sql/portfolio-demo.sql
   mysql -u root -p online_learn < sql/migrate-ai-data-agent.sql
   ```

3. 由 `.env.example` 生成权限受限的本地 `.env`，设置 `DB_PASSWORD`、不少于 32 字符的 `JWT_SECRET` 和可选 DeepSeek 凭据；`.env` 不得提交。
4. 在两套前端目录分别执行标准 `npm ci`；启动脚本不会自动安装依赖或导入数据库。
5. 执行 `bash scripts/start-demo.sh`；学生端为 `http://localhost:3001/login`，教师/管理端为 `http://localhost:3000/login`。
6. 演示结束执行 `bash scripts/stop-demo.sh`。当前 launcher 假设 macOS Temurin 17 的本机安装布局，其他环境需要单独验证。

无 DeepSeek 凭据时非 AI 功能仍可启动，生成型 AI 会返回明确的“未配置”错误。Demo 使用合成账号，凭据只通过私下方式提供，不写入仓库或演示画面。

## 代码讲解入口

- [面试代码讲解索引](docs/interview-code-map.md)
- [系统架构](docs/architecture.md)
- [AI 纵切](docs/ai-vertical-slice.md)

## Known Limitations

- Vue 2 已 EOL；Vue CLI / webpack 构建链仍有经裁决为 dev/build-only 的 audit high。
- Vue Router 动态路由 API 与重复命名路由仍有 warning。
- Node 版本尚未通过 `.nvmrc`、Volta 或 `engines` 完全锁定。
- 文章、论坛等非招聘 Demo 主链路的 ownership 尚未完成第二轮逐接口治理，不能宣称全站授权已全面闭合。
- 当前 JWT/Redis 滑动会话尚未逐请求严格比较来路 token 与缓存 token；现有证据覆盖身份失效、角色矩阵与 401/403 语义，不代表完整 token 攻击面已闭合。
- 项目包含 RuoYi 的既有基础及后续大量修改；阅读贡献边界时应结合本页“开源来源与许可”和“我负责的部分”，不能将全部代码表述为从零原创。
