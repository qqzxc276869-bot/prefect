import request from '@/utils/request'

// 智能预测相关API

/**
 * 为指定试剂生成预测
 */
export function generateForecast(reagentId) {
  return request({
    url: `/forecasting/generate/${reagentId}`,
    method: 'post'
  })
}

/**
 * 获取所有活跃预测
 */
export function getActiveForecasts() {
  return request({
    url: '/forecasting/active',
    method: 'get'
  })
}

/**
 * 获取即将需要采购的试剂
 */
export function getUpcomingOrders() {
  return request({
    url: '/forecasting/upcoming',
    method: 'get'
  })
}

