# mobile · uni-app 多端应用

一套代码同时运行 **H5 / 微信小程序 / App**，对接后端网关（`baizhan-gateway:9000`）。

## 目录说明

```
mobile/
├─ manifest.json      # 应用配置（appid、H5 代理、小程序设置）
├─ pages.json         # 页面路由 / tabBar / 全局样式
├─ src/
│  ├─ main.js         # Vue3 入口（createSSRApp）
│  ├─ App.vue
│  ├─ uni.scss
│  ├─ config/         # API 地址（H5 相对路径 + 代理；小程序填合法域名）
│  ├─ api/            # 请求封装（uni.request）与业务接口
│  └─ pages/          # index 首页 / detail 详情 / login 登录
```

## 运行方式

推荐使用 **HBuilderX**（uni-app 官方 IDE）：

1. HBuilderX → 文件 → 打开目录 → 选择 `mobile`；
2. 运行 → 运行到浏览器 → H5（开发环境通过 vite 代理 `/api` 访问网关 `localhost:9000`）；
3. 运行 → 运行到小程序模拟器 → 微信开发者工具（首次需在 `manifest.json` 填入你的小程序 appid）；
4. 运行 → 运行到手机或模拟器 → App（需真机调试或云打包）。

命令行方式（H5）：

```bash
npm i -g @dcloudio/uni-cli   # 或使用 HBuilderX 内置 cli
npm run dev:h5
npm run build:h5             # 产物 dist/h5，可交由 Nginx 托管
```

## 注意事项

- 小程序端 `src/config/index.js` 的生产地址必须为 **HTTPS 合法域名**，开发阶段可在微信开发者工具中勾选「不校验合法域名」；
- 登录态存储于 `uni.setStorageSync('token')`，请求拦截器自动附加 `Authorization`；
- 后续规划：微信一键登录（`uni.login` 拿 code → 后端换 openid → JWT）、支付（微信 JSAPI/V3）、订阅消息。
