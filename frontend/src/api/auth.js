import request from '@/utils/request'

/** 登录（type: admin 运营后台 / user C端） */
export function login(data) {
  return request.post('/auth/login', data)
}

/** 令牌校验 */
export function check() {
  return request.get('/auth/check')
}
