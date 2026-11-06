import { get } from './request.js'

/** 商品分页 */
export const getGoodsPage = (params) =>
  get('/product/goods/page', { current: 1, size: 10, ...params })

/** 商品详情 */
export const getGoods = (id) => get(`/product/goods/${id}`)
