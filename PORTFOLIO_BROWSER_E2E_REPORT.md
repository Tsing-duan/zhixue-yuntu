# 智学云途浏览器 E2E 报告

## Environment

- 验证日期：2026-08-28（Asia/Shanghai）。
- 起始 Git：`main`，HEAD `2e3f7fe015b77e4a17af4bcb0009c1fc99ec0865`，工作区 clean，remote 为空；`PORTFOLIO_RUNTIME_BASELINE_REPORT.md` 已单独提交且状态为 `RUNTIME_BASELINE_PASS`。
- Java 17.0.19，Maven 3.9.16，Node.js 26.7.0，npm 11.19.0，MySQL 8.4.11，Redis 8.8.0。
- 后端：`http://127.0.0.1:8080`；管理/教师端：`http://127.0.0.1:3000`；学生端：`http://127.0.0.1:3001`。
- 管理/教师端由 Vue CLI dev server 在 loopback 启动；学生端使用本轮 `npm run build` 的真实 `dist`，由仅监听 loopback、`Cache-Control: no-store` 的临时 SPA server 提供。
- 使用空的 DashScope API Key、随机临时 JWT secret、运行前为空的 Redis 和临时日志目录。密码和 JWT 仅存在于权限 0600 的临时文件或进程内存，未写入报告或仓库。
- 首轮 RED 使用数据库 `online_learn_portfolio_e2e_20260828_182333`。Demo 媒体修复后按硬约束新建并完整重跑 `online_learn_portfolio_e2e_20260828_184800_green`，依次导入 `sql/portfolio-demo.sql` 和 `sql/migrate-ai-data-agent.sql`。
- GREEN 库断言：36 张表；`prompt_tokens`、`completion_tokens` 两个 migration 列存在；核心课程闭包 orphan 数为 0；章节媒体字段非空数为 0；第二名合成教师、教师角色关系和 ownership canary 关系均有效。
- 内置浏览器没有独立 Context 创建接口，因此采用硬约束允许的 fallback：每个角色使用新 tab，前一角色先可见退出，下一角色先访问受保护路由确认回到登录页。结束时以同源探针分别验证两端；管理端发现两个无 token 的残留 localStorage 键后执行同源清理，最终两端 localStorage、sessionStorage、cookies、Cache Storage 均为 0。

## Admin Flow

1. 从 `http://127.0.0.1:3000/login` 通过真实表单登录管理员，进入首页并显示用户、教师、学生和课程数据。
2. 打开 `/user`，用户列表成功加载并包含第二名合成教师；打开 `/role`，角色管理接口成功返回。
3. 打开 `/aiData`，只出现一个可用的 AI 数据整理入口；近 7 天 token、调用次数和费用均为 0，历史为空。未点击任何生成按钮。
4. 浏览器同源请求 `/user/getUserInfo` 和 `/user/getUserPage` 均为 HTTP 200、业务码 1000；递归检查响应对象，`password`/`salt` 字段计数为 0。
5. 可见退出后访问受保护页面回到登录页。

## Teacher Flow

1. 通过管理端真实登录页面登录教师。
2. `/teacherTask` 只显示教师自己的 `Portfolio Demo Course`，不显示 ownership canary 的外部课程。
3. `/teacherChapter` 显示 `Portfolio Demo Chapter`；`/homework` 显示对应作业；`/teacherTest` 显示 `Portfolio Demo Exam`。
4. `/aiData` 的课程健康度显示 1 名报名学生、1 个章节、1 道作业和 1 次提交；考试质量分析显示 1 名参与者、0/1 及格，并渲染成绩统计图。未点击生成分析。
5. 直接访问 `/user`，管理员接口真实返回 HTTP 403，页面显示明确的 403 无权限页，未泄露管理员数据。
6. 浏览器从教师身份 POST 修改 `portfolio-e2e-foreign-task`，得到 HTTP 403、业务码 1003；随后数据库断言课程名称和第二名教师归属均未变化，因此无需因 canary 成功而重建数据库。

## Student Flow

1. 从 `http://127.0.0.1:3001/login` 通过真实表单登录学生。
2. `/myTask` 只显示已选的 `Portfolio Demo Course`。进入课程详情并选择 `Portfolio Demo Chapter`；无视频时显示安全空状态，仍可进入作业。
3. 作业页显示已提交、总分 1、得分 0 和错误选择；考试页显示已完成的 `Portfolio Demo Exam` 和 0 分记录。
4. `/ai` 的“AI 错题 Agent”只读取错题列表，显示 2 道错题，同时包含“作业”和“考试”来源；未点击生成或发送。
5. 浏览器分别请求教师课程接口和管理员角色分页接口，两者均为 HTTP 403、业务码 1003。
6. 可见退出后访问 `/myTask`，被正确引导到学生登录页。

## Authorization UX

- 401：精确删除教师 Redis `user_token` 键后，通过页面触发受保护请求；真实 HTTP 401 导致前端清理 token、用户、菜单、session 和 Cache Storage，显示“登录已失效”，并回到 `/login`。没有递归调用失效 logout API，也没有未处理 Promise error。
- 403：管理端显示“权限不足”并进入现有 `/403`；学生端保留安全页面并显示明确提示。预期 403 不再弹出原始 JSON，也不产生 Vue error overlay。
- 管理端登录接口现在拒绝学生账号进入管理/教师门户；学生只能使用独立学生入口。对应后端测试先 RED、后 GREEN。
- 所有权限失败均 fail closed；ownership canary、教师到管理员、学生到教师/管理员的真实浏览器请求均被服务端拒绝。

## Console and Network

