## 项目结构
```
live-shop
├── gateway              ⭐ API网关（统一入口）
├── common               ⭐ 公共模块（工具/DTO/配置）
├── user-service         用户服务
├── live-service         直播服务
├── order-service        订单服务
├── seckill-service      秒杀服务
├── payment-service      支付服务（后期）
├── points-service       积分服务（后期）
```

- 用户服务：登录 / 注册、JWT签发、用户信息管理
- 直播服务：直播间管理、WebSocket聊天、实时统计人数（Redis）
- 订单服务：订单管理、订单支付、订单取消、订单查询
- 秒杀服务：秒杀商品管理、Redis预减库存、MQ异步削峰
- 支付服务：微信支付、支付宝支付
- 积分服务：用户积分管理、MQ异步处理
- gateway网关：统一入口、权限验证、负载均衡、限流