import request from '@/utils/request'

// 提交反馈
export function submitFeedback(data) {
  return request({
    url: '/feedback/submit',
    method: 'post',
    data
  })
}

// 查询我的反馈
export function getMyFeedback() {
  return request({
    url: '/feedback/my',
    method: 'get'
  })
}

// 查询所有反馈
export function getAllFeedback() {
  return request({
    url: '/feedback/all',
    method: 'get'
  })
}

// 查询待处理反馈
export function getPendingFeedback() {
  return request({
    url: '/feedback/pending',
    method: 'get'
  })
}

// 处理反馈
export function handleFeedback(id, data) {
  return request({
    url: `/feedback/handle/${id}`,
    method: 'post',
    data
  })
}




