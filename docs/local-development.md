# 本地运行

以下步骤用于在本机运行演示环境，命令都从仓库根目录执行。不要连接已有业务数据库。

## 准备环境

已有运行记录使用 Java 17、Maven 3.9、MySQL 8.4、Redis、Node.js 26 和 npm 11。Node 版本尚未锁定，换版本后需要自行检查兼容性。

当前启动脚本按 macOS 配置，Java 路径固定为：

```text
/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
```

它还会使用 `bash`、`lsof`、`curl` 等命令。如果你的系统或 Java 安装位置不同，下面的启动命令不能保证直接可用；请先查看 [start-backend.sh](../scripts/start-backend.sh)，本页不提供未经验证的跨平台启动步骤。

启动本地 MySQL 和 Redis。当前开发配置连接：

- MySQL：`localhost:3306`，数据库 `online_learn`，用户 `root`。
- Redis：`127.0.0.1:6379`。

这些地址写在 [application-dev.yml](../learn_server-master/ape-admin/src/main/resources/application-dev.yml) 中。`.env.example` 里注释的 `DB_HOST`、`DB_NAME` 等变量目前不会替换上述固定地址。

## 1. 创建演示数据库

**注意：`portfolio-demo.sql` 会删除并重建同名表。只能导入新建、可丢弃的演示库，不能用于已有业务库。** 如果本机已经有 `online_learn`，请停止，不要继续导入；先准备独立的演示 MySQL 环境。

下面的建库命令会在同名数据库已存在时失败，不要忽略这个错误继续执行。

```sh
mysql -u root -p -e 'CREATE DATABASE `online_learn` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;'
```

确认新库创建成功，再依次导入：

```sh
mysql -u root -p online_learn < sql/portfolio-demo.sql
mysql -u root -p online_learn < sql/migrate-ai-data-agent.sql
```

## 2. 配置本机凭据

首次运行时复制配置模板。如果已经有 `.env`，直接编辑现有文件，不要覆盖：

```sh
cp .env.example .env
chmod 600 .env
```

`chmod 600` 表示只有当前文件所有者可读写。`.env` 保存本机密码和密钥，不要提交、截图或分享。

先生成 JWT 密钥：

```sh
openssl rand -hex 32
```

把生成结果填入 `.env` 的 `JWT_SECRET=` 后面。不要使用示例文字代替密钥，长度至少为 32 字符。

再运行配置检查，按终端提示输入本机 MySQL root 密码和可选的 DeepSeek API Key；输入不会显示在终端中：

```sh
bash scripts/start-backend.sh --prepare-env
```

不使用 AI 时，DeepSeek Key 可以直接回车跳过。其余教学功能仍可运行，AI 生成入口会提示未配置。若启用 Druid 监控，请在 `.env` 中设置自己的 `DRUID_PASSWORD`，不要保留模板占位文字。

脚本会按 Shell 配置读取 `.env`，不要放入来源不明的命令。

## 3. 安装两个前端的依赖

学生端目录是 `learn_front-master`，教师和管理端目录是 `learn_front_mange-master`（目录名中的 `mange` 是仓库现有拼写）。分别执行：

```sh
(cd learn_front-master && npm ci)
(cd learn_front_mange-master && npm ci)
```

启动脚本不会自动安装这些依赖，也不会替你导入数据库。

## 4. 启动与访问

确认 MySQL、Redis 正在运行，且 3000、3001、8080 端口未被其他项目占用后执行：

```sh
bash scripts/start-demo.sh
```

脚本会构建并启动后端，再启动两个前端。后端构建使用 `-DskipTests`，因此启动成功不等于测试已通过。

- 学生端：[localhost:3001/login](http://localhost:3001/login)
- 教师和管理端：[localhost:3000/login](http://localhost:3000/login)
- 后端：`http://127.0.0.1:8080`

演示账号使用合成数据，密码不放在公开仓库；需要登录演示时请向维护者获取。没有账号凭据，仅启动服务还不能完成登录演示。

启动失败时先查看仓库根目录的 `.run/backend.log`、`.run/student.log` 和 `.run/admin.log`。分享日志前请检查是否包含本机路径或敏感信息。

## 5. 停止演示

```sh
bash scripts/stop-demo.sh
```

已有测试范围和遗留问题见[验证说明](verification-notes.md)。
