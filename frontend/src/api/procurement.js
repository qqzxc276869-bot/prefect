import request from '@/utils/request'

// 采购相关API

/**
 * 分页查询采购申请
 */
export function getProcurementPage(params) {
  return request({
    url: '/procurement/page',
    method: 'get',
    params
  })
}

/**
 * 创建采购申请
 */
export function createProcurement(data) {
  return request({
    url: '/procurement/create',
    method: 'post',
    data
  })
}

/**
 * PI审批
 */
export function piApproveProcurement(data) {
  return request({
    url: '/procurement/approve/pi',
    method: 'post',
    data
  })
}

/**
 * 管理员审批（下单）
 */
export function adminApproveProcurement(data) {
  return request({
    url: '/procurement/approve/admin',
    method: 'post',
    data
  })
}

/**
 * 根据ID获取采购申请
 */
export function getProcurementById(id) {
  return request({
    url: `/procurement/${id}`,
    method: 'get'
  })
}

