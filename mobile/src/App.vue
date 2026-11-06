<script>
export default {
  name: 'App',
  onLaunch() {
    // #ifdef MP-WEIXIN
    // 小程序启动时检查登录态
    const token = uni.getStorageSync('token')
    if (!token) {
      console.log('[小程序] 未登录')
    }
    // #endif
  },
  onShow() {},
  onHide() {}
}
</script>

<style lang="scss">
/* 全局样式 */
page {
  background: #f5f6f8;
  font-size: 28rpx;
  color: #303133;
}
</style>
