import { post } from './request.js'

/** 登录（type: user） */
export const login = (data) => post('/auth/login', data)

/** 注册 */
export const register = (data) => post('/auth/register', data)
