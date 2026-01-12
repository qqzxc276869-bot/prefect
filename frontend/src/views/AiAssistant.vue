<template>
  <div class="ai-assistant-page">
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <div class="header-icon-wrapper">
            <i class="el-icon-chat-dot-round header-icon"></i>
          </div>
          <div>
            <h1 class="page-title">AI智能助手</h1>
            <p class="page-subtitle">专业的试剂管理智能助手，为您提供全方位的帮助</p>
          </div>
        </div>
        <div class="header-actions">
          <el-button @click="handleBack" icon="el-icon-back">返回</el-button>
          <el-button type="primary" plain @click="clearChat" icon="el-icon-delete" :disabled="messages.length <= 1">清空对话</el-button>
        </div>
      </div>
    </div>

    <div class="chat-wrapper">
      <!-- 侧边栏 - 快捷功能 -->
      <div class="sidebar">
        <div class="sidebar-section">
          <h3 class="section-title">
            <i class="el-icon-star-on"></i>
            快捷功能
          </h3>
          <div class="quick-actions">
            <div
              v-for="action in quickActions"
              :key="action.key"
              class="quick-action-item"
              @click="handleQuickAction(action)"
              :class="{ disabled: loading }"
            >
              <i :class="action.icon || 'el-icon-chat-line-round'"></i>
              <span>{{ action.label }}</span>
            </div>
          </div>
        </div>

        <div class="sidebar-section">
          <h3 class="section-title">
            <i class="el-icon-info"></i>
            使用提示
          </h3>
          <div class="tips">
            <div class="tip-item">
              <i class="el-icon-key"></i>
              <span>Enter 发送消息</span>
            </div>
            <div class="tip-item">
              <i class="el-icon-key"></i>
              <span>Shift + Enter 换行</span>
            </div>
            <div class="tip-item">
              <i class="el-icon-chat-line-round"></i>
              <span>支持多轮对话</span>
            </div>
            <div class="tip-item">
              <i class="el-icon-refresh"></i>
              <span>可随时清空对话</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 主聊天区域 -->
      <div class="chat-main">
        <div class="chat-container">
          <!-- 对话历史 -->
          <div class="chat-history" ref="chatHistory">
            <div
              v-for="(message, index) in messages"
              :key="index"
              :class="['message-wrapper', message.role]"
            >
              <div class="message-avatar">
                <i :class="getMessageIcon(message.role)"></i>
              </div>
              <div class="message-content-wrapper">
                <div class="message-header">
                  <span class="message-role">{{ getMessageLabel(message.role) }}</span>
                  <span class="message-time" v-if="message.timestamp">{{ formatTime(message.timestamp) }}</span>
                </div>
                <div class="message-body" v-html="formatMessage(message.content)"></div>
              </div>
            </div>

            <!-- 加载状态 -->
            <div v-if="loading" class="message-wrapper assistant loading">
              <div class="message-avatar">
                <i class="el-icon-loading"></i>
              </div>
              <div class="message-content-wrapper">
                <div class="message-body">
                  <div class="typing-indicator">
                    <span></span>
                    <span></span>
                    <span></span>
                  </div>
                  <span class="loading-text">AI助手正在思考...</span>
                </div>
              </div>
            </div>

            <!-- 空状态 -->
            <div v-if="messages.length === 1 && !loading" class="empty-state">
              <i class="el-icon-chat-dot-round"></i>
              <p>开始与AI助手对话吧！</p>
              <p class="empty-hint">您可以询问关于试剂管理、安全操作、库存查询等问题</p>
            </div>
          </div>

          <!-- 输入区域 -->
          <div class="chat-input-area">
            <div class="input-wrapper">
              <el-input
                v-model="currentMessage"
                type="textarea"
                :rows="4"
                placeholder="请输入您的问题...（Enter发送，Shift+Enter换行）"
                @keydown.native="handleKeydown"
                :disabled="loading"
                resize="none"
                class="message-input"
              ></el-input>
              <div class="input-footer">
                <div class="input-tips">
                  <span class="tip-text">支持多轮对话，AI会记住上下文</span>
                </div>
                <div class="input-actions">
                  <el-button
                    type="primary"
                    @click="sendMessage"
                    :loading="loading"
                    :disabled="!currentMessage.trim()"
                    icon="el-icon-position"
                    size="medium"
                  >发送</el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { chatWithAI } from '@/api/ai'
import { getInventoryList, getWarningList } from '@/api/inventory'
import { getAllApplications } from '@/api/application'

