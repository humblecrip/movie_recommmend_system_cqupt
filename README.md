# 基于协同过滤的AI电影推荐系统

## 项目简介

本项目是一个基于 Spring Boot + Vue 2 的电影推荐与管理系统。
后端提供电影信息、用户、收藏、评论、AI 对话式推荐等接口；前端包含
面向用户的前台页面和面向管理员的后台页面。

当前仓库为单体 Maven 后端项目，两个 Vue 前端工程放在
`src/main/resources` 下独立运行。

## 技术栈

- 后端：Spring Boot `2.2.2.RELEASE`、Java `1.8`、Maven 项目
- ORM：MyBatis Plus `2.3`、mybatisplus-spring-boot-starter `1.0.5`
- 数据库驱动：MySQL Connector/J `8.0.18`
- 前台：Vue `2.6.14`、Vue Router `^3.5.2`、Element UI `^2.15.5`
- 管理端：Vue `2.6.14`、Vue Router `^3.1.5`、Element UI `^2.13.0`
- 前端构建：Vue CLI service，前台 `~4.5.0`，管理端 `^4.1.0`
- 样式编译：sass `^1.77.8`

## 当前验证环境

- OS：Windows 10 amd64
- Java：Oracle JDK `1.8.0_271`
- Maven：Apache Maven `3.8.4`
- Maven 路径：`D:\apache-maven-3.8.4`
- Node.js：`v24.13.0`
- npm：`10.8.1`

## 端口与访问路径

- 后端端口：`8080`
- 后端 context-path：`/springbootdo4wek3z`
- 后端接口基址：`http://127.0.0.1:8080/springbootdo4wek3z`
- 前台默认端口：`8082`
- 管理端默认端口：`8081`

前台和管理端的开发服务器都通过 `/springbootdo4wek3z` 代理到后端。

## 数据库配置

当前后端配置位于 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    driverClassName: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3308/movie_recommend_system_cqupt?useSSL=false&useUnicode=true&characterEncoding=utf-8&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true
    username: root
    password: root
```

注意事项：

- MySQL 服务端按 `127.0.0.1:3308` 使用。
- 当前业务库名为 `movie_recommend_system_cqupt`。
- JDBC URL 必须保留 `allowPublicKeyRetrieval=true`。
- 本机 PATH 上的 `mysql` 客户端显示为 MySQL `5.5.36`，这不代表服务端版本。
- 项目运行以 `127.0.0.1:3308` 上的服务端和项目内 MySQL Connector/J `8.0.18` 为准。

## LLM 环境变量

AI 对话式推荐功能通过 `.env` 或系统环境变量读取 LLM 配置。
不要把真实密钥提交到 Git。

必需变量：

- `LLM_API_URL`
- `LLM_API_KEY`
- `LLM_MODEL`
- `LLM_PROVIDER`
- `LLM_ANTHROPIC_VERSION`

建议从 `.env.example` 复制一份本地 `.env`，再填入自己的供应商配置：

```env
LLM_API_URL=https://your-provider.example/v1
LLM_API_KEY=replace-with-your-key
LLM_MODEL=your-model-name
LLM_PROVIDER=openai
LLM_ANTHROPIC_VERSION=2023-06-01
```

说明：

- `LLM_API_URL` 是供应商 base URL，不是完整 chat endpoint。
- `LLM_API_KEY` 只放在本机 `.env` 或系统环境变量中。
- `.env.example` 只用于展示变量格式，不应写入真实密钥。

## 后端启动与构建

本仓库在当前环境下需要显式指定 Maven 本地仓库路径，避免写到不可用的用户目录。

编译后端：

```powershell
D:\apache-maven-3.8.4\bin\mvn.cmd "-Dmaven.repo.local=D:\123123-main\.m2\repository" -DskipTests compile
```

启动后端：

```powershell
D:\apache-maven-3.8.4\bin\mvn.cmd "-Dmaven.repo.local=D:\123123-main\.m2\repository" spring-boot:run
```

后端启动后可访问：

```text
http://127.0.0.1:8080/springbootdo4wek3z
```

根路径没有页面映射时返回 `404` 不一定代表服务未启动，需要以具体接口响应为准。

## 前台启动与构建

前台工程路径：

```text
src/main/resources/front/front
```

安装依赖：

```powershell
cd src/main/resources/front/front
npm install --package-lock=false
```

启动前台：

```powershell
npm run serve
```

构建前台：

```powershell
npm run build
```

当前 `package.json` 中的 `serve` 和 `build` 脚本已内置：

```text
NODE_OPTIONS=--openssl-legacy-provider
```

## 管理端启动与构建

管理端工程路径：

```text
src/main/resources/admin/admin
```

安装依赖：

```powershell
cd src/main/resources/admin/admin
npm install
```

启动管理端：

```powershell
npm run serve
```

构建管理端：

```powershell
npm run build
```

当前 `package.json` 中的 `serve` 和 `build` 脚本已内置：

```text
NODE_OPTIONS=--openssl-legacy-provider
```

## 测试与检查命令

后端编译检查：

```powershell
D:\apache-maven-3.8.4\bin\mvn.cmd "-Dmaven.repo.local=D:\123123-main\.m2\repository" -DskipTests compile
```

后端测试：

```powershell
D:\apache-maven-3.8.4\bin\mvn.cmd "-Dmaven.repo.local=D:\123123-main\.m2\repository" test
```

前台构建检查：

```powershell
cd src/main/resources/front/front
npm run build
```

管理端构建检查：

```powershell
cd src/main/resources/admin/admin
npm run build
```

Git 空白检查：

```powershell
git diff --check
```

## 注意事项

- 不要提交 `.env`、真实 API Key、`node_modules`、`target`、本地 Maven 缓存等本机产物。
- 当前仓库已有本地 Maven 仓库路径约束，后端命令应始终带
  `"-Dmaven.repo.local=D:\123123-main\.m2\repository"`。
- Node.js `v24.13.0` 下运行旧 Vue CLI 工程时，依赖当前脚本内置的
  `NODE_OPTIONS=--openssl-legacy-provider`。
- 数据库版本判断以服务端和 JDBC 驱动为准，不要把 PATH 上旧 `mysql` 客户端版本
  当作服务端版本。
- 如需上传到公开 Git 仓库，先确认 `.env` 未被跟踪，且 README 中没有真实密钥。
