# PDF转Word服务

## 项目简介

这是一个基于Spring Boot的PDF转Word服务，使用Python的pdf2docx库进行转换，提供了简单易用的Web接口。

## 功能特性

- ✅ PDF文件上传和转换为Word格式
- ✅ 支持大文件上传（最大10MB）
- ✅ 实时转换状态反馈
- ✅ 系统状态检查接口
- ✅ 详细的日志记录

## 技术栈

- **后端**：Spring Boot 2.7.15
- **前端**：HTML5 + JavaScript
- **转换引擎**：Python 3.x + pdf2docx
- **构建工具**：Maven

## 快速开始

### 环境要求

- Java 8 或更高版本
- Maven 3.6 或更高版本
- Python 3.6 或更高版本
- pip 包管理工具

### 安装依赖

1. **安装Python依赖**：
   ```bash
   pip install pdf2docx
   ```

2. **构建项目**：
   ```bash
   mvn clean package
   ```

### 运行服务

```bash
mvn spring-boot:run
```

或使用jar包运行：

```bash
java -jar target/file-change-1.0-SNAPSHOT.jar
```

## 访问服务

- **服务主页**：http://localhost:8089
- **API接口**：
  - 健康检查：http://localhost:8089/api/file/check/libreoffice
  - 服务信息：http://localhost:8089/api/file/info
  - PDF转Word：POST http://localhost:8089/api/file/pdf-to-word

## 使用指南

1. 打开服务主页：http://localhost:8089
2. 点击"选择文件"按钮，选择要转换的PDF文件
3. 点击"转换"按钮，等待转换完成
4. 转换完成后，浏览器会自动下载转换后的Word文件

## 配置说明

配置文件位于 `src/main/resources/application.yml`，主要配置项：

```yaml
server:
  port: 8089  # 服务端口

# 文件上传配置
spring:
  servlet:
    multipart:
      max-file-size: 10MB  # 单个文件最大大小
      max-request-size: 10MB  # 整个请求最大大小

logging:
  file:
    name: logs/application.log  # 日志文件路径
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
  level:
    root: info
    com.dream.change: debug  # 项目日志级别
```

## 部署指南

详细的部署指南请查看 [DEPLOYMENT.md](DEPLOYMENT.md) 文件。

## 常见问题

### 1. 转换失败
**问题**：转换过程中出现错误
**解决**：
- 检查PDF文件是否损坏
- 确保Python和pdf2docx已正确安装
- 查看 `logs/application.log` 中的详细错误信息

### 2. 文件大小超过限制
**问题**：上传文件时提示"文件大小超过限制"
**解决**：
- 减小PDF文件大小
- 或修改 `application.yml` 中的文件大小限制

### 3. Python未找到
**问题**：服务启动时提示Python不可用
**解决**：
- 确保Python已正确安装
- 检查环境变量是否配置正确
- 尝试使用 `python3` 命令

## 性能优化

1. **增加JVM内存**：
   ```bash
   java -Xms512m -Xmx1024m -jar target/file-change-1.0-SNAPSHOT.jar
   ```

2. **使用专业的PDF转Word工具**：
   - 考虑使用商业PDF转Word API，如Adobe PDF Services API
   - 或部署更强大的开源工具

3. **增加服务器资源**：
   - 对于大量转换请求，增加CPU和内存资源
   - 考虑使用负载均衡，部署多个服务实例

## 安全建议

1. **限制访问**：
   - 在生产环境中，配置防火墙规则，只允许特定IP访问服务
   - 考虑添加认证机制

2. **文件处理安全**：
   - 对上传的PDF文件进行病毒扫描
   - 限制上传文件的类型和大小

3. **定期更新**：
   - 定期更新Java、Python和依赖库
   - 监控安全漏洞

## 项目结构

```
FileConversion/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/dream/change/
│   │   │       ├── annotation/      # 注解类
│   │   │       ├── aspect/          # 切面类
│   │   │       ├── config/          # 配置类
│   │   │       ├── controller/      # 控制器
│   │   │       ├── service/         # 服务层
│   │   │       └── FileChangeApplication.java  # 应用主类
│   │   ├── resources/
│   │   │   ├── static/             # 静态资源
│   │   │   │   ├── css/             # 样式文件
│   │   │   │   │   └── style.css
│   │   │   │   ├── js/              # JavaScript文件
│   │   │   │   │   └── script.js
│   │   │   │   └── index.html      # 前端页面
│   │   │   └── application.yml     # 配置文件
├── pom.xml                         # Maven配置
└── README.md                       # 项目说明
```

## 贡献

欢迎提交Issue和Pull Request来改进这个项目。

---

**版本**：1.0
**最后更新**：2026-03-26