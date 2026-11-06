# 亿级高并发电商平台

基于 **Spring Cloud Alibaba** 生态的企业级电商解决方案，覆盖 **微服务后端 + Vue3 管理后台 + uni-app 多端（H5/微信小程序/App）+ Docker Compose 一键部署**。

## 技术栈

| 层 | 技术 |
|---|---|
| 后端 | Spring Boot 3.2 + Spring Cloud 2023 + Spring Cloud Alibaba 2023 + JDK 17 |
| 微服务 | Nacos（注册/配置）、**Higress 云原生网关**、**Dubbo RPC**、MyBatis-Plus、Sentinel（规划）、Seata（规划） |
| 中间件 | MySQL 8、Redis 7、RocketMQ 5、MinIO |
| 前端 | Vue3 + Vite + Element Plus + Pinia + Axios |
| 多端 | uni-app（Vue3） |
| 部署 | Docker + Docker Compose |

## 工程结构

```
├─ baizhan-common-core       通用内核（Result/分页/异常/ID生成）
├─ baizhan-common-security   安全内核（JWT/密码工具/服务级鉴权拦截器）
├─ baizhan-api               跨服务 Dubbo 接口与 DTO
├─ baizhan-auth              认证服务（9101，登录注册签发 JWT）
├─ baizhan-user              用户服务（9102，我的信息/收货地址）
├─ baizhan-product           商品服务（9103，商品/广告 + Redis 缓存）
├─ baizhan-order             订单服务（9104，下单/查询 + RocketMQ 延迟消息）
├─ frontend                  Vue3 管理后台（5173 开发 / 80 生产）
├─ mobile                    uni-app 多端应用
├─ docker                    Compose、初始化 SQL、镜像构建
└─ docs                      方案文档（架构/规范/高并发/前端/部署）
```

## 快速启动

### 方式一：Docker Compose 一键部署（推荐）

```powershell
# 1. 宿主机打包后端（Compose 使用预构建 Jar 构建镜像）
mvn clean package -DskipTests

# 2. 启动全部服务（镜像已在本地构建过时用 --no-build；
#    若需重新构建而 buildx 报 gRPC 错误，参考下方"排障备忘"）
cd docker
docker compose up -d --no-build

# 3. 查看容器状态
docker compose ps
```

### 方式二：本地开发（IDE 运行 + Docker 基础设施）

```powershell
# 1. 启动基础设施
cd docker
docker compose up -d mysql redis nacos rocketmq-namesrv rocketmq-broker minio

# 2. 依次启动（或 IDE 中运行各 *Application；网关为 Higress 容器，见 docker/）
mvn -pl baizhan-auth,baizhan-user,baizhan-product,baizhan-order spring-boot:run

# 3. 启动管理后台
cd frontend
npm install
npm run dev        # http://localhost:5173
```

## 访问入口

| 服务 | 地址 | 账号 |
|---|---|---|
| 管理后台 | http://localhost | baizhan / baizhan123（admin）<br>user001 / user123（user） |
| Higress 网关 | http://localhost:8080 | - |
| Higress 控制台 | http://localhost:8001 | - |
| 接口文档 | http://localhost:9103/doc.html | - |
| Nacos 控制台 | http://localhost:8848/nacos | nacos / nacos |
| RocketMQ Dashboard | http://localhost:8180 | - |
| MinIO 控制台 | http://localhost:9001 | minioadmin / minioadmin |

## 核心流程演示

1. **登录**：`POST /api/auth/login` → 签发 JWT（`baizhan-auth`）；
2. **鉴权链路**：请求带 `Authorization: Bearer <token>` → Higress 纯路由 → **业务服务级 JWT 校验**（`common-security` 拦截器 + `UserContext`，无状态共享密钥）；
3. **商品浏览**：`GET /api/product/goods/page` → Redis 缓存旁路策略（商品/广告接口免登录）；
4. **下单**：`POST /api/order/create` → **Dubbo RPC** 调商品服务取价（服务端计价）→ 落库 → RocketMQ 延迟消息（30 分钟关单）。

## 已验证（本机 Docker 环境实测通过）

- [x] `mvn clean package` 7 个模块全部编译打包（JDK17）
- [x] MySQL 8 自动导入 `baizhanshopping`（22 张表），Nacos/Redis/RocketMQ 就绪
- [x] Higress 经 McpBridge 对接 Nacos 服务发现，4 条 `/api/**` 路由全部生效
- [x] 服务级 JWT 鉴权（无令牌 401 / 假令牌 401 / 白名单放行）
- [x] Dubbo RPC 跨服务调用（订单 → 商品，Nacos 注册 `baizhan-product-dubbo:20880`）
- [x] 商品分页（COUNT+LIMIT 正常，total=7）、详情 Redis 缓存、广告位
- [x] 用户信息、下单（Dubbo 取价）、我的订单
- [x] RocketMQ 延迟消息发送成功（`[RocketMQ] 超时关单消息已发送`）
- [x] Vue3 管理后台 `npm run build` 成功，Nginx → Higress → 业务服务全链路 200

## 排障备忘（实测踩坑）

