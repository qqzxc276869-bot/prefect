<template>
  <div class="ai-assistant-page">
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <i class="el-icon-chat-dot-round header-icon"></i>
          <div>
            <h1 class="page-title">AI智能助手</h1>
            <p class="page-subtitle">专业的试剂管理智能助手，为您提供全方位的帮助</p>
          </div>
        </div>
        <div class="header-actions">
          <el-button @click="handleBack" icon="el-icon-back">返回</el-button>
          <el-button type="primary" @click="clearChat" icon="el-icon-delete" :disabled="messages.length <= 1">清空对话</el-button>
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
          prompt: '请帮我查询当前库存情况，有哪些试剂需要补充？'
        },
        {
          key: 'safety',
          label: '安全操作',
          icon: 'el-icon-warning',
          prompt: '请告诉我处理易燃易爆试剂时需要注意哪些安全事项？'
        },
        {
          key: 'storage',
          label: '存储规范',
          icon: 'el-icon-box',
          prompt: '不同类别的试剂应该如何正确存储？'
        },
        {
          key: 'analysis',
          label: '数据分析',
          icon: 'el-icon-data-analysis',
          prompt: '请分析一下最近的试剂使用趋势，有哪些需要注意的地方？'
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
        // 准备消息格式（不包含timestamp）
        const messagesForAPI = this.messages
          .filter(msg => msg.role !== 'assistant' || msg.content !== this.messages[0].content)
          .map(msg => ({
            role: msg.role,
            content: msg.content
          }))

        // 调用AI API
        const response = await chatWithAI('qwen-plus-2025-07-28', messagesForAPI)

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
            label: '申领辅助',
            icon: 'el-icon-edit',
            prompt: '请帮我检查我的试剂申领理由是否充分？'
          },
          {
            key: 'inventory',
            label: '库存查询',
            icon: 'el-icon-document',
            prompt: '请帮我查询可用的试剂库存'
          },
          {
            key: 'safety',
            label: '安全提示',
            icon: 'el-icon-warning',
            prompt: '提醒我在处理易燃试剂时需要注意的事项'
          }
        ]
      } else if (userInfo.role === 'TEACHER') {
        this.quickActions = [
          {
            key: 'approval',
            label: '审批建议',
            icon: 'el-icon-check',
            prompt: '请给出处理试剂申领审批的建议'
          },
          {
            key: 'inventory',
            label: '库存分析',
            icon: 'el-icon-data-analysis',
            prompt: '请帮我分析当前库存是否存在风险点'
          },
          {
            key: 'safety',
            label: '安全巡检',
            icon: 'el-icon-view',
            prompt: '提醒我进行本周的实验室安全巡检要点'
          }
        ]
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
      // 简单的格式化：将换行转换为<br>
      return content.replace(/\n/g, '<br>')
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
      // Shift+Enter 允许默认行为（换行）
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
  background: #f5f7fa;
}

.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px 30px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-icon {
  font-size: 40px;
  opacity: 0.9;
}

.page-title {
  margin: 0;
  font-size: 28px;
  font-weight: 600;
}

.page-subtitle {
  margin: 5px 0 0 0;
  font-size: 14px;
  opacity: 0.9;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.chat-wrapper {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.sidebar {
  width: 280px;
  background: white;
  border-right: 1px solid #e4e7ed;
  padding: 20px;
  overflow-y: auto;
}

.sidebar-section {
  margin-bottom: 30px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 15px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-title i {
  color: #409EFF;
}

.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.quick-action-item {
  padding: 12px 15px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 10px;
}

.quick-action-item:hover:not(.disabled) {
  background: #f0f9ff;
  border-color: #409EFF;
  color: #409EFF;
  transform: translateX(5px);
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
  color: #606266;
  font-size: 14px;
}

.tip-item i {
  color: #909399;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: white;
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.chat-history {
  flex: 1;
  overflow-y: auto;
  padding: 30px;
  background: #fafbfc;
}

.message-wrapper {
  display: flex;
  margin-bottom: 25px;
  animation: fadeIn 0.3s ease-in;
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
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
  margin: 0 12px;
}

.message-wrapper.user .message-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.message-wrapper.assistant .message-avatar {
  background: #e6f7ff;
  color: #409EFF;
}

.message-content-wrapper {
  max-width: 70%;
  min-width: 200px;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.message-role {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.message-time {
  font-size: 12px;
  color: #909399;
}

.message-body {
  padding: 15px 20px;
  border-radius: 12px;
  line-height: 1.6;
  word-wrap: break-word;
  white-space: pre-wrap;
}

.message-wrapper.user .message-body {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-bottom-right-radius: 4px;
}

.message-wrapper.assistant .message-body {
  background: white;
  color: #303133;
  border: 1px solid #e4e7ed;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  border-bottom-left-radius: 4px;
}

.message-wrapper.loading .message-body {
  background: white;
  border: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  gap: 10px;
}

.typing-indicator {
  display: flex;
  gap: 4px;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #409EFF;
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
    opacity: 0.7;
  }
  30% {
    transform: translateY(-10px);
    opacity: 1;
  }
}

.loading-text {
  color: #909399;
  font-size: 14px;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: #909399;
}

.empty-state i {
  font-size: 64px;
  margin-bottom: 20px;
  opacity: 0.5;
}

.empty-state p {
  margin: 10px 0;
  font-size: 16px;
}

.empty-hint {
  font-size: 14px;
  color: #c0c4cc;
}

.chat-input-area {
  border-top: 1px solid #e4e7ed;
  padding: 20px 30px;
  background: white;
}

.input-wrapper {
  max-width: 100%;
}

.message-input {
  margin-bottom: 12px;
}

.input-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.input-tips {
  font-size: 12px;
  color: #909399;
}

.input-actions {
  display: flex;
  gap: 10px;
}

/* 滚动条样式 */
.chat-history::-webkit-scrollbar,
.sidebar::-webkit-scrollbar {
  width: 6px;
}

.chat-history::-webkit-scrollbar-track,
.sidebar::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.chat-history::-webkit-scrollbar-thumb,
.sidebar::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.chat-history::-webkit-scrollbar-thumb:hover,
.sidebar::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>


