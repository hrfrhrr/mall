/**
 * 多端 API 地址：
 * - H5：走 /api 相对路径，由 vite proxy / 生产 Nginx 转发网关
 * - 微信小程序：必须配置合法域名，开发期可在开发者工具关闭域名校验
 * - App：直接请求网关地址
 */
const BASE_URL =
  process.env.NODE_ENV === 'development'
    ? '/api'
    : 'http://localhost:9000'

export const API_BASE = BASE_URL

export default {
  API_BASE
}
