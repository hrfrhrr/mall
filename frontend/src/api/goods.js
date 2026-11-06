import request from '@/utils/request'

/** 商品分页 */
export function getGoodsPage(params) {
  return request.get('/product/goods/page', { params })
}

/** 商品详情 */
export function getGoods(id) {
  return request.get(`/product/goods/${id}`)
}

/** 更新商品 */
export function updateGoods(data) {
  return request.put('/product/goods', data)
}

/** 首页广告位 */
export function getAds() {
  return request.get('/product/ads')
}
