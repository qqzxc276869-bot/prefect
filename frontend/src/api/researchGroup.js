import request from '@/utils/request'

// 课题组相关API

/**
 * 分页查询课题组
 */
export function getResearchGroupPage(params) {
  return request({
    url: '/research-group/page',
    method: 'get',
    params
  })
}

/**
 * 根据ID获取课题组
 */
export function getResearchGroupById(id) {
  return request({
    url: `/research-group/${id}`,
    method: 'get'
  })
}

/**
 * 保存或更新课题组
 */
export function saveResearchGroup(data) {
  return request({
    url: '/research-group/save',
    method: 'post',
    data
  })
}

/**
 * 删除课题组
 */
export function deleteResearchGroup(id) {
  return request({
    url: `/research-group/${id}`,
    method: 'delete'
  })
}

/**
 * 添加成员
 */
export function addMember(data) {
  return request({
    url: '/research-group/member/add',
    method: 'post',
    data
  })
}

/**
 * 获取成员列表
 */
export function getMembers(groupId) {
  return request({
    url: `/research-group/member/list/${groupId}`,
    method: 'get'
  })
}

/**
 * 移除成员
 */
export function removeMember(id) {
  return request({
    url: `/research-group/member/${id}`,
    method: 'delete'
  })
}