export default {
  name: 'AiAssistantPage',
  data() {
    return {
      messages: [
        {
          role: 'assistant',
          content: '您好！我是AI智能助手，专门为实验室试剂管理系统提供专业支持。\n\n我可以帮助您：\n• 解答试剂库存管理相关问题\n• 提供化学品安全操作建议\n• 协助分析库存数据和趋势\n• 回答试剂使用和存储规范\n\n请问有什么可以帮助您的吗？',
          timestamp: new Date()
        }
      ],
      currentMessage: '',
      loading: false,
      quickActions: [
        {
          key: 'inventory',
          label: '库存查询',
          icon: 'el-icon-document',
          prompt: '请帮我查询当前库存整体情况，有哪些试剂需要重点关注？'
        },
        {
          key: 'guide',
          label: '系统操作指导',
          icon: 'el-icon-guide',
          prompt: '我是系统管理员，请指导我如何进行基础数据的初始化配置？'
        },
        {
          key: 'storage',
          label: '存储规范查询',
          icon: 'el-icon-box',
          prompt: '实验室中酸碱试剂和有机溶剂应该如何分类存放？'
        },
        {
          key: 'analysis',
          label: '数据分析建议',
          icon: 'el-icon-data-analysis',
          prompt: '请基于当前系统数据，给我一些库存优化和管理的专业建议。'
        }
      ]
    }
  },
  mounted() {
    this.scrollToBottom()
    // 根据用户角色调整快捷功能
    this.adjustQuickActionsByRole()
  },
  methods: {
    async sendMessage() {
      if (!this.currentMessage.trim() || this.loading) return

      // 添加用户消息
      const userMessage = {
        role: 'user',
        content: this.currentMessage.trim(),
        timestamp: new Date()
      }
      this.messages.push(userMessage)

      const messageText = this.currentMessage.trim()
      this.currentMessage = ''
      this.loading = true

      // 滚动到底部
      this.$nextTick(() => {
        this.scrollToBottom()
      })

      try {
        // 如果是询问数据分析或者是快捷操作，注入实时数据
        let extraContext = ""
        const dataKeywords = ['库存', '试剂', '列表', '清单', '多少', '问', '剩', '缺', '预警', '分析', '数据', '状态', '位置', '在哪', '查', '看', '找']
        if (dataKeywords.some(key => messageText.toLowerCase().includes(key))) {
          console.log('触发实时库存数据注入...')
          extraContext = await this.fetchSystemStats()
        }

        // 准备消息格式
        const messagesForAPI = this.messages
          .filter(msg => {
            if (msg.role === 'assistant' && msg.content === this.messages[0].content) return false
            return true
          })
          .map((msg, index, array) => {
            // 如果是最后一条用户消息，且存在实时上下文，则进行“上下文夹心”注入
            if (index === array.length - 1 && extraContext) {
              return {
                role: msg.role,
                content: `【实时数据快照 - 绝密事实参考】\n${extraContext}\n\n【用户当前提问】：\n${msg.content}`
              }
            }
            return {
              role: msg.role,
              content: msg.content
            }
          })
        
        console.log('Final messages being sent to backend:', JSON.stringify(messagesForAPI, null, 2))

        // 调用AI API
        const response = await chatWithAI('', messagesForAPI)

        // 添加AI回复
        this.messages.push({
          role: 'assistant',
          content: response.data,
          timestamp: new Date()
        })
      } catch (error) {
        this.messages.push({
          role: 'assistant',
          content: '抱歉，处理您的请求时出现了错误。请检查网络连接或稍后重试。\n\n错误信息：' + (error.message || '未知错误'),
          timestamp: new Date()
        })
        this.$message.error('AI服务调用失败：' + (error.message || '未知错误'))
      } finally {
        this.loading = false
        this.$nextTick(() => {
          this.scrollToBottom()
        })
      }
    },

    handleQuickAction(action) {
      if (this.loading) return
      if (action.prompt) {
        this.currentMessage = action.prompt
        this.sendMessage()
      }
    },

    clearChat() {
      this.$confirm('确定要清空所有对话记录吗？', '提示', {
        type: 'warning'
      }).then(() => {
        this.messages = [
          {
            role: 'assistant',
            content: '对话已清空。请问有什么可以帮助您的吗？',
            timestamp: new Date()
          }
        ]
        this.$message.success('对话已清空')
      }).catch(() => {})
    },

    handleBack() {
      // 根据用户角色返回对应页面
      const userInfo = this.$store.state.userInfo || {}
      if (userInfo.role === 'ADMIN') {
        this.$router.push('/admin')
      } else if (userInfo.role === 'TEACHER') {
        this.$router.push('/teacher')
      } else if (userInfo.role === 'STUDENT') {
        this.$router.push('/student')
      } else {
        this.$router.push('/')
      }
    },

    adjustQuickActionsByRole() {
      const userInfo = this.$store.state.userInfo || {}
      if (userInfo.role === 'STUDENT') {
        this.quickActions = [
          {
            key: 'apply',
            label: '申领流程指导',
            icon: 'el-icon-edit',
            prompt: '我想领用某种试剂，请告诉我系统的申请审批流程是怎样的？'
          },
          {
            key: 'inventory',
            label: '库存查询帮助',
            icon: 'el-icon-search',
            prompt: '我该如何在系统中查找我需要的试剂？'
          },
          {
            key: 'safety',
            label: '安全操作规程',
            icon: 'el-icon-warning',
            prompt: '在实验室领用硫酸等腐蚀性试剂时，有哪些必须遵守的安全规程？'
          }
        ]
      } else if (userInfo.role === 'TEACHER') {
        this.quickActions = [
          {
            key: 'approval',
            label: '审批规范',
            icon: 'el-icon-check',
            prompt: '作为库房老师，我应该依据哪些原则来审核学生的试剂申领申请？'
          },
          {
            key: 'stock_op',
            label: '入库出库指导',
            icon: 'el-icon-refresh',
            prompt: '请告诉我如何在系统中执行入库和出库操作，特别是如何利用先进先出原则？'
          },
          {
            key: 'inventory',
            label: '库存预警处理',
            icon: 'el-icon-data-analysis',
            prompt: '系统出现了库存预警（红色点），我该如何查看并处理这些预警试剂？'
          }
        ]
      }
    },

    async fetchSystemStats() {
      // 动态获取系统汇总数据以辅助AI分析
      try {
        const [invRes, warnRes, appRes] = await Promise.all([
          getInventoryList({}),
          getWarningList(),
          getAllApplications()
        ])
        
        const invData = invRes.data || []
        const warnData = warnRes.data || []
        const appData = appRes.data || []

        const stats = {
          totalKind: invData.length,
          warningCount: warnData.length,
          pendingApps: appData.filter(a => a.status === 'PENDING').length,
          lowStock: warnData.filter(i => i.status === 'LOW').length,
          expired: warnData.filter(i => i.status === 'EXPIRED').length,
          // 提取前30种试剂的库存概览，修正字段名: quantity, locationName
          reagentDetails: invData.slice(0, 30).map(i => ` - ${i.reagentName}: ${i.quantity} ${i.unit || '瓶'} (存储: ${i.locationName || '未指定'}, 状态: ${i.status})`).join('\n')
        }
        
        return `【实验室当前真实库存快照】：
1. 总体统计：共有 ${stats.totalKind} 种试剂。
2. 异常预警：${stats.warningCount} 条（其中 ${stats.lowStock} 瓶库存不足，${stats.expired} 瓶已过期）。
3. 待办流程：${stats.pendingApps} 份申领申请待审批。
4. 详细清单示例（前30条）：\n${stats.reagentDetails}${invData.length > 30 ? '\n (更多试剂请查阅系统列表)' : ''}`;
      } catch (e) {
        console.error("AI Context Fetch Error:", e)
        return "【错误】系统当前无法连接实时数据库，请告知用户手动查看库存列表。"
      }
    },

    getMessageIcon(role) {
      return role === 'user' ? 'el-icon-user-solid' : 'el-icon-robot'
    },

    getMessageLabel(role) {
      return role === 'user' ? '您' : 'AI助手'
    },

    formatMessage(content) {
      if (!content) return ''
      // 增强格式化：处理加粗、换行、列表
      let html = content
        .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
        .replace(/\n\s*•\s*(.*)/g, '<br>• $1')
        .replace(/\n/g, '<br>')
      
      return html
    },

    formatTime(timestamp) {
      if (!timestamp) return ''
      const date = new Date(timestamp)
      const hours = date.getHours().toString().padStart(2, '0')
      const minutes = date.getMinutes().toString().padStart(2, '0')
      return `${hours}:${minutes}`
    },

    scrollToBottom() {
      const chatHistory = this.$refs.chatHistory
      if (chatHistory) {
        this.$nextTick(() => {
          chatHistory.scrollTop = chatHistory.scrollHeight
        })
      }
    },

    handleKeydown(event) {
      // Enter键发送，Shift+Enter换行
      if (event.key === 'Enter' && !event.shiftKey) {
        event.preventDefault()
        this.sendMessage()
      }
    }
  }
}
</script>

