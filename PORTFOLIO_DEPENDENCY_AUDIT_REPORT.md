# 智学云途前端依赖可复现性与漏洞裁决

最终状态：`DEPENDENCY_BASELINE_PASS`

审计日期：2026-08-28  
起始提交：`b872aa1d4b5ea349b73c51637e1360624271bb6a`  
依赖基线提交：`7eb6e117a8da6e2b85171b247e55431c02e3f7a8`  
实测环境：Node.js `v26.7.0`、npm `11.19.0`

## Initial State

两套前端在修改前均只能通过 `npm ci --legacy-peer-deps` 安装，随后 `npm run build` 均成功；标准 `npm ci` 均以 `ERESOLVE` 失败。

- 学生端 `learn_front-master`：根依赖 `vue-video-player@6.0.0` 要求 `video.js@7.x`，而根项目声明 `video.js@8.6.1`；其传递依赖 `@videojs-player/vue@1.0.0` 又要求 Vue 3.x，而项目实际为 Vue 2.7.x。
- 管理端 `learn_front_mange-master`：根依赖 `element-plus` 解析为 `2.7.8`，与 `@element-plus/icons-vue@2.3.1` 一起要求 Vue `^3.2.0`，而项目实际为 Vue 2.7.x。
- `--legacy-peer-deps` 能通过，是因为 npm 跳过了这些 peer contract 的严格求解；它掩盖冲突，但没有证明组合兼容。

初始 audit 计数：

| 前端 | 范围 | Critical | High | Moderate | Low | Total |
| --- | --- | ---: | ---: | ---: | ---: | ---: |
| 学生端 | 全部依赖 | 3 | 23 | 25 | 10 | 61 |
| 学生端 | `--omit=dev` | 1 | 4 | 5 | 4 | 14 |
| 管理端 | 全部依赖 | 3 | 21 | 26 | 9 | 59 |
| 管理端 | `--omit=dev` | 1 | 5 | 5 | 3 | 14 |

## Dependency Conflicts

源码检索而不是 manifest 推测给出的结论如下：

- 学生端没有 import、require、插件注册或组件使用 `video.js` / `vue-video-player`；课程详情实际使用原生 `<video>`。
- 管理端只注册并使用 `element-ui`，没有任何 `element-plus` import；名为 `install` 的包也没有源码引用。
- 因此上述冲突依赖不是当前运行链路的一部分，删除它们比引入 `.npmrc` 或强行接受错误 peer tree 更窄、更可验证。
- 未增加 `.npmrc`，最终安装入口仍是标准 `npm ci`。

## Changes

- 学生端删除未使用的 `video.js`、`vue-video-player` 及其传递闭包。
- 管理端删除误装且未使用的 `element-plus`、未使用的 `install` 包及其传递闭包；保留真实使用的 `element-ui`。
- 两端 Axios 分别由 `^1.6.2` / `^1.3.4` 收敛为 `^1.20.0`，实际解析为 `axios@1.20.0`、`form-data@4.0.6`。
- 对 lockfile 做定向传递依赖更新，实际解析为 `shell-quote@1.10.0`、`websocket-driver@0.7.5`、`ws@7.5.13` / `8.21.3`。
- 未改 Vue、Vue Router、Element UI、Vue CLI 的 major；未使用 `npm audit fix --force`；未修改业务源码、API Base URL、AI 配置、数据库或后端代码。

## Reproducibility

两个前端均从空 `node_modules` 执行以下命令并成功：

```text
npm ci
npm run build
```

- 学生端：标准 `npm ci` 成功，production build 成功。
- 管理端：标准 `npm ci` 成功，production build 成功。
- lockfileVersion 保持原状：学生端 `2`，管理端 `3`。
- 无需 `--legacy-peer-deps`，也没有项目级兼容旁路。
- lockfile 中仍有历史 `registry.npmmirror.com` resolution；本机实测可复现，但镜像可用性仍是供应链可用性风险。
- 当前 Node 26 会对 `@achrinza/node-ipc` 打印 engine warning；仓库没有 Node 版本锁定文件，因此“跨 Node 版本均可复现”尚未被证明。

最终 audit 计数：

| 前端 | 范围 | Critical | High | Moderate | Low | Total |
| --- | --- | ---: | ---: | ---: | ---: | ---: |
| 学生端 | 全部依赖 | 0 | 20 | 24 | 9 | 53 |
| 学生端 | `--omit=dev` | 0 | 2 | 3 | 3 | 8 |
| 管理端 | 全部依赖 | 0 | 18 | 24 | 9 | 51 |
| 管理端 | `--omit=dev` | 0 | 2 | 3 | 3 | 8 |

## Vulnerability Triage

`npm audit --omit=dev` 只按安装树分类，不能证明某个包会进入浏览器 bundle。本裁决同时核对了依赖来源、webpack report 和实际 production JavaScript。

### FIXED

