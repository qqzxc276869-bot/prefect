import request from '@/utils/request'

// 查询库存列表
export function getInventoryList(params) {
  return request({
    url: '/inventory/list',
    method: 'get',
    params
  })
}

// 查询预警列表
export function getWarningList() {
  return request({
    url: '/inventory/warning',
    method: 'get'
  })
}

// 更新预警阈值
export function updateThreshold(id, data) {
  return request({
    url: `/inventory/threshold/${id}`,
    method: 'put',
    data
  })
}






