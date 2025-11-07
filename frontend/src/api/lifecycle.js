import request from '@/utils/request'

// 试剂生命周期相关API

/**
 * 创建生命周期记录
 */
export function createLifecycle(data) {
  return request({
    url: '/lifecycle/create',
    method: 'post',
    data
  })
}

/**
 * 扫码查询
 */
export function scanQrCode(qrCode, userId, userName) {
  return request({
    url: `/lifecycle/scan/${qrCode}`,
    method: 'get',
    params: { userId, userName }
  })
}

/**
 * 标记为已开封
 */
export function markAsOpened(data) {
  return request({
    url: '/lifecycle/mark-opened',
    method: 'post',
    data
  })
}

/**
 * 记录使用
 */
export function recordUsage(data) {
  return request({
    url: '/lifecycle/record-usage',
    method: 'post',
    data
  })
}

/**
 * 转移库位
 */
export function transferLocation(data) {
  return request({
    url: '/lifecycle/transfer',
    method: 'post',
    data
  })
}