| 来源链 | 初始风险 | 处理与结果 |
| --- | --- | --- |
| root Axios → `form-data` | 两端 direct Axios high、`form-data` critical | Axios 更新到 1.20.0，`form-data` 为 4.0.6；两项不再出现在 audit 高危项中。 |
| Vue CLI shared utils → `launch-editor` → `shell-quote` | 两端 `shell-quote` critical | 定向 lock 更新到 1.10.0；critical 消失。 |
| Vue CLI dev server → SockJS/Faye → `websocket-driver` / `ws` | 两端 critical/high | 定向更新到 `websocket-driver@0.7.5`、`ws@7.5.13` / `8.21.3`；相应 critical/high 消失。 |
| 学生端未使用的 video player closure → `@xmldom/xmldom` | high | 删除未使用的 video 依赖闭包；该路径不再存在。 |
| 管理端未使用的 Element Plus closure → `lodash-es` | high | 删除误装的 Vue 3 组件库；该路径不再存在。 |

两端最终 critical 均为 0。

### DEV_ONLY_ACCEPTED

最终全量 audit 的 remaining high 来源逐项归并如下；它们均位于编译、预处理、dev server 或 CLI 路径：

| Direct root / 路径 | 学生端 remaining high | 管理端 remaining high | 生产 bundle 结论 |
| --- | --- | --- | --- |
| `@vue/cli-plugin-babel` | `@babel/plugin-transform-modules-systemjs` | 同左 | 编译期 Babel 插件。 |
| `@vue/cli-service` → webpack/dev-server/proxy/优化工具 | `body-parser`, `brace-expansion`, `braces`, `cross-spawn`, `express`, `http-proxy-middleware`, `launch-editor`, `lodash`, `minimatch`, `node-forge`, `path-to-regexp`, `picomatch`, `serialize-javascript`, `svgo`, `webpack-dev-middleware` | 除 `braces`、`webpack-dev-middleware` 外同类路径，并包含 `fast-uri` | 只在 CLI、构建或本地开发服务器执行。 |
| 学生端 direct dev `less` → `image-size` | `less`, `image-size` | 不适用 | 样式预处理期。 |
| 管理端 direct dev `sass` / CLI schema path | 不适用 | `immutable`, `fast-uri` | Sass/构建配置处理期。 |
| `vue` → `@vue/compiler-sfc` / Vue CLI CSS pipeline → `postcss` → `nanoid` | `postcss`, `nanoid` | `postcss`, `nanoid` | npm 将它们保留在 `--omit=dev` 结果中，但 webpack module report 和最终 production JS 均未包含对应运行时模块；属于构建期工具，不是已证明的浏览器运行时利用面。 |

### RUNTIME_ACCEPTED_WITH_RATIONALE

High / critical：`NONE`。本轮没有把已证明会进入 production bundle 的 high 或 critical 风险作为“可接受运行时风险”放行。

`--omit=dev` 仍报告的 moderate/low 包括学生端 DOMPurify/Quill/Vue，以及管理端 ECharts/Quill/Vue；它们不属于本轮 high/critical 目标，保留为后续定向升级和输入面验证技术债，不能据此声称 audit 为零。

### BLOCKED_REQUIRES_MAJOR_MIGRATION

- 若目标是把“全部依赖” audit 数字降为 0，remaining Vue CLI 5 / webpack dev-server / Vue 2 工具链需要整体迁移或替换；这超出本阶段允许的 patch/minor 修复边界。
- Vue 2 已 EOL；彻底移除其生态和构建链风险需要框架迁移。本轮明确不执行 Vue 3 迁移，也不使用 force resolution 伪造零风险结果。

## Regression

- 后端：`mvn -pl ape-admin -am clean package` 成功；63 tests，0 failures，0 errors，0 skipped；6/6 Reactor modules SUCCESS。
- 学生端：空 `node_modules` 后标准 `npm ci` 与 `npm run build` 成功。
- 管理端：空 `node_modules` 后标准 `npm ci` 与 `npm run build` 成功。
- 浏览器 smoke 使用全新隔离 MySQL 数据库、独立 Redis DB、临时 JWT 且 `DEEPSEEK_API_KEY` 为空：
  - 管理端登录页、管理员首页、教师课程页可访问；
  - 学生端登录页、学生课程页、AI 错题工作台可访问；
  - 未点击任何生成操作，外部 DeepSeek / DashScope / Aliyun 请求为 0；
  - 浏览器 console 无新增 error，可见图片无加载失败。
- 观察到的 warning 为既有 Vue Router `addRoutes` 弃用/重复命名路由、ECharts 旧式配置以及 build 的 Browserslist/体积/v-for key warning；本轮没有为消除 warning 扩大修改范围。
- 本轮没有修改 package 之外的前端配置，因此 API Base URL 与 DeepSeek 配置保持不变；复扫未发现新增 secret、真实个人数据或媒体文件。

## Remaining Debt

1. Vue 2 EOL 与 Vue CLI 5 / webpack 老旧工具链。
2. `npm audit --omit=dev` 的构建期 `postcss` / `nanoid` high 仍会影响扫描数字，尽管未进入 production JS。
3. DOMPurify、Quill、ECharts、Vue 的 moderate/low 风险需要结合实际不可信输入面做单独升级与回归。
4. 缺少 `.nvmrc` / Volta / `engines` 等 Node 版本约束；Node 26 当前安装成功但有旧依赖 engine warning。
5. lockfile 仍混有历史 npm 镜像 resolution。
6. Vue Router 动态路由 API、重复命名路由、ECharts 配置和 Browserslist 数据存在既有 warning。
7. production bundle 体积 warning 尚未处理；本轮不为优化 warning 重构页面或拆包。
