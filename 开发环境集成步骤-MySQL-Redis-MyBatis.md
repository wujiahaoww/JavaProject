# 开发环境集成步骤：Maven + MySQL + Redis + MyBatis

本文档按**推荐顺序**说明如何把当前多模块工程接上 **MySQL（持久化）**、**Redis（缓存）**、**MyBatis（SQL 映射）**。每一步完成后可做一次编译或启动验证。

**Maven 仓库**：项目根目录已提供 `.mvn/settings.xml`（将 `central` 镜像到**阿里云** `maven.aliyun.com`）与 `.mvn/maven.config`（让 **`mvnw` / `mvnw.cmd` 自动使用该 settings**）。在 **IDEA** 中若不用 Wrapper 命令行，请在 **Settings → Build, Execution, Deployment → Maven → User settings file** 中指定 `d:\JavaProject\.mvn\settings.xml`，或勾选使用项目自带的 Maven 配置（视 IDEA 版本而定）。

---

## 步骤 0：本机准备（先于改代码）

1. **安装并启动 MySQL 8.x**  
   - 创建一个数据库，例如：`travel_assistant`，字符集建议 `utf8mb4`，排序规则 `utf8mb4_unicode_ci`（或 `utf8mb4_0900_ai_ci`）。  
   - 记下：**主机**、**端口**（默认 `3306`）、**库名**、**用户名**、**密码**。

2. **安装并启动 Redis 7.x**  
   - 默认无密码时记下 `host`（如 `127.0.0.1`）、`port`（`6379`）。  
   - 若设置了密码，记下 `password`。

3. **确认 JDK**  
   - 工程使用 **Java 17**（与父 `pom.xml` 中 `java.version` 一致）；IDEA 的 Project SDK 也选 17。

---

## 步骤 1：修改 Maven — 父工程 `pom.xml`（建议）

**目的**：集中管理第三方版本，子模块不写版本号，避免漂移。

**操作**：

1. 打开仓库根目录 `pom.xml`。  
2. 在 `<properties>` 中增加（版本可按需要微调，需与 Spring Boot 3.2.x 兼容）：

```xml
<mybatis-spring-boot.version>3.0.3</mybatis-spring-boot.version>
```

3. 在 `<dependencyManagement><dependencies>` 里增加（**不要**去掉已有的 `spring-boot-dependencies`）：

```xml
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>${mybatis-spring-boot.version}</version>
</dependency>
```

**说明**：MySQL 驱动版本由 Spring Boot BOM 管理，一般**不必**在父 POM 再写 `mysql-connector-j` 版本，子模块引入坐标即可。

**验证**：保存后可在 IDEA 右侧 Maven 面板点 **Reload**。

**注意**：若在 `dependencyManagement` 里写了 `${mybatis-spring-boot.version}`，**必须**同时在同一父 POM 的 `<properties>` 中定义该属性；否则会出现  
`Could not find artifact ... mybatis-spring-boot-starter:pom:${mybatis-spring-boot.version}`（版本未被解析）。

---

## 步骤 2：修改 Maven — `travel-bootstrap/pom.xml`

**目的**：启动模块 classpath 上具备 **数据源 + MyBatis + MySQL 驱动 + Redis**（Redis 依赖你已存在，只需后续打开自动配置）。

**操作**：在 `<dependencies>` 中**追加**（若已存在同类依赖则合并，避免重复）：

