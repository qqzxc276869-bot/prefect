import request from '@/utils/request'

// 提交申请
export function submitApplication(data) {
  return request({
    url: '/application/submit',
    method: 'post',
    data
  })
}

// 查询我的申请
export function getMyApplications() {
  return request({
    url: '/application/my',
    method: 'get'
  })
}

// 查询待审批申请
export function getPendingApplications() {
  return request({
    url: '/application/pending',
    method: 'get'
  })
}

// 查询所有申请
export function getAllApplications() {
  return request({
    url: '/application/all',
    method: 'get'
  })
}

// 审批申请
export function reviewApplication(id, data) {
  return request({
    url: `/application/review/${id}`,
    method: 'post',
    data
  })
}






