# 智学云途作品库安全加固报告（第一轮）

## Authorization Model

- 认证继续使用现有 JWT + Shiro 链路：除 `ShiroConfig` 明确列出的匿名路径外，其余请求先经过 `JwtFilter`。
- 方法级授权复用 Shiro `@RequiresAuthentication` / `@RequiresRoles`，没有引入第二套鉴权框架。
- 服务端从已认证的 `ApeUser.userType` 补充规范角色 `admin`、`teacher`、`student`，同时保留既有数据库角色/菜单权限；客户端传入的 `role` 或 `userId` 不作为角色判定依据。
- 角色矩阵不做隐式继承：管理员不会因管理员身份自动拥有教师或学生角色。仅有现有业务证据明确支持的接口使用 `admin OR teacher`。
- 教学资源写操作在角色校验后继续通过 `ResourceAuthorizationService` 校验课程教师归属；当前身份、资源或父资源无法解析时均拒绝。
- Shiro 的未认证与未授权异常分别映射为 HTTP 401 / 403；JWT 认证失败也设置 HTTP 401，不再以 HTTP 200 包装失败。

## Endpoint Coverage

当前后端共有 37 个 Controller、226 个请求映射。本轮针对高风险授权面建立如下边界；这不是对全部业务接口的完整授权闭合声明。

### PUBLIC

- `/login`、`/login/student`
- `/login/register`：仅允许注册教师或学生类型，拒绝创建管理员
- `/classification/getApeClassificationList`
- `/school/getApeSchoolList`
- `/major/getApeMajorList`
- `/common/error/**`
- `/common/uploadImg`（本轮按约束不修改，列入剩余风险）
- `/img/**`、`/video/**`、`/file/**`

### AUTHENTICATED

- 除上述匿名路径外的请求由 JWT 过滤器认证。
- `/user/changePassword`、`/user/getUserInfo`、`/user/setUserInfo`、`/user/setUserAvatar/{id}` 增加明确的认证要求。
- 通用 AI 对话与属于当前用户的 AI 历史仍沿用现有身份校验和查询约束。

### STUDENT_ONLY

- 学生选课、我的课程及报名状态。
- 学生作业查询、提交、修改、删除和错题查询。
- 学生考试列表、答题提交和提交状态。
- AI 学习规划、错题列表、错题 Agent。

### TEACHER_ONLY

- `/task/getApeTaskByTeacherId`，只查询当前教师课程。
- 管理员不会自动继承该角色。

### ADMIN_ONLY

- 用户管理：列表、按类型查询、详情、新增、编辑、删除、重置密码。
- 角色、账户、部门、代码生成、登录日志、操作日志、参数、岗位管理 Controller。
- 管理员 AI 用量接口。

### OWNER_OR_PRIVILEGED

- 课程、章节、作业、考试、考试题目的新增/修改/删除：管理员，或拥有父级课程的教师。
- 选课审核与记录管理、作业提交管理、考试提交/评分/重置：管理员，或拥有对应课程的教师。
- AI 备课、课程总结、数据整理、课程统计、考试统计：管理员或教师；涉及具体课程/考试时继续沿用已有归属校验。
- 学生作业详情/修改/删除：仅当前提交人。
- 个人资料：仅当前认证用户；头像路径参数必须等于当前用户 ID。

## Vulnerabilities Found

- 多个系统管理 Controller 只有 JWT 认证，没有服务端角色授权，学生或教师可直接调用管理员接口。
- 学生可调用教师专属课程接口；教学资源写接口仅依赖前端菜单或请求参数。
- 教师可通过替换课程、章节、作业、考试、题目 ID 操作非本人课程，存在垂直越权和 IDOR。
- 学生可替换作业/考试提交中的 `userId`、题目答案、分值等服务端应掌握的字段；资料更新存在 mass assignment，头像更新存在用户 ID 横向越权。
- 公开注册信任客户端 `userType`，可尝试创建管理员类型账户。
- Shiro/JWT 拒绝路径曾返回 HTTP 200，调用方难以区分认证失败与正常响应。
- `ApeUser` 直接进入 `Result` / `Page` 序列化时可输出密码哈希和 salt。

## Fixes

