import request from '@/utils/request'

// 查询分类列表
export function getCategoryList() {
  return request({
    url: '/base/category/list',
    method: 'get'
  })
}

// 添加分类
export function addCategory(data) {
  return request({
    url: '/base/category/add',
    method: 'post',
    data
  })
}

// 删除分类
export function deleteCategory(id) {
  return request({
    url: `/base/category/delete/${id}`,
    method: 'delete'
  })
}

// 查询存放位置列表
export function getLocationList() {
  return request({
    url: '/base/location/list',
    method: 'get'
  })
}

// 添加存放位置
export function addLocation(data) {
  return request({
    url: '/base/location/add',
    method: 'post',
    data
  })
}

// 删除存放位置
export function deleteLocation(id) {
  return request({
    url: `/base/location/delete/${id}`,
    method: 'delete'
  })
}






