<template>
  <el-config-provider :locale="$elementLocale"><div class="app-shell" :class="{ 'is-demo': $isDemo }"><DemoNotice />
    <router-view />
  </div></el-config-provider>
</template>

<style lang="scss">
:root {
  --admin-bg: #f5f7fb;
  --admin-surface: #ffffff;
  --admin-surface-soft: #f8fafc;
  --admin-border: #e6eaf0;
  --admin-border-strong: #d7dee8;
  --admin-text: #243145;
  --admin-muted: #667085;
  --admin-primary: #2563eb;
  --admin-primary-hover: #1d4ed8;
  --admin-success: #16a34a;
  --admin-warning: #d97706;
  --admin-danger: #dc2626;
  --admin-shadow: 0 10px 24px rgba(15, 23, 42, 0.06);
}

* {
  box-sizing: border-box;
}

html,
body,
#app,
.common-layout {
  margin: 0;
  padding: 0;
  height: 100%;
}

body {
  background: var(--admin-bg);
  color: var(--admin-text);
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC",
    "Microsoft YaHei", Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
}

.xiaoshi {
  display: none;
}

body::-webkit-scrollbar {
  width: 0;
  height: 0;
}

// 滚动条
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
  background-color: #eef2f7;
  border-radius: 999px;
}

::-webkit-scrollbar-thumb {
  background-color: #c5ceda;
  border-radius: 999px;
}

.el-pagination {
  padding-bottom: 10px !important;
  padding-top: 10px !important;
  display: flex;
  justify-content: flex-end;
}

/* Global admin theme */
#app .common-layout {
  background: var(--admin-bg);
}

#app .common-layout .search-table {
  padding-left: 0 !important;
}

#app .common-layout .search,
#app .common-layout .table {
  background: var(--admin-surface) !important;
  border: 1px solid var(--admin-border) !important;
  border-radius: 12px !important;
  box-shadow: var(--admin-shadow) !important;
}

#app .common-layout .search {
  margin-bottom: 12px;
}

#app .common-layout .action-toolbar {
  padding: 12px 14px 4px !important;
  margin-left: 0 !important;
}

#app .common-layout .search-title {
  color: var(--admin-muted) !important;
  font-family: inherit !important;
  font-weight: 600;
}

#app .common-layout .el-card {
  color: var(--admin-text);
  border: 1px solid var(--admin-border) !important;
  border-radius: 12px !important;
  box-shadow: var(--admin-shadow) !important;
}

#app .common-layout .el-card__body {
  padding: 18px;
}

#app .common-layout .el-input__inner,
#app .common-layout .el-select .el-input__inner,
#app .common-layout .el-textarea__inner {
  border-color: var(--admin-border-strong) !important;
  border-radius: 9px !important;
  color: var(--admin-text) !important;
  background: #ffffff !important;
  font-family: inherit !important;
}

#app .common-layout .el-input__inner:focus,
#app .common-layout .el-select .el-input.is-focus .el-input__inner,
#app .common-layout .el-textarea__inner:focus {
  border-color: #8bb2f1 !important;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1) !important;
}

#app .common-layout .el-table {
  color: var(--admin-text) !important;
  font-family: inherit !important;
  background: transparent !important;
}

#app .common-layout .el-table th.el-table__cell {
  color: #344054 !important;
  background: #f8fafc !important;
  border-bottom: 1px solid var(--admin-border) !important;
  font-weight: 700;
}

#app .common-layout .el-table td.el-table__cell {
  color: #475467 !important;
  border-bottom: 1px solid #eef2f7 !important;
}

#app .common-layout .el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell {
  background: #fbfdff !important;
}

#app .common-layout .el-table__body tr:hover > td.el-table__cell {
  background: #f7fbff !important;
}

#app .common-layout .el-tag {
  border-radius: 7px;
  border-color: #dbe3ef;
  background: #f8fafc;
  color: #475467;
  font-weight: 600;
}

#app .common-layout .el-tag--success {
  border-color: #bbefd0;
  background: #ecfdf3;
  color: #15803d;
}

#app .common-layout .el-tag--warning {
  border-color: #fed7aa;
  background: #fff7ed;
  color: #b45309;
}