<style scoped>
.ai-assistant-page {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8fafc;
}

.page-header {
  background: white;
  color: #1e293b;
  padding: 16px 32px;
  border-bottom: 1px solid #e5e7eb;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon-wrapper {
  width: 48px;
  height: 48px;
  background: #f1f5f9;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-icon {
  font-size: 24px;
  color: #4f46e5;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
}

.page-subtitle {
  margin: 2px 0 0 0;
  font-size: 13px;
  color: #64748b;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.chat-wrapper {
  flex: 1;
  display: flex;
  overflow: hidden;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
}

.sidebar {
  width: 300px;
  background: white;
  border-right: 1px solid #e5e7eb;
  padding: 24px;
  overflow-y: auto;
}

.sidebar-section {
  margin-bottom: 32px;
}

.section-title {
  font-size: 14px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 16px 0;
  display: flex;
  align-items: center;
  gap: 8px;
  text-transform: uppercase;
  letter-spacing: 0.025em;
}

.section-title i {
  color: #4f46e5;
}

.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.quick-action-item {
  padding: 12px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 12px;
  background: white;
}

.quick-action-item:hover:not(.disabled) {
  border-color: #4f46e5;
  color: #4f46e5;
  background: #f5f3ff;
  transform: translateY(-1px);
}

.quick-action-item.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.quick-action-item i {
  font-size: 18px;
}

