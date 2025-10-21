import request from '@/utils/request'

// 入库
export function stockIn(data) {
  return request({
    url: '/stock/in',
    method: 'post',
    data
  })
}

// 出库
export function stockOut(data) {
  return request({
    url: '/stock/out',
    method: 'post',
    data
  })
}

// 入库记录查询
export function getStockInList(params) {
  return request({
    url: '/stock/in/list',
    method: 'get',
    params
  })
}

// 出库记录查询
export function getStockOutList(params) {
  return request({
    url: '/stock/out/list',
    method: 'get',
    params
  })
}