```xml
<!-- JDBC 数据源（与 MyBatis 配合） -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>

<!-- MyBatis 与 Spring Boot 集成 -->
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
</dependency>

<!-- MySQL 8 驱动（Boot 3 使用 mysql-connector-j） -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

**说明**：

- `spring-boot-starter-data-redis` **已在** `travel-bootstrap` 中，本步骤无需重复添加。  
- Mapper 接口写在 `travel-user` 等模块时，只要该模块被 `travel-bootstrap` 依赖，运行期会在同一 classpath，MyBatis 能扫描到。

**验证**：在项目根目录执行：

```text
mvnw.cmd -pl travel-bootstrap -am clean compile
```

（Linux/mac 使用 `./mvnw`。）应 **BUILD SUCCESS**。

---

## 步骤 3：修改 Maven — `travel-user/pom.xml`（按需）

**目的**：若希望在 **仅编译 user 模块** 时就能解析 MyBatis 注解类型，可为 `travel-user` 增加 **可选** 依赖。

**操作**（二选一即可）：

- **方案 A（推荐，最简单）**：**不修改** `travel-user/pom.xml`，MyBatis 相关依赖只放在 `travel-bootstrap`，Mapper 接口仍在 `travel-user` 中编写 — 这是多模块常见做法。  
- **方案 B**：在 `travel-user` 增加（`provided` 或默认 `compile` 均可，团队统一即可）：

```xml
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
</dependency>
```

**验证**：任选方案后再次 `mvnw.cmd clean compile`。

---

## 步骤 4：配置数据源与 Redis — `travel-bootstrap/.../application.yml`

**目的**：连接 MySQL；启用 Redis 自动配置（去掉骨架阶段的排除项）。

**操作**：

1. 打开 `travel-bootstrap/src/main/resources/application.yml`。  
2. **删除** 整个 `spring.autoconfigure.exclude` 段（或至少删除其中对 `RedisAutoConfiguration` 的排除），否则 Redis 不会自动装配。  
3. **增加** 数据源与 MyBatis、Redis 配置，示例（请把账号密码换成你的真实值；**不要**把生产密码提交到公开仓库，可用环境变量覆盖）：

```yaml
spring:
  application:
    name: travel-assistant
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/travel_assistant?useSSL=false&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  data:
    redis:
      host: 127.0.0.1
      port: 6379
      # password: your_redis_password   # 有密码时取消注释

mybatis:
  mapper-locations: classpath*:mapper/**/*.xml
  configuration:
    map-underscore-to-camel-case: true
    default-fetch-size: 100
    default-statement-timeout: 30

server:
  port: 8080
