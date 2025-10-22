import request from '@/utils/request'

// AI通用对话
export function chatWithAI(model, messages) {
  return request({
    url: '/ai/chat',
    method: 'post',
    data: { model, messages }
  })
}

// 生成库存管理建议
export function generateInventoryAdvice(reagentName, currentStock, unit) {
  return request({
    url: '/ai/inventory-advice',
    method: 'post',
    params: { reagentName, currentStock, unit }
  })
}

// 生成安全处理建议
export function generateSafetyAdvice(chemicalName, scenario) {
  return request({
    url: '/ai/safety-advice',
    method: 'post',
    params: { chemicalName, scenario }
  })
}

// 语义检索
export function semanticSearch(params) {
  return request({
    url: '/ai/semantic-search',
    method: 'post',
    data: params
  })
}

// 申领表单智能优化
export function optimizeApplyForm(params) {
  return request({
    url: '/ai/apply-optimize',
    method: 'post',
    data: params
  })
}

// 公告智能生成
export function generateAnnouncement(params) {
  return request({
    url: '/ai/announcement/generate',
    method: 'post',
    data: params
  })
}

// 入库智能提示
export function stockInHint(params) {
  return request({
    url: '/ai/stockin/hint',
    method: 'post',
    data: params
  })
}

// 审批AI预审
export function approvePrecheck(params) {
  return request({
    url: '/ai/approve/precheck',
    method: 'post',
    data: params
  })
}

// AI行为风控
export function riskAnalyze(params) {
  return request({
    url: '/ai/risk/analyze',
    method: 'post',
    data: params
  })
}

// 导出数据AI分析
export function exportAnalyze(params) {
  return request({
    url: '/ai/export/analyze',
    method: 'post',
    data: params
  })
}

// 兼容旧版本的aiChat函数
export function aiChat(data) {
  return chatWithAI(data.model, data.messages)
}


