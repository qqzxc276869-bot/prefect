import request from '@/utils/request'

// SOP 相关API

/**
 * 分页查询SOP
 */
export function getSopPage(params) {
  return request({
    url: '/sop/page',
    method: 'get',
    params
  })
}

/**
 * 根据ID获取SOP
 */
export function getSopById(id) {
  return request({
    url: `/sop/${id}`,
    method: 'get'
  })
}

/**
 * 保存或更新SOP
 */
export function saveSop(data) {
  return request({
    url: '/sop/save',
    method: 'post',
    data
  })
}

/**
 * 删除SOP
 */
export function deleteSop(id) {
  return request({
    url: `/sop/${id}`,
    method: 'delete'
  })
}

/**
 * 检查培训状态
 */
export function checkTraining(userId, sopId) {
  return request({
    url: '/sop/training/check',
    method: 'get',
    params: { userId, sopId }
  })
}

/**
 * 记录培训
 */
export function recordTraining(data) {
  return request({
    url: '/sop/training/record',
    method: 'post',
    data
  })
}

/**
 * 获取用户培训记录
 */
export function getUserTraining(userId) {
  return request({
    url: `/sop/training/user/${userId}`,
    method: 'get'
  })
}

