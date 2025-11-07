import request from '@/utils/request'

// GHS/MSDS 相关API

/**
 * 分页查询GHS/MSDS
 */
export function getGhsMsdsPage(params) {
  return request({
    url: '/ghs/page',
    method: 'get',
    params
  })
}

/**
 * 根据试剂ID获取MSDS
 */
export function getGhsMsdsByReagentId(reagentId) {
  return request({
    url: `/ghs/reagent/${reagentId}`,
    method: 'get'
  })
}

/**
 * 保存或更新MSDS
 */
export function saveGhsMsds(data) {
  return request({
    url: '/ghs/save',
    method: 'post',
    data
  })
}

/**
 * 删除MSDS
 */
export function deleteGhsMsds(id) {
  return request({
    url: `/ghs/${id}`,
    method: 'delete'
  })
}

/**
 * 获取未验证的MSDS列表
 */
export function getUnverifiedMsds() {
  return request({
    url: '/ghs/unverified',
    method: 'get'
  })
}

/**
 * 检查存储兼容性
 */
export function checkStorageCompatibility(data) {
  return request({
    url: '/storage/compatibility/check',
    method: 'post',
    data
  })
}