- GREEN 三角色页面 console error 数均为 0；没有空白页、CORS error、未处理异常或 Vue error overlay。
- 管理端仍有 Vue Router `addRoutes` deprecated、重复命名路由等旧框架 warning；未影响本轮主链路，列入 Remaining Risks。
- 无未预期 HTTP 4xx/5xx。操作日志中唯一 `state=1` 是预期的 ownership canary `/task/editApeTask` 403；未发现 HTTP 500。
- RED 阶段课程详情实际引用 `/video/1701306897160.mp4`，浏览器 `readyState=0` 且本地后端返回 404。修复并重建空库后，浏览器断言视频元素数为 0、损坏图片数为 0、页面显示无视频安全空状态；GREEN 未再出现 `/video/` 或 `/file/` 资源请求。
- 浏览器驱动的数据库请求账本中，AI 只读接口（用量、历史、课程/考试统计、错题列表）共 13 次；生成型端点 `/ai/chat`、`/ai/study-plan`、`/ai/wrong-agent`、`/ai/lesson-prepare`、`/ai/course-summary`、`/ai/data-agent` 请求总数为 0。
- `ape_ai_history` 行数为 0；后端 GREEN 日志对 `dashscope`、`aliyuncs.com`、`qwen` 的外部目标匹配数为 0。结合空 API Key、浏览器未触发生成端点，可确认本轮外部 DashScope/LLM 请求为 0。

## Fixes

- `LoginController`：管理/教师入口拒绝学生账号，并增加针对性后端测试。
- 两套前端 `request.js`：HTTP 401 清理本地会话并返回登录页；HTTP 403 显示明确权限反馈，避免原始 JSON、递归 logout 和未处理 rejection。
- 管理端 AI 数据整理：管理员只加载管理员课程/用量接口，教师只加载教师课程且不调用管理员用量接口。
- 学生 AI 工作台：学生只加载已选课程，教师分支才调用教师课程接口。
- 教师课程新增/编辑隐藏组件：教师页面不再挂载时调用管理员专属教师列表接口。
- 管理端 403 页面：修复重复 Vue key；布局组件改用非保留组件名。
- 教师章节编辑组件：初始化 `task`，消除隐藏组件渲染 error。
- `sql/portfolio-demo.sql`：清空不存在的视频/课件路径；学生课程详情允许选择无视频章节并显示安全空状态，不影响作业入口。
- 未修改数据库 schema、package manifest、lockfile或权限产品矩阵；没有新增依赖或真实 LLM 调用。

## Screenshots

- `output/playwright/browser-e2e/01-admin-home.png`：管理员首页。
- `output/playwright/browser-e2e/02-admin-user-role.png`：管理员角色管理。
- `output/playwright/browser-e2e/03-admin-ai-data.png`：管理员 AI 用量为零和单一入口。
- `output/playwright/browser-e2e/04-teacher-course.png`：教师自有课程。
- `output/playwright/browser-e2e/05-teacher-chapter-homework.png`：教师章节作业。
- `output/playwright/browser-e2e/06-teacher-exam-stats.png`：教师考试统计。
- `output/playwright/browser-e2e/07-teacher-403.png`：教师访问管理员资源的 403。
- `output/playwright/browser-e2e/08-student-course-chapter.png`：学生课程、章节和无视频安全空状态。
- `output/playwright/browser-e2e/09-student-homework-exam.png`：学生已提交作业记录；考试记录另经同一浏览器流程验证。
- `output/playwright/browser-e2e/10-student-wrong-sources.png`：作业、考试双来源错题。
- `output/playwright/browser-e2e/11-auth-logout-login.png`：退出后受保护路由回到登录页。

11 张截图均来自第二个 GREEN 隔离库，不包含密码、JWT、登录响应或 secret。

## Build Regression

- `mvn -pl ape-admin -am clean package`：26 个测试全部通过（0 failure、0 error、0 skipped），6/6 Reactor 模块 `SUCCESS`，最终 `BUILD SUCCESS`。
- `learn_front-master`: `npm run build` 成功，仅有既有 Browserslist、asset size 和 Node deprecation warning。
- `learn_front_mange-master`: `npm run build` 成功，仅有既有 Browserslist 和 asset size warning。
- 两套前端的 `package.json`、`package-lock.json` SHA-256 与本轮起始值完全一致；本轮没有 `.npmrc` 或依赖树修改。
- 本轮两套依赖安装均使用已验证的 `npm ci --legacy-peer-deps` 并成功。标准 `npm ci` 的 peer dependency 可复现性问题未在本阶段修复。
- 最终服务清理：3000、3001、8080、3306 无本轮监听；MySQL 恢复为停止；Redis 保持原服务且 `DBSIZE=0`；浏览器 tab 为 0；临时 JWT、密码、token、日志和会话随后删除。第二隔离数据库保留但不得复用。

## Remaining Risks

- 标准 `npm ci` 仍受旧 peer dependency 树阻塞，且现有依赖漏洞未在本阶段处理。
- 真实 DashScope/LLM 链路尚未验证；本轮明确为 0 次外部调用。
- 文章、论坛等非 Demo ownership、公开 `uploadImg`、旧 Vue Router 重复命名/deprecation warning 等次级安全和前端债务仍未闭合；不能据此宣称全站授权已全面闭合。
- AI 菜单目前由硬编码入口与数据库菜单模型并存；本轮管理员和教师均只看到一个可用入口，未出现真实缺失或重复，因此未修改菜单关系。
- 最终招聘 Demo 视频、README 和架构图尚未完成。

## Final Status

`BROWSER_E2E_PASS`
