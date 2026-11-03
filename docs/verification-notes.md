# 验证记录与已知问题


仓库保留了后续修改时的验证报告，以下是报告记录的结果，不代表每次提交后都已重新测试，也不代表线上性能或教学效果。

- [后端与 AI 验证](../PORTFOLIO_DEEPSEEK_V4_FLASH_E2E_REPORT.md#regression)：后端测试 63/63。
- [依赖与构建](../PORTFOLIO_DEPENDENCY_AUDIT_REPORT.md#regression)：6 个 Maven 模块通过；[两套前端安装与构建](../PORTFOLIO_DEPENDENCY_AUDIT_REPORT.md#reproducibility)通过；[该次检查未发现 critical 依赖漏洞](../PORTFOLIO_DEPENDENCY_AUDIT_REPORT.md#vulnerability-triage)，仍有下述构建链风险。
- [运行基线](../PORTFOLIO_RUNTIME_BASELINE_REPORT.md#fresh-database-bootstrap)：空库初始化、Redis 和后端启动，以及[三角色登录与权限检查](../PORTFOLIO_RUNTIME_BASELINE_REPORT.md#three-role-authorization-smoke)。
- [浏览器测试](../PORTFOLIO_BROWSER_E2E_REPORT.md)：三角色主要流程、401/403 和资源归属检查。

AI 真实接口测试只覆盖学生错题分析与教师课程健康度，共 2 次成功调用；其余 4 个生成入口使用模拟服务进行请求与响应测试。真实测试记录中没有 Provider 失败或重试，连续提交也被冷却规则拦截。这些结果只说明当次调用和拦截流程正常，不能用于证明所有题目上的回答质量。详情见 [AI 调用记录](../PORTFOLIO_DEEPSEEK_V4_FLASH_E2E_REPORT.md#real-call-budget)和[处理流程](ai-vertical-slice.md)。

公开快照发布前还对 Demo SQL 的 36 张表、三角色关系和最小教学数据做过静态检查；该轮没有重新导入 MySQL，实际空库运行结果以运行基线报告为准。

## 当前限制


- Vue 2 已 EOL；Vue CLI / webpack 构建链仍有经裁决为 dev/build-only 的 audit high。
- Vue Router 动态路由 API 与重复命名路由仍有 warning。
- Node 版本尚未通过 `.nvmrc`、Volta 或 `engines` 完全锁定。
- 文章、论坛等非主要演示流程的 ownership 尚未完成第二轮逐接口治理，尚不能确认全站接口都完成了同样的权限检查。
- 当前 JWT/Redis 滑动会话尚未逐请求严格比较来路 token 与缓存 token；现有证据覆盖身份失效、角色矩阵与 401/403 语义，不代表已经覆盖全部 token 安全风险。


