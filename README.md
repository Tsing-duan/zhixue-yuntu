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

需要 Java 17、Maven、MySQL、Redis 和 Node.js。当前启动脚本按 macOS + Temurin 17 配置，具体版本、目录和操作步骤见[本地运行指南](docs/local-development.md)。

**演示 SQL 会删除并重建同名表，请只使用全新、可丢弃的数据库，不要导入已有业务库。**

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
