import request from '@/utils/request'

// 导出库存清单
export function exportInventory() {
  return request({
    url: '/export/inventory',
    method: 'get',
    responseType: 'blob'
  })
}

// 导出入库记录
export function exportStockIn(params) {
  return request({
    url: '/export/stock-in',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

// 导出出库记录
export function exportStockOut(params) {
  return request({
    url: '/export/stock-out',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

// 获取FIFO建议
export function getFifoSuggestion(reagentId) {
  return request({
    url: '/fifo/suggestion',
    method: 'get',
    params: { reagentId }
  })
}

// 获取AI补货建议
export function getReplenishSuggestions(data) {
  return request({
    url: '/ai/replenish/suggest',
    method: 'post',
    data
  })
}




