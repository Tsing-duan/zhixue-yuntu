# 中英文界面与演示内容

**简体中文** · [English](internationalization.en.md) · [项目介绍](../README.md)

学生、教师和管理员登录页/导航提供中文与 English 入口。首次默认中文，`zhixue.locale` 保存本地偏好，`zhixue_locale` cookie 让同主机不同端口在聚焦时同步；不同域名各自保存。Element Plus 的日期、分页、空态等使用对应语言包。页面不会为换语言重新挂载，未保存输入保留。

## 维护方式

两端 `src/i18n/index.ts` 提供 Vue 响应式 `t()`、`$t`、`$locale`。中文源文字作为精确键，英文来自共享 `shared/i18n/en.json`。只在界面文字、固定枚举的显示标签与系统反馈处调用翻译；提交值、菜单路由、权限标识不变。不要对任意用户内容调用 `$t`。

新增翻译编辑 `shared/i18n/translations.txt` 的 `中文|English` 行，运行 `python3 scripts/build-i18n-catalog.py` 生成目录。构建器保留已有键并生成 JSON；若本地有界面清单，还会辅助检查遗漏，结果需审阅。新增页面应明确标注界面文字，避免自动替换业务数据或权限常量。内置表单示例通过 `localizeExamples` 随语言切换，用户首次编辑后即保留其输入。已有 AI 历史不翻译；新回答的语言由服务端提示词指定。

## 演示内容边界

`VITE_DEMO_CONTENT` 默认关闭。仅在两端本地忽略的 `.env.development.local` 设置 `VITE_DEMO_CONTENT=true` 可启用当前演示映射；Vite 配置变更会重新加载。该变量是公开显示配置，不能包含密钥。生产构建不会读取 development 文件；不要在真实业务环境启用演示模式。

`shared/i18n/demo-content.json` 保存经过核实的合成样本 ID、字段原值和两种示例显示值。仅当 ID **及字段原值均相符**时显示翻译；新数据、未知 ID、用户已改写字段保持原文。它不调用翻译模型，也不修改数据库。页面顶部明确显示演示标识。序列化和请求层会恢复未编辑样本原值，兼容旧表单复制记录的行为；明确修改的输入保留。

`scripts/build-demo-translations.py` 只读取当前 `.run/modernization/state.json` 指向的隔离库，强校验数据库名与 13306/16379/18080 端口。它不支持任意业务库；新增合成样例需重新生成和审阅映射。该快照并非面向所有数据库的自动翻译器。不要将真实记录加入演示快照。

## 验证与范围

`npm test --prefix learn_front-master`、`npm test --prefix learn_front_mange-master` 覆盖偏好保存、示例与自定义输入、非演示记录保持原文、序列化和提交值保护。两端 `npm run build --prefix <目录>` 运行类型检查与构建。浏览器验证脚本和证据见 [本轮报告](verification-notes.md)。

真实用户文章、课程资料、上传文件、外部内容和旧 AI 回答不做静默翻译。后端返回的未知业务异常、用户自定义菜单名保留原文；已知系统反馈有英文映射。历史架构/验证报告目前保留中文，项目介绍、模型接入及本文提供双语互链。