| 现象 | 原因 | 处理 |
|---|---|---|
| Higress 只有第一条 `/api` 路由生效，其余 404 | 文件式配置每个 YAML 文件只加载第一个文档 | 一个 Ingress 一个文件（`docker/higress/data/ingresses/*.yaml`，已处理） |
| Dubbo 启动报 `Failed to create directory /home/xxx/.dubbo` | 镜像用 `useradd -r` 建系统用户无家目录，Dubbo 元数据缓存写不进去 | Dockerfile 改 `useradd -m`（已处理） |
| 网关把请求路由到 20880 端口报 `invalid version format: UNSUPPORTED` | Dubbo 实例注册进了 Spring Cloud 同名服务（Nacos 里一个服务两个端口） | `dubbo.application.name` 加 `-dubbo` 后缀隔离（已处理） |
| `@RequestParam` 报 "parameter name information not available" | 未开启 `-parameters` 编译参数 | 父 POM 配置 `maven-compiler-plugin` `<parameters>true</parameters>`（已处理） |
| `Unknown column 'nick_name'` | 本库列名为驼峰，MP 默认转下划线 | `mybatis-plus.configuration.map-underscore-to-camel-case: false`（已处理） |
| 分页无 LIMIT/COUNT、total=0 | MP 3.5.x 不再自动注册分页拦截器 | `common-core` 中显式定义 `MybatisPlusInterceptor`（已处理） |
| jar 运行报 "没有主清单属性" | `spring-boot-maven-plugin` 未绑定 `repackage` | 父 POM pluginManagement 增加 executions（已处理） |
| 镜像构建找不到 jar | `.dockerignore` 排除了 `**/target/` | 已移除该忽略项 |
| `docker compose build` 报 gRPC 错误 | Docker Desktop + Buildx 兼容问题（环境相关） | 重启 Docker Desktop，或 `docker build` 经典构建器手动构建 |
| `docker compose up -d` 报 `No such image: docker-xxx:latest` | 业务服务未声明 `image:`，默认镜像名与本地不符 | compose 已显式声明 `image: baizhan-xxx:1.0.0`，本地构建同名镜像即可 |
| minio/dashboard 镜像拉取 EOF（镜像源故障） | 镜像源临时不可用 | 先跳过：`docker compose up -d --no-build mysql redis nacos rocketmq-namesrv rocketmq-broker higress baizhan-auth baizhan-user baizhan-product baizhan-order baizhan-frontend`（MinIO/Dashboard 为可选组件，不影响主流程）；Higress 镜像用阿里云 ACR 路径 `higress-registry.cn-hangzhou.cr.aliyuncs.com/higress/all-in-one:latest` |
| 主机跑服务 + 容器跑中间件时 MQ 超时 | Broker 注册容器内网 IP，主机不可达 | 业务服务与中间件统一放 Docker 网络（Compose 部署即正常） |
| 业务容器莫名退出（`Exited (137)`），Nacos 服务列表变空 | Docker（WSL2）内存超限被 OOM Kill；RocketMQ Broker 默认 1G 堆 + Nacos 常驻是主要消耗 | compose 已调低堆：业务服务 `-Xmx320m`、Broker `JAVA_OPT_EXT=-Xmx512m`、Nacos `JVM_XMS=128m`，整套约 4.7G/7.6G；仍紧张可在 `%USERPROFILE%\.wslconfig` 给 WSL2 分配更多内存 |
| 自定义 broker.conf 不生效 | 挂载路径写成 `rocketmq-5.3.0`，镜像是 5.3.2 | 已修正为 `rocketmq-5.3.2/conf/broker.conf` |
| Nacos 控制台 404 | 访问地址少了尾斜杠（须为 `http://localhost:8848/nacos/`） | 带尾斜杠访问 |
| 服务列表接口 404/500 | Nacos 2.3.2 控制台调用 naming 模块 `/v1/ns/catalog/services`，参数名与旧版不同 | 属正常现象，注册数据以 `/v1/ns/service/list` 为准 |

## 高并发设计要点

详见 [docs/03-亿级高并发设计方案.md](docs/03-亿级高并发设计方案.md)：多级缓存、秒杀 Lua 预扣、MQ 削峰、分库分表、Sentinel 限流熔断、幂等与对账补偿。

## 环境变量

| 变量 | 默认 | 说明 |
|---|---|---|
| `MYSQL_HOST` / `MYSQL_PASSWORD` | mysql / baizhan123 | 数据库 |
| `REDIS_HOST` / `REDIS_PASSWORD` | redis / baizhan123 | 缓存 |
| `NACOS_SERVER` | nacos:8848 | 注册配置中心 |
| `ROCKETMQ_ADDR` / `ROCKETMQ_ENABLED` | rocketmq-namesrv:9876 / false | 消息队列 |
| `BAZHAN_JWT_SECRET` | 内置默认值 | **生产必须覆盖** |
| `JAVA_OPTS` | -Xms256m -Xmx512m | JVM 参数 |

## 路线图

- [ ] baizhan-cart 购物车、baizhan-pay 支付（微信/支付宝）、baizhan-seckill 秒杀、baizhan-admin 运营后台
- [ ] Sentinel 限流熔断、Seata 分布式事务
- [ ] 订单分库分表 + 读写分离
- [ ] K8s + HPA + CI/CD（Jenkins/GitLab Runner + Harbor）
- [ ] 可观测性：Prometheus + Grafana + SkyWalking