```

**说明**：

- `mapper-locations` 与你在各模块 `src/main/resources/mapper/` 下存放 XML 的路径一致即可；若暂时没有 XML，可先保留，后续添加文件。  
- 更安全的写法是使用 `spring.datasource.url` 等从环境变量读取（IDEA Run Configuration 或 Docker 注入），此处不展开。

**验证**：保存后无 YAML 语法错误即可。

---

## 步骤 5：注册 MyBatis Mapper 扫描

**目的**：让 Spring 识别 `travel-user`（及其他模块）中的 `@Mapper` 接口。

**操作**（任选一种）：

1. 在 **`TravelAssistantApplication`** 上增加：  
   `@MapperScan("com.travel.user.infrastructure.mapper")`  
   若后续酒店等模块也有 Mapper，可写成多个包：`@MapperScan({"com.travel.user.infrastructure.mapper", "com.travel.hotel.infrastructure.mapper"})`。  

2. 或新建配置类 `@Configuration`，专门写 `@MapperScan(...)`，保持启动类整洁。

**验证**：编写一个最小 `@Mapper` 接口与对应 XML 后启动应用，不应出现 “not found mapper” 类错误（具体以你实现为准）。

---

## 步骤 6：建表（MySQL）

**目的**：持久化用户等数据。

**操作**：

1. 用 MySQL 客户端连接 `travel_assistant` 库。  
2. 执行建表 SQL（用户表示例，可按《项目开发方案》调整字段）：

```sql
CREATE TABLE IF NOT EXISTS user_account (
    id            BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    open_id       VARCHAR(64)  NOT NULL,
    union_id      VARCHAR(64)           DEFAULT NULL,
    nickname      VARCHAR(128)          DEFAULT NULL,
    avatar_url    VARCHAR(512)          DEFAULT NULL,
    phone         VARCHAR(32)           DEFAULT NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login_at DATETIME              DEFAULT NULL,
    UNIQUE KEY uk_open_id (open_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**可选**：引入 **Flyway** 后把上述 SQL 放到 `db/migration/V1__init_user.sql`，实现版本化迁移（父文档中已提及，此处为可选增强）。

---

## 步骤 7：在工程内放置 MyBatis 文件（与步骤 4 路径一致）

**目的**：完成「接口 + XML」或「注解 SQL」的最小闭环。

**操作**：

1. 将 **Mapper 接口** 放在例如：`travel-user/src/main/java/com/travel/user/infrastructure/mapper/UserAccountMapper.java`，并添加 `@Mapper`。  
2. 将 **XML** 放在例如：`travel-user/src/main/resources/mapper/UserAccountMapper.xml`，且 `namespace` 与接口全限定名一致。  
3. 在 `UserAccountMapper.xml` 中编写 `resultMap`、`select`、`insert` 等语句与表 `user_account` 对应。

**验证**：编写 `selectByOpenId` 后，可写单测或临时 Controller 调用，确认能查库。

---

## 步骤 8：使用 Redis 做缓存（实现层）

**目的**：把 Redis 当缓存而不是仅依赖自动配置存在。

**操作**（按需逐步实现）：

1. 确认步骤 4 已配置 `spring.data.redis.*` 且已移除 `RedisAutoConfiguration` 排除。  
2. 在配置类中注入 `RedisTemplate<String, Object>` 或 `StringRedisTemplate`（如需统一序列化，可自定义 `RedisTemplate` Bean）。  
3. 在 **登录后写用户**、**读用户信息** 等路径上：先读 Redis，未命中再读 MySQL，并回写 TTL。  
4. Key 命名建议带业务前缀，例如：`user:profile:{userId}`，并设置合理过期时间。

**验证**：登录一次后，用 Redis CLI `KEYS user:*` 或 `GET` 观察是否有数据（生产环境慎用 `KEYS`，可用 SCAN）。

---

## 步骤 9：整体验证

| 动作 | 命令或方式 |
|------|------------|
| 编译全工程 | 在项目根目录：`mvnw.cmd clean verify` |
| 启动应用 | 运行 `TravelAssistantApplication`，确认控制台无数据源/Redis 连接失败 |
| 健康检查 | 浏览器或 curl 访问 `http://localhost:8080/actuator/health`（若需展示 db/redis 细节，再在配置中开放 `management.endpoint.health.show-details`） |

---

## 步骤顺序小结

| 顺序 | 内容 |
|------|------|
| 1 | 本机安装并启动 MySQL、Redis |
| 2 | 父 `pom.xml`：可选锁定 `mybatis-spring-boot-starter` 版本 |
| 3 | `travel-bootstrap/pom.xml`：`jdbc` + `mybatis-spring-boot-starter` + `mysql-connector-j` |
| 4 | `travel-user/pom.xml`：可选重复引入 MyBatis（见步骤 3） |
| 5 | `application.yml`：数据源、Redis、MyBatis；**去掉** Redis 自动配置排除 |
| 6 | `@MapperScan` 指向 Mapper 接口包 |
| 7 | MySQL 中建表 |
| 8 | 编写 Mapper 接口与 XML（或注解） |
| 9 | 业务代码中接入 Redis 缓存策略 |
| 10 | `mvnw verify` + 启动联调 |

---

## 常见问题

- **启动报 Redis 连接失败**：检查 Redis 是否启动、`host/port/password` 是否正确；是否已删除 `RedisAutoConfiguration` 的 exclude。  
- **启动报数据源错误**：检查 MySQL 是否监听、库名与用户权限、`url` 中时区与参数。  
- **找不到 Mapper XML**：检查 `mybatis.mapper-locations` 与 XML 实际路径、`namespace`、以及 `travel-user` 的 `resources` 是否被打进 jar。  

---

*文档与当前仓库模块结构（`travel-assistant` 多模块）对齐；若你后续把 MyBatis 仅用于某一子模块，保持 `bootstrap` 依赖该模块即可，无需重复引入多个 starter。*
