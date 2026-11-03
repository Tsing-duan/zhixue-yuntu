# 智学云途

一个支持课程、作业、考试和错题整理的在线教学项目，包含学生、教师和管理员三个角色。在原有教学功能上，加入了 AI 错题分析和课程分析。

## 功能

- **学生端**：查看课程、完成作业与考试，整理作业和考试中的错题，并使用 AI 辅助分析。
- **教师端**：管理课程、章节、作业与考试，查看统计数据和 AI 课程分析。
- **管理端**：管理用户与权限，查看 AI 调用记录和 token 用量。

AI 功能接入 DeepSeek，服务端处理调用额度、输入长度和冷却时间。调用失败时不会记录为成功，也不会计入成功调用的 token 统计。

## 项目来源与我的工作

项目基于 [WuJunJie521/RuoYi](https://github.com/WuJunJie521/RuoYi)，原有教学模块由课程小组完成。我在此基础上继续维护，查找并修复了账号、头像、收藏和课程状态等问题，提出并借助 AI 实现了项目中的 AI 功能。

后续还整理了权限检查、依赖和本地演示环境。开发过程中使用了 Codex 与 CC-DS 辅助。

## 技术栈

- 前端：Vue 2、Element UI、Axios、ECharts，学生端与教师/管理端分别运行。
- 后端：Spring Boot、MyBatis-Plus、Shiro、JWT。
- 数据存储：MySQL、Redis。
- AI：DeepSeek V4 Flash，通过兼容 OpenAI Chat Completions 的接口调用。

## 本地运行

已验证环境：Java 17、Maven 3.9、MySQL 8.4、Redis，以及 Node.js 26 / npm 11。仓库尚未锁定 Node 版本，因此其他版本需要重新验证。

1. 启动本地 MySQL 和 Redis。当前后端开发配置使用 `localhost:3306/online_learn`、MySQL `root` 用户和 `127.0.0.1:6379` Redis；先创建名为 `online_learn` 的全新专用 Demo 库。
2. 依次向该库导入两份 SQL。首份脚本会删除并重建已有表，禁止用于已有业务库：

   ```sh
   mysql -u root -p -e 'CREATE DATABASE `online_learn` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;'
   mysql -u root -p online_learn < sql/portfolio-demo.sql
   mysql -u root -p online_learn < sql/migrate-ai-data-agent.sql
   ```

3. 由 `.env.example` 生成权限受限的本地 `.env`，设置 `DB_PASSWORD`、不少于 32 字符的 `JWT_SECRET` 和可选 DeepSeek 凭据；`.env` 不得提交。
4. 在两套前端目录分别执行标准 `npm ci`；启动脚本不会自动安装依赖或导入数据库。
5. 执行 `bash scripts/start-demo.sh`；学生端为 `http://localhost:3001/login`，教师/管理端为 `http://localhost:3000/login`。
6. 演示结束执行 `bash scripts/stop-demo.sh`。当前 launcher 假设 macOS Temurin 17 的本机安装布局，其他环境需要单独验证。

无 DeepSeek 凭据时非 AI 功能仍可启动，生成型 AI 会返回明确的“未配置”错误。Demo 使用合成账号，账号凭据不放在公开仓库。

## 项目状态

目前主要用于学习和演示。AI 功能只用少量计算机类题目做过测试，生成结果需要自行判断。项目尚未完成全站安全审查，不建议直接用于生产环境。

已有构建、浏览器流程和部分真实 AI 接口的验证记录，具体范围与已知问题见[验证说明](docs/verification-notes.md)。

## 文档

- [系统架构](docs/architecture.md)
- [AI 请求流程](docs/ai-vertical-slice.md)
- [代码阅读索引](docs/interview-code-map.md)
- [演示步骤](docs/recruiter-demo-script.md)

## 许可证

采用 [MIT License](LICENSE)，保留上游 RuoYi 的版权声明与许可文本。
