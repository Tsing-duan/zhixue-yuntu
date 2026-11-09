# 智学云途

**简体中文** · [English](README.en.md)

一个支持课程学习、作业考试、同学交流和 AI 辅助的在线教学项目，包含学生、教师和管理员三个角色。

## 功能

- **学生端**：发现课程与教师，学习章节，完成作业和考试，整理错题，分享笔记与讨论；教师资料和公告只读。
- **教师端**：管理自己的课程、章节、作业与考试，查看教学数据，使用 AI 整理备课材料。
- **管理端**：管理用户、角色、菜单及平台内容，查看 AI 成功调用和 token 用量。
- **AI 辅助**：学习问答、学习计划、错题分析、课程总结、教师备课、教学数据分析。授权、课程归属、输入限制、额度和冷却均在服务端检查。
- **中英文界面**：默认中文，登录页和导航可切换 English，刷新后保留；原创内容和已保存 AI 回答保持原文。显式演示模式提供标识清楚的双语合成样例。

首页保留真实课程、教师和分享内容；导航适配窄屏与缩放，短页面页脚位于视口底部。关于页提供项目与个人标识、简短介绍和 [GitHub 反馈入口](https://github.com/Tsing-duan/zhixue-yuntu/issues)。

## 项目来源与后续维护

项目基于 [WuJunJie521/RuoYi](https://github.com/WuJunJie521/RuoYi)，原有教学模块由课程小组共同完成。倾断在此基础上持续维护，修复业务与权限问题，并借助 AI 实现教学辅助功能及后续工程改进；不代表全部代码从零独立开发。开发过程中使用了 Codex 与 CC-DS 辅助。

本次更新将两套前端从 Vue 2 / Vue CLI 迁移到 Vue 3 / Vite，加入 Pinia 会话管理、Element Plus、中英文界面，并完善学生只读接口、AI 用量保护与功能提示词。保留原团队贡献和上游许可。

## 技术栈

| 层 | 技术 |
| --- | --- |
| 前端 | Vue 3、Vite、TypeScript（核心层）、Pinia、Vue Router 4、Element Plus、Axios、ECharts、Quill 2 |
| 后端 | Spring Boot、MyBatis-Plus、Shiro、JWT |
| 数据 | MySQL、Redis |
| AI | 官方 DeepSeek，固定模型与 Chat Completions 协议，非流式调用 |
| 验证 | JUnit / MockMvc、Vitest、Playwright、隔离环境权限检查 |

旧业务页面主要保留 Options API / JavaScript，未全量改写为 TypeScript。

## 本地运行

使用 Java 17、Maven、MySQL、Redis、Node 26.12.0 / npm 11.20.0。当前启动脚本按 macOS + Temurin 17 配置，完整步骤见 [本地运行指南](docs/local-development.md)。

**演示 SQL 会删除并重建同名表，只能导入全新、可丢弃的数据库。** 公开仓库不提供可直接登录的密码，也不包含本机数据库、上传文件或 `.env`。

```sh
npm ci --prefix learn_front-master
npm ci --prefix learn_front_mange-master
# 按运行指南完成新建演示库和后端环境配置后：
bash scripts/start-demo.sh
```

默认学生端 `http://localhost:3001/login`，教师/管理端 `http://localhost:3000/login`。两端的 `npm run build` 包含类型检查；部署时需配置 history 路由回退。

## 接入模型

不配置模型也可使用普通教学功能。当前只适配官方 DeepSeek：代码限定 `https://api.deepseek.com`、`deepseek-v4-flash` 和 `reasoning_effort=high`，不是任意 OpenAI 兼容服务的通用适配器。

在后端本地 `.env` 或部署环境设置 `DEEPSEEK_API_KEY`；置空显示未配置提示，`DEEPSEEK_ENABLED=false` 可关闭生成。密钥不得写入前端或提交。参数、数据范围、Redis 额度保护及验证方式见 [模型接入说明](docs/ai-model-setup.md)。本次更新未发起真实供应商调用，不将 mock 通过称为真实模型可用。

## 验证与限制

本轮源码通过两端构建、34 项前端单元测试和 87 项后端测试。本机浏览器检查覆盖 51 个页面入口、24 组双语布局、3 组偏好保留及 16 组真实缩放；范围与未覆盖项见 [验证说明](docs/verification-notes.md)。这些是本地证据，不是全站生产验收。

学生查看教师与公告所需的旧 403 问题已通过必要字段的只读接口修复；管理员写入权限未开放给学生。其他基于角色或资源归属的 403 是预期拒绝，不能笼统视为故障。

项目仍用于学习和演示：部分文章/论坛归属路径、完整 token 攻击面及所有业务弹窗尚未完成全面审查；少量旧后端分组和图表文字仍保留原文。模型回答需要人工判断，不建议未经进一步验证直接用于生产。

## 文档与反馈

- [本地运行](docs/local-development.md) · [模型接入](docs/ai-model-setup.md)
- [国际化与演示数据边界](docs/internationalization.md) · [验证说明](docs/verification-notes.md)
- [系统架构](docs/architecture.md) · [AI 请求流程](docs/ai-vertical-slice.md)
- [GitHub Issues](https://github.com/Tsing-duan/zhixue-yuntu/issues)：请勿公开密码、密钥或真实学生资料。

## 许可证

采用 [MIT License](LICENSE)，保留上游 RuoYi 的版权声明与许可文本。

旧菜单图标字体保留 [Element UI MIT 许可](docs/modernization/element-icons-LICENSE)。
