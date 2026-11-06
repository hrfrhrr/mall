# 05 · Docker 部署方案

## 1. 一键部署架构

```
docker-compose.yml
├─ mysql        8.0        （自动导入 docker/baizhanshopping.sql + migrations/01-optimize.sql）
├─ redis        7.2-alpine （requirepass baizhan123）
├─ nacos        v2.3.2     （standalone 模式，注册+配置中心）
├─ rocketmq-namesrv / rocketmq-broker / rocketmq-dashboard
├─ higress      all-in-one （云原生网关：8080 HTTP 入口 / 8001 控制台，
│                              McpBridge 对接 Nacos 服务发现，Ingress 路由见 higress/data/）
├─ minio        latest     （对象存储，自动创建 baizhan 桶）
├─ minio-init   mc         （初始化桶，一次性任务）
├─ baizhan-auth / user / product / order   （业务镜像，Dubbo RPC + 服务级 JWT）
└─ baizhan-frontend        （Nginx 托管 Vue3 构建产物，80 端口，/api 反代到 higress:8080）
```

## 2. 使用步骤

```powershell
# 1. 先在宿主机打包后端（Compose 使用预构建 Jar 构建镜像）
mvn clean package -DskipTests

# 2. 一键启动全部基础设施 + 业务服务 + 前端
docker compose up -d --build

# 3. 查看状态
docker compose ps
```

仅启动基础设施做本地开发：

```powershell
docker compose up -d mysql redis nacos rocketmq-namesrv rocketmq-broker minio
```

## 3. 环境变量注入（同一镜像多环境）

| 变量 | 默认值 | 说明 |
|---|---|---|
| `MYSQL_HOST` | mysql | 容器网络内为服务名 |
| `MYSQL_PASSWORD` | baizhan123 | 与 compose 保持一致 |
| `REDIS_HOST` / `REDIS_PASSWORD` | redis / baizhan123 | |
| `NACOS_SERVER` | nacos:8848 | 注册配置中心 |
| `ROCKETMQ_ADDR` | rocketmq-namesrv:9876 | 为空则关闭 MQ |
| `JAVA_OPTS` | -Xms128m -Xmx320m | JVM 参数（已按 8G Docker 内存调低，防 OOM） |

## 4. 访问入口

| 服务 | 地址 |
|---|---|
| 管理后台 | http://localhost |
| Higress 网关 | http://localhost:8080 |
| Higress 控制台 | http://localhost:8001 |
| 接口文档 | http://localhost:9103/doc.html （各服务端口） |
| Nacos 控制台 | http://localhost:8848/nacos （nacos/nacos） |
| RocketMQ Dashboard | http://localhost:8180 |
| MinIO 控制台 | http://localhost:9001 （minioadmin/minioadmin） |

## 4.1 Higress 路由配置说明

Higress 采用文件式配置（容器内 `/data`，挂载自 `docker/higress/data/`）：

- `mcpbridges/default.yaml`：服务来源。除控制台自身的 static 来源外，配置 `type: nacos2` 对接 Nacos 2.x（gRPC）；
- `ingresses/baizhan-*.yaml`：每个服务一个文件，通过 `higress.io/destination: 服务名.DEFAULT-GROUP.public.nacos` 注解指向 Nacos 中的服务；
- 注意：**一个 YAML 文件只加载第一个文档**，多文档必须拆分为多个文件；
- 新增服务路由：复制现有 ingress 文件改 `name`/`destination`/`path` 后 `docker compose restart higress`。

## 5. 镜像分层与优化

- 后端：`eclipse-temurin:17-jre-jamji` 运行层，Jar 外置；生产建议加 `-XX:+UseZGC -XX:MaxRAMPercentage=75`；
- 前端：`node:20-alpine` 构建 → `nginx:1.27-alpine` 运行，镜像 < 50MB；
- 健康检查：基础设施使用 `healthcheck`，业务服务 `depends_on: condition: service_healthy` 保证启动顺序；
- 日志：容器日志限制 `max-size=50m max-file=3`，生产对接 ELK/Loki。

## 6. 演进到 Kubernetes

1. Compose 文件结构与 K8s 资源一一对应（Service/Deployment/ConfigMap/Secret）；
2. 配置文件迁移 Nacos 命名空间（dev/test/prod 隔离）；
3. 引入 HPA（按 CPU/QPS 弹性扩容网关与商品服务）；
4. 中间件外置：RDS、云 Redis、托管 MQ、OSS；
5. CI/CD：GitLab Runner/Jenkins 构建镜像 → 推送 Harbor → kubectl apply 或 Helm 升级。
