import request from '@/utils/request'

export function getAnnouncements(params) {
  return request({
    url: '/announcement',
    method: 'get',
    params
  })
}

export function createAnnouncement(data) {
  return request({
    url: '/announcement',
    method: 'post',
    data
  })
}

export function deleteAnnouncement(id) {
  return request({
    url: `/announcement/${id}`,
    method: 'delete'
  })
}


