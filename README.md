# 校园活动座位智能分配系统

基于 Spring Boot + React + Redis 的校园活动座位分配系统，支持学生选择偏好区域、团队连座、管理员手动调整和 Excel 导出。

## 功能特性

- 学生报名时选择偏好区域（前排、后排、靠窗、不限）
- 支持团队报名，同团队自动分配连座
- 基于报名顺序和偏好的智能座位分配算法
- 可视化座位图（颜色区分状态）
- 管理员手动调整/交换座位
- 导出座位表 Excel（含座位图 + 报名列表两个 Sheet）
- Redis 分布式锁保证并发分配安全性

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Spring Boot 3.2, Spring Data JPA, Spring Data Redis |
| 数据库 | H2 (内嵌) |
| 缓存/锁 | Redis 7 |
| 前端 | React 18, TypeScript, Vite, Ant Design 5 |
| 导出 | Apache POI |

## 快速启动

### 前置要求

- JDK 17+
- Node.js 18+
- Docker (用于 Redis)

### 1. 启动 Redis

```bash
docker-compose up -d
```

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端启动后访问 http://localhost:8080/h2-console 可查看数据库（JDBC URL: `jdbc:h2:mem:seatdb`）。

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端访问 http://localhost:3000

## 项目结构

```
├── backend/                    Spring Boot 后端
│   └── src/main/java/com/campus/seat/
│       ├── algorithm/          座位分配算法
│       ├── config/             Redis、CORS 配置
│       ├── controller/         REST 控制器
│       ├── dto/                数据传输对象
│       ├── entity/             JPA 实体
│       ├── repository/         数据访问层
│       └── service/            业务逻辑层
├── frontend/                   React 前端
│   └── src/
│       ├── api/                API 调用封装
│       ├── components/         组件 (SeatGrid, SeatCell, RegistrationForm)
│       ├── pages/              页面 (Home, Register, SeatMap, Admin)
│       └── types/              TypeScript 类型定义
└── docker-compose.yml          Redis 容器编排
```

## API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/activities | 获取活动列表 |
| POST | /api/activities | 创建活动 |
| POST | /api/activities/{id}/register | 学生报名 |
| POST | /api/activities/{id}/allocate | 触发座位分配 |
| GET | /api/activities/{id}/seat-map | 获取座位图数据 |
| PUT | /api/seats/adjust | 手动调整座位 |
| PUT | /api/seats/swap | 交换两个座位 |
| GET | /api/activities/{id}/export/excel | 导出 Excel |

## 座位分配算法

1. 获取 Redis 分布式锁
2. 按报名时间排序，团队优先、个人其次
3. 团队分配：在偏好区域寻找连续座位 → 任意区域连续座位 → 最近聚簇
4. 个人分配：偏好区域最优座位 → 任意可用座位
5. 持久化结果，释放锁

## 演示数据

系统启动时自动创建：
- 1 个场地（5排×10列，两侧有窗）
- 1 个活动（2026年春季技术分享会）
- 5 个测试用户