- 对管理员 Controller、角色专属 AI/课程接口和关键教学写接口增加集中、可审计的 Shiro 方法注解。
- 新增 `ResourceAuthorizationService`，沿课程—章节—作业/考试—题目的父级关系做 fail-closed 教师归属校验。
- 学生提交时由认证身份确定 `userId`，由数据库中的作业/考试题确定题目、答案、分值和所属资源；客户端只保留确需提交的作答内容。
- 审核/评分更新先读取已存记录并校验归属，只更新允许变更的状态或分值。
- 个人资料更新改为显式字段白名单；头像修改必须命中当前用户。
- 公开注册只接受教师/学生类型，拒绝管理员及未知类型。
- 为认证/授权异常建立 HTTP 401/403 映射，并让 JWT 失败返回 HTTP 401。

## Sensitive Response

修复前，下列代表性接口返回 `ApeUser` 或其分页/列表包装时可能序列化 `password` / `salt`：

- `/user/getUserPage`
- `/user/getUserListByType`
- `/user/getUserById`
- `/user/getUserInfo`
- 其他任何直接或嵌套序列化 `ApeUser` 的响应

采用项目现有 Jackson 序列化机制做最小全局治理：

- `password` 使用 `JsonProperty.Access.WRITE_ONLY`：注册、登录相关内部输入兼容，但任何 JSON 出参不包含该字段。
- `salt` 使用 `JsonIgnore`：禁止客户端序列化和反序列化；数据库内部认证迁移代码仍可通过 Java 属性访问。
- 未进行全项目 DTO/VO 重构。

## Tests

### RED evidence

- 初始请求/序列化安全测试共 9 项，其中 6 项失败：匿名管理员接口得到 HTTP 200、学生/教师可进管理员接口、学生可进教师接口、`password`/`salt` 被序列化、`salt` 可反序列化。
- 资源归属守卫在新增测试时不存在，编译阶段 RED。
- JWT 四类认证异常在修复前均得到 HTTP 200，而非 401。
- 管理员接口元数据、AI 角色元数据、资料 mass assignment、报名/提交边界测试在修复前失败。
- 公开管理员注册测试在修复前进入持久化调用，RED。
- AI 数据整理、课程统计、考试统计的方法级授权元数据测试在补充审计时 RED。

### GREEN evidence

- `mvn -pl ape-admin -am test -Dsurefire.failIfNoSpecifiedTests=false`
  - 25 tests；0 failures；0 errors；0 skipped。
- 其中 9 个 `MockMvc` 请求级测试通过真实 HTTP 请求分发、生产 Controller、Shiro `AuthorizationAttributeSourceAdvisor` AOP 代理和生产异常处理器，验证：
  - anonymous → admin：HTTP 401
  - student → admin：HTTP 403
  - teacher → admin：HTTP 403
  - student → teacher：HTTP 403
  - admin → teacher/student：HTTP 403（无隐式继承）
  - admin → admin、teacher → teacher、student → student：HTTP 200
- 请求级测试使用隔离的测试 Realm 和 mock Service，不代表数据库、Redis 或完整运行时 JWT E2E。
- `mvn -pl ape-admin -am clean package`
  - 25 tests 全通过。
  - 6/6 Reactor 模块 `SUCCESS`，最终 `BUILD SUCCESS`。
- `git diff --check` 通过；变更集中在后端授权、用户序列化、测试依赖、安全测试和本报告。
- 变更文件定向密钥扫描未发现私钥、云访问密钥或固定 API/JWT secret；测试中仅有明确的合成身份和测试密码。

## Remaining Risks

- 文章、论坛、评论、提问、收藏、消息等模块的 ownership / IDOR 尚未逐项闭合，本轮按范围约束未修改。
- `/common/uploadImg` 仍是匿名上传入口；文件类型、大小、存储隔离与滥用治理留待后续安全阶段。
- 章节视频及其他未纳入本轮高风险清单的写接口，仍需后续逐接口确认角色与资源归属。
- 学生题目/作业读取链路仍可能把标准答案下发到客户端，存在提前查看答案风险；需要后续专门设计答案发布策略或响应 DTO。
- 本轮没有启动 MySQL、Redis，没有执行真实 JWT 登录链路、浏览器 E2E 或真实 LLM 调用；构建与 MockMvc 通过不等同于完整运行时验收。
- 数据库角色/菜单配置仍需在下一阶段真实初始化与三角色 E2E 中核对；当前方法级授权不宣称整个系统授权已全面闭合。

## Final Status

SECURITY_HARDENING_PASS
