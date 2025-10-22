import request from '@/utils/request'

// 查询试剂列表
export function getReagentList(params) {
  return request({
    url: '/reagent/list',
    method: 'get',
    params
  })
}

// 添加试剂
export function addReagent(data) {
  return request({
    url: '/reagent/add',
    method: 'post',
    data
  })
}

// 更新试剂
export function updateReagent(data) {
  return request({
    url: '/reagent/update',
    method: 'put',
    data
  })
}

// 删除试剂
export function deleteReagent(id) {
  return request({
    url: `/reagent/delete/${id}`,
    method: 'delete'
  })
}