#app .common-layout .el-tag--danger {
  border-color: #fecaca;
  background: #fef2f2;
  color: #b91c1c;
}

#app .common-layout .el-pagination.is-background .el-pager li,
#app .common-layout .el-pagination.is-background .btn-next,
#app .common-layout .el-pagination.is-background .btn-prev {
  color: #596579;
  background: #f8fafc;
  border: 1px solid var(--admin-border);
}

#app .common-layout .el-pagination.is-background .el-pager li.active {
  color: #ffffff;
  background: var(--admin-primary);
  border-color: var(--admin-primary);
}

.el-dialog__wrapper .el-dialog {
  border-radius: 14px;
  border: 1px solid var(--admin-border);
  box-shadow: 0 20px 48px rgba(15, 23, 42, 0.18);
  overflow: hidden;
}

.el-dialog__header {
  border-bottom: 1px solid var(--admin-border);
  background: #f8fafc;
  padding: 14px 18px;
}

.el-dialog__title {
  font-size: 14px !important;
  font-family: inherit;
  color: var(--admin-text);
  font-weight: 700;
}

.el-dialog__headerbtn .el-dialog__close {
  color: #98a2b3;
  transition: all 0.2s ease;
}

.el-dialog__headerbtn .el-dialog__close:hover {
  color: var(--admin-primary);
}

.el-dialog__body {
  background: #ffffff;
  color: var(--admin-text);
}

.el-dialog__footer {
  border-top: 1px solid var(--admin-border);
  background: #ffffff;
  padding-top: 12px;
}

.el-message-box {
  border-radius: 14px;
  border: 1px solid var(--admin-border);
  box-shadow: 0 20px 48px rgba(15, 23, 42, 0.18);
  overflow: hidden;
}

.el-message-box__header {
  border-bottom: 1px solid var(--admin-border);
  background: #f8fafc;
}

.el-message-box__title {
  color: var(--admin-text);
}

.el-message-box__content {
  color: #475467;
}

.el-button {
  border-radius: 9px;
  border-color: var(--admin-border-strong);
  background: #ffffff;
  color: #344054;
  font-weight: 600;
  transition: all 0.2s ease;
}

.el-button:hover,
.el-button:focus {
  border-color: #bfd2f5;
  background: #f8fbff;
  color: var(--admin-primary-hover);
}

.el-button:active {
  transform: translateY(1px);
}

.el-button--primary {
  background: var(--admin-primary);
  border-color: var(--admin-primary);
  color: #fff;
}

.el-button--primary:hover,
.el-button--primary:focus {
  background: var(--admin-primary-hover);
  border-color: var(--admin-primary-hover);
  color: #fff;
}

#app .common-layout .btn-create,
#app .common-layout .search-query-btn,
#app .common-layout .view-all-btn {
  background: var(--admin-primary) !important;
  border-color: var(--admin-primary) !important;
  color: #ffffff !important;
}

#app .common-layout .btn-create:hover,
#app .common-layout .search-query-btn:hover,
#app .common-layout .view-all-btn:hover {
  background: var(--admin-primary-hover) !important;
  border-color: var(--admin-primary-hover) !important;
}

#app .common-layout .btn-edit-active,
#app .common-layout .row-edit-btn {
  background: #ecfdf3 !important;
  border-color: #bbefd0 !important;
  color: #15803d !important;
}

#app .common-layout .btn-delete,
#app .common-layout .row-delete-btn {
  background: #fef2f2 !important;
  border-color: #fecaca !important;
  color: #b91c1c !important;
}

.el-button--danger {
  background: var(--admin-danger);
  border-color: var(--admin-danger);
  color: #fff;
}

.el-button--danger:hover,
.el-button--danger:focus {
  background: #b91c1c;
  border-color: #b91c1c;
}

.el-button--text {
  color: var(--admin-primary);
}

.el-button--text:hover,
.el-button--text:focus {
  color: var(--admin-primary-hover);
}

.el-button--mini,
.el-button--small {
  border-radius: 9px;
}
</style>

<style>.app-shell { min-height: 100%; height: 100%; }</style>

<style>.app-shell { --demo-banner-height: 0px; } .app-shell.is-demo { --demo-banner-height: 30px; } .demo-notice { min-height: 30px; box-sizing: border-box; }</style>