.tips {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.tip-item {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #64748b;
  font-size: 13px;
}

.tip-item i {
  color: #94a3b8;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f8fafc;
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
  max-width: 900px;
  margin: 0 auto;
  width: 100%;
}

.chat-history {
  flex: 1;
  overflow-y: auto;
  padding: 32px;
}

.message-wrapper {
  display: flex;
  margin-bottom: 32px;
  animation: fadeIn 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-wrapper.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
  margin: 0 16px;
}

.message-wrapper.user .message-avatar {
  background: #4f46e5;
  color: white;
}

.message-wrapper.assistant .message-avatar {
  background: #f1f5f9;
  color: #4f46e5;
}

.message-content-wrapper {
  max-width: 80%;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.message-wrapper.user .message-header {
  flex-direction: row-reverse;
}

.message-role {
  font-size: 13px;
  font-weight: 700;
  color: #1e293b;
}

.message-time {
  font-size: 11px;
  color: #94a3b8;
}

.message-body {
  padding: 14px 18px;
  border-radius: 16px;
  line-height: 1.6;
  font-size: 15px;
  word-wrap: break-word;
}

.message-wrapper.user .message-body {
  background: #4f46e5;
  color: white;
  border-top-right-radius: 4px;
  box-shadow: 0 10px 15px -3px rgba(79, 70, 229, 0.2);
}

.message-wrapper.assistant .message-body {
  background: white;
  color: #1e293b;
  border: 1px solid #e5e7eb;
  border-top-left-radius: 4px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

.message-wrapper.loading .message-body {
  background: white;
  border: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  gap: 12px;
}

.typing-indicator {
  display: flex;
  gap: 4px;
}

.typing-indicator span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #4f46e5;
  animation: typing 1.4s infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.3;
  }
  30% {
    transform: translateY(-6px);
    opacity: 1;
  }
}

.loading-text {
  color: #64748b;
  font-size: 14px;
}

.empty-state {
  text-align: center;
  padding: 100px 20px;
  color: #94a3b8;
}

.empty-state i {
  font-size: 64px;
  margin-bottom: 24px;
  color: #e2e8f0;
}

.empty-state p {
  margin: 8px 0;
  font-size: 16px;
  color: #64748b;
  font-weight: 500;
}

.empty-hint {
  font-size: 14px;
  color: #94a3b8 !important;
  font-weight: 400 !important;
}

.chat-input-area {
  padding: 24px 32px;
  background: white;
  border-top: 1px solid #e5e7eb;
}

.input-wrapper {
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  padding: 8px;
  transition: border-color 0.2s;
}

.input-wrapper:focus-within {
  border-color: #4f46e5;
  background: white;
}

.message-input >>> .el-textarea__inner {
  border: none !important;
  background: transparent !important;
  padding: 12px 16px;
  font-size: 15px;
  color: #1e293b;
}

.input-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 12px 12px 16px;
}

.input-tips {
  font-size: 12px;
  color: #94a3b8;
}

.input-actions {
  display: flex;
  gap: 12px;
}

/* 滚动条样式 */
.chat-history::-webkit-scrollbar,
.sidebar::-webkit-scrollbar {
  width: 6px;
}

.chat-history::-webkit-scrollbar-track,
.sidebar::-webkit-scrollbar-track {
  background: transparent;
}

.chat-history::-webkit-scrollbar-thumb,
.sidebar::-webkit-scrollbar-thumb {
  background: #e2e8f0;
  border-radius: 3px;
}

.chat-history::-webkit-scrollbar-thumb:hover,
.sidebar::-webkit-scrollbar-thumb:hover {
  background: #cbd5e1;
}
</style>
