# 系统架构

这张图只保留招聘 Demo 主链路。它描述当前双前端、Spring Boot 多模块后端、认证/授权、教学业务和 AI Provider 之间的关系；不是全部接口或 36 张表的穷举。

```mermaid
flowchart LR
    subgraph Browser[Browser]
        Student[Student Vue 3 / Vite<br/>:3001]
        Staff[Teacher / Admin Vue 3 / Vite<br/>:3000]
    end

    subgraph Backend[Spring Boot]
        Filter[JWT Filter + Shiro Realm<br/>authentication]
        Controller[REST Controllers]
        Authorization[Method RBAC +<br/>ResourceAuthorizationService]
        Teaching[Teaching Services<br/>course / homework / exam]
        AiPolicy[AI Endpoint Policy<br/>role / ownership / quota / cooldown]
        AiService[AiService<br/>prompt + context assembly]
        Client[DeepSeekClient<br/>request / response contract]
    end

    Student -->|HTTP + JWT| Filter
    Staff -->|HTTP + JWT| Filter
    Filter --> Controller
    Controller --> Authorization
    Authorization --> Teaching
    Authorization --> AiPolicy
    AiPolicy --> AiService
    AiService --> Client
    Client -->|HTTPS Chat Completions| Provider[api.deepseek.com]

    Redis[(Redis<br/>login token + Shiro cache + AI reservations)]
    MySQL[(MySQL<br/>business data + AI history + token usage)]

    Filter <--> Redis
    Teaching <--> MySQL
    AiPolicy -->|atomic quota + cooldown + in-flight lease| Redis
    AiPolicy -->|historical usage floor| MySQL
    AiService -->|context queries| MySQL
    Client -->|validated success only| AiService
    AiService -->|persist history and usage| MySQL
```

## 读图要点

- 学生端和教师/管理端是两套 Vue 3 / Vite 应用，共用一套后端 API。
- 认证与授权分层：filter/realm 建立身份，方法级角色注解限制入口，资源守卫继续校验课程及其子资源的 ownership。
- Redis 承担登录 token、Shiro 缓存与 AI 原子额度/冷却预留；教学数据、AI 成功历史和 token 用量以 MySQL 为事实来源。
- AI 请求先经过角色、资源归属、每日额度和冷却检查，再组装服务端上下文并调用统一 Provider client。
- 只有通过响应合同校验的生成结果才写入成功历史；失败路径不增加成功生成次数或 token。

## 当前边界

- 该授权结构集中覆盖招聘 Demo 与教学主链路，不代表文章、论坛等所有资源接口都已完成 ownership 治理。
- 当前运行基线是本机 MySQL、Redis 与三进程前后端，不代表生产部署或多实例能力。
- AI 预留使用共享 Redis Lua，支持跨实例原子检查；生产仍需配置 Redis 持久化、访问控制和一致时区，多实例部署未在本轮实际验收。
