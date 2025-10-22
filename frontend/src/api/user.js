import request from '@/utils/request'

// 查询用户列表
export function getUserList(params) {
  return request({
    url: '/user/list',
    method: 'get',
    params
  })
}

// 添加用户
export function addUser(data) {
  return request({
    url: '/user/add',
    method: 'post',
    data
  })
}

// 更新用户
export function updateUser(data) {
  return request({
    url: '/user/update',
    method: 'put',
    data
  })
}

// 更新用户状态
export function updateUserStatus(id, status) {
  return request({
    url: `/user/status/${id}/${status}`,
    method: 'put'
  })
}

// 删除用户
export function deleteUser(id) {
  return request({
    url: `/user/delete/${id}`,
    method: 'delete'
  })
}







