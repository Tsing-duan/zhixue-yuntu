# 智学云途数据库与运行基线验证报告

最终状态：`RUNTIME_BASELINE_PASS`

本报告只覆盖空库初始化、AI 数据库迁移、Redis、后端真实启动、生产登录/JWT、三角色服务端权限和最小业务数据闭包。未执行前端浏览器 E2E、真实 LLM 调用或部署验证。

## Baseline

- 工作目录：`<repository-root>`
- 分支：`main`
- 起始 HEAD：`a18fbb4b46893804dcc3b4007762d09507d0f45b`
- 起始状态：clean
- remotes：空
- 仓库根目录不存在真实 `.env`；运行配置均通过当前进程的临时环境变量注入。
- 本轮没有继续修改 RBAC、用户出参、前端、npm、DTO 或其他业务代码。

## Runtime Environment

- Java：Temurin 17.0.19
- Maven：3.9.16
- MySQL：Homebrew MySQL 8.4.11
- Redis：实际运行服务 8.8.0
- 后端：Spring Boot 2.4.2，端口 `8080`
- MySQL 在验证前未运行，由本轮启动；验证结束后已停止，隔离数据库保留。
- Redis 在验证前已运行且 `DBSIZE` 为 0；验证结束后只删除本轮创建的 6 个 Demo key，恢复为 `DBSIZE 0`，未停止原有 Redis 服务。

## Fresh Database Bootstrap

- 创建全新隔离空库：`online_learn_portfolio_runtime_20260828_172419`
- 未删除、覆盖或复用既有数据库。
- 依次导入：
  1. `sql/portfolio-demo.sql`
  2. `sql/migrate-ai-data-agent.sql`
- 两次导入退出码均为 0。
- 导入后共有 36 张表。
- 三个合成 Demo 用户、三个角色和三条用户角色关系完整；用户类型 `0/1/2` 各 1 条，`salt` 为空。
- Demo BCrypt 共 3 条、结构均为 60 字符；使用系统 `htpasswd` 独立验证公开 Demo 密码可登录。报告未记录密码或哈希值。
- 最小闭包静态断言：课程、选课、章节、作业、作业提交、考试、试题、考试提交均各至少 1 条；选课、作业提交、考试提交的孤儿引用均为 0。
- 错题 fixture：作业错误答案 1 条、考试错误答案 1 条。

实际命令（省略本机认证细节）：

```sh
brew services start mysql@8.4
mysql -uroot -e 'CREATE DATABASE `online_learn_portfolio_runtime_20260828_172419` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci'
mysql -uroot online_learn_portfolio_runtime_20260828_172419 < sql/portfolio-demo.sql
mysql -uroot online_learn_portfolio_runtime_20260828_172419 < sql/migrate-ai-data-agent.sql
```

## AI Migration

- `ape_ai_history.prompt_tokens`：存在。
- `ape_ai_history.completion_tokens`：存在。
- AI 数据整理菜单记录：1 条。
- AI 历史初始为空。
- 管理员真实调用 `GET /ai/usage`：HTTP 200、业务码 1000，`dbCallCount=0`、`dbTotalTokens=0`。
- 未配置 DashScope Key，未发起真实 LLM 请求；这不是本报告覆盖范围。

已确认一个非阻断的一致性风险：迁移新增的 AI 菜单记录没有对应 `ape_role_menu` 关系，因此 `GET /menu/getMenuByUser` 返回的 34 条管理员菜单中不包含该记录。当前管理端通过静态路由和硬编码侧栏入口访问 `/aiData`，管理员 AI API 也已真实通过。本轮不为使结果变绿而临时修改迁移，后续应统一菜单数据来源。

## Redis & Backend Startup

- 启动前 Redis `PING=PONG`，`DBSIZE=0`。
- 后端使用外部注入的随机 64 位十六进制 JWT secret、隔离数据库连接和临时 Druid 凭据启动；没有在仓库中创建 `.env`，也没有把 secret 写入报告。
- 启动日志确认：active profile `dev`、Tomcat 监听 `8080`、`Started ApeAdminApplication`，启动耗时约 3.0 秒。
- 真实请求期间无 ERROR/Exception 日志行。
- 三角色登录后 Redis 创建 3 个 `user_token:*` key；请求级授权缓存另创建 3 个 Shiro authorization cache key。
- 验证结束后后端已正常停止，`8080` 不再监听；上述 6 个 key 已按确切 key 删除，临时 JWT 文件目录已删除。

启动方式（值已脱敏）：

```sh
JWT_SECRET=<ephemeral-random-secret> \
SPRING_DATASOURCE_DRUID_MASTER_URL=<isolated-local-database-url> \
SPRING_DATASOURCE_DRUID_MASTER_USERNAME=<local-runtime-user> \
SPRING_DATASOURCE_DRUID_MASTER_PASSWORD=<local-runtime-password> \
SPRING_DATASOURCE_DRUID_USERNAME=<ephemeral-druid-user> \
SPRING_DATASOURCE_DRUID_PASSWORD=<ephemeral-druid-password> \
LOGGING_FILE_PATH=<temporary-log-directory> \
java -jar learn_server-master/ape-admin/target/ape-admin.jar
```

## Production Login & JWT

真实调用生产 Controller，而非 mock：

