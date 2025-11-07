import request from '@/utils/request'

// 废弃物管理相关API

/**
 * 分页查询废弃物记录
 */
export function getWastePage(params) {
  return request({
    url: '/waste/page',
    method: 'get',
    params
  })
}

/**
 * 创建废弃物登记
 */
export function createWaste(data) {
  return request({
    url: '/waste/create',
    method: 'post',
    data
  })
}

/**
 * 更新状态
 */
export function updateWasteStatus(data) {
  return request({
    url: '/waste/update-status',
    method: 'post',
    data
  })
}

/**
 * 获取所有废弃物类别
 */
export function getWasteCategories() {
  return request({
    url: '/waste/categories',
    method: 'get'
  })
}

/**
 * 根据ID获取废弃物记录
 */
export function getWasteById(id) {
  return request({
    url: `/waste/${id}`,
    method: 'get'
  })
}

/**
 * 生成标签
 */
export function generateWasteLabel(id) {
  return request({
    url: `/waste/label/${id}`,
    method: 'get'
  })
}