| 角色 | 接口 | HTTP | 业务码 | JWT 结构 |
| --- | --- | ---: | ---: | --- |
| 管理员 | `POST /login` | 200 | 1000 | 3 段 |
| 教师 | `POST /login` | 200 | 1000 | 3 段 |
| 学生 | `POST /login/student` | 200 | 1000 | 3 段 |

JWT 只保存于权限为 600 的临时文件，未打印 token，验证后已删除。

## Three-role Authorization Smoke

所有请求均携带上述真实登录返回的 `x_access_token`；没有伪造身份或直接调用 Controller。

| 调用者 | 目标接口 | 预期 | 实际 |
| --- | --- | --- | --- |
| 匿名 | `POST /role/getRoleList` | 拒绝 | HTTP 401 / 1006 |
| 管理员 | `POST /role/getRoleList` | 允许 | HTTP 200 / 1000 |
| 教师 | `POST /role/getRoleList` | 拒绝 | HTTP 403 / 1003 |
| 学生 | `POST /role/getRoleList` | 拒绝 | HTTP 403 / 1003 |
| 管理员 | `GET /task/getApeTaskByTeacherId` | 拒绝 | HTTP 403 / 1003 |
| 教师 | `GET /task/getApeTaskByTeacherId` | 允许 | HTTP 200 / 1000 |
| 学生 | `GET /task/getApeTaskByTeacherId` | 拒绝 | HTTP 403 / 1003 |
| 管理员 | `POST /student/getApeMyTaskList` | 拒绝 | HTTP 403 / 1003 |
| 教师 | `POST /student/getApeMyTaskList` | 拒绝 | HTTP 403 / 1003 |
| 学生 | `POST /student/getApeMyTaskList` | 允许 | HTTP 200 / 1000 |

结果证明真实 Shiro 请求链上的方法级注解生效，且管理员没有默认继承教师或学生权限。

## Sensitive User Response Smoke

管理员、教师、学生分别真实调用 `GET /user/getUserInfo`：

- 三次均为 HTTP 200 / 业务码 1000。
- `userType` 分别为 0、1、2。
- 对完整 JSON 递归检查，`password` 和 `salt` 字段名出现次数均为 0。

## Minimal Business Closure Smoke

| 链路 | 接口与结果 |
| --- | --- |
| 分类 | `GET /classification/getApeClassificationList`：1 条，HTTP 200 / 1000 |
| 学生选课 | `POST /student/getApeMyTaskList`：1 条，HTTP 200 / 1000 |
| 课程章节 | `GET /chapter/getApeChapterByTaskId`：1 个章节，`homework=1` |
| 学生作业 | `POST /student/getApeHomeworkStudentList`：1 条，答案不一致，形成可识别错题 |
| 学生考试 | `GET /test/getTestListByUser`：1 场考试 |
| 错题闭包 | `GET /ai/wrong-list`：2 条，来源同时包含“作业”和“考试” |
| 教师课程 | `GET /task/getApeTaskByTeacherId`：1 门课程 |
| 教师课程统计 | `GET /ai/course-stats`：报名 1、章节 1、作业 1/提交 1、考试 1 |
| 教师考试统计 | `GET /ai/exam-stats`：提交 1、学生 1 |
| 管理员 AI 用量 | `GET /ai/usage`：调用 0、token 0 |

一次探索性请求使用了错误的 `/homeworkStudent/...` 路由并得到 404；读取真实 Controller 和前端 API 映射后，使用实际路由 `/student/getApeHomeworkStudentList` 重试通过。这是验证脚本路径假设错误，不是应用运行失败。

## Build

实际执行：

```sh
mvn -pl ape-admin -am clean package
```

- 测试：25 个，0 failure，0 error，0 skipped。
- Reactor：6/6 模块 `SUCCESS`。
- 最终结果：`BUILD SUCCESS`。

## Remaining Risks

1. AI 菜单迁移记录未关联角色；当前入口依赖管理端硬编码，数据库菜单模型与 UI 来源不一致。
2. Demo 章节记录仍引用 `/video/...` 和 `/file/...`，而作品库按安全白名单未纳入对应历史媒体；本轮只验证 API 数据闭包，没有验证附件播放/下载。
3. 未验证真实 DashScope/LLM 请求、超时、额度消耗或外部服务错误路径。
4. 未启动两套前端，也未执行浏览器三角色 E2E。
5. 本机 MySQL 允许本地默认 root 访问；公开可复现启动仍应由使用者通过环境变量提供自己的数据库账号，不应依赖这一机器状态。

## Final Lifecycle & Scope Check

- 隔离数据库保留，MySQL 服务恢复为验证前的停止状态。
- Redis 保持原服务运行，数据恢复为验证前 `DBSIZE 0`。
- 后端已停止；端口 `8080` 空闲。
- 临时 JWT、登录响应和运行请求响应均已删除。
- 没有数据库 schema 业务修改，没有前端/npm 修改，没有继续安全加固。
- 本轮仓库变更仅为本报告。

结论：从空 MySQL 数据库导入两个 SQL 文件后，后端可在真实 Redis 和 MySQL 上启动，三角色可通过生产登录接口取得真实 JWT，服务端请求级角色隔离、用户敏感字段治理和最小教学业务闭包均通过。以上证据支持 `RUNTIME_BASELINE_PASS`，但不扩张为前端 E2E、真实 LLM 或完整媒体 Demo 已通过。
