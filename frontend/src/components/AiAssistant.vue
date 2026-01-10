<template>
  <div class="ai-assistant">
    <el-card class="chat-card">
      <div slot="header" class="card-header">
        <span><i class="el-icon-chat-dot-round"></i> AI智能助手</span>
        <el-button
          v-if="showClose"
          style="float: right; padding: 3px 0"
          type="text"
          icon="el-icon-close"
          @click="$emit('close')"
        ></el-button>
      </div>
      
      <div class="chat-container">
        <!-- 对话历史 -->
        <div class="chat-history" ref="chatHistory">
          <div
            v-for="(message, index) in messages"
            :key="index"
            :class="['message', message.role]"
          >
            <div class="message-content">
              <div class="message-header">
                <i :class="getMessageIcon(message.role)"></i>
                <span>{{ getMessageLabel(message.role) }}</span>
              </div>
              <div class="message-body">{{ message.content }}</div>
            </div>
          </div>
          
          <!-- 加载状态 -->
          <div v-if="loading" class="message assistant">
            <div class="message-content">
              <div class="message-header">
                <i class="el-icon-loading"></i>
                <span>AI助手正在思考...</span>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 输入区域 -->
        <div class="chat-input">
          <el-input
            v-model="currentMessage"
            type="textarea"
            :rows="3"
            placeholder="请输入您的问题...（Enter发送，Shift+Enter换行）"
            @keydown.native.enter.exact.prevent="sendMessage"
            @keydown.native.enter.shift.exact="handleShiftEnter"
            :disabled="loading"
          ></el-input>
          <div class="input-actions">
            <el-button
              type="primary"
              size="small"
              @click="sendMessage"
              :loading="loading"
              icon="el-icon-position"
            >发送</el-button>
            <el-button
              size="small"
              @click="clearChat"
              icon="el-icon-delete"
            >清空</el-button>
          </div>
        </div>
        
        <!-- 快捷功能 -->
        <div class="quick-actions" v-if="quickActions && quickActions.length > 0">
          <div class="quick-title">快捷功能：</div>
          <el-button
            v-for="action in quickActions"
            :key="action.key"
            size="mini"
            @click="handleQuickAction(action)"
            :disabled="loading"
          >{{ action.label }}</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { chatWithAI } from '@/api/ai'

export default {
  name: 'AiAssistant',
  props: {
    showClose: {
      type: Boolean,
      default: false
    },
    quickActions: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      messages: [
        {
          role: 'assistant',
          content: '您好！我是AI智能助手，可以为您提供试剂库存管理和化学品安全方面的专业建议。请问有什么可以帮助您的吗？'
        }
      ],
      currentMessage: '',
      loading: false
    }
  },
  methods: {
    async sendMessage() {
      if (!this.currentMessage.trim() || this.loading) return
      
      // 添加用户消息
      this.messages.push({
        role: 'user',
        content: this.currentMessage.trim()
      })
      
      const userMessage = this.currentMessage.trim()
      this.currentMessage = ''
      this.loading = true
      
      try {
        // 准备消息格式
        const messages = this.messages.map(msg => ({
          role: msg.role,
          content: msg.content
        }))
        
        // 调用AI API
        const response = await chatWithAI('qwen-plus-2025-07-28', messages)
        
        // 添加AI回复
        this.messages.push({
          role: 'assistant',
          content: response.data
        })
        
      } catch (error) {
        this.messages.push({
          role: 'assistant',
          content: '抱歉，处理您的请求时出现了错误，请稍后重试。'
        })
        this.$message.error('AI服务调用失败：' + error.message)
      } finally {
        this.loading = false
        this.$nextTick(() => {
          this.scrollToBottom()
        })
      }
    },
    
    handleQuickAction(action) {
      if (action.handler) {
        action.handler()
      } else if (action.prompt) {
        this.currentMessage = action.prompt
        this.sendMessage()
      }
    },
    
    clearChat() {
      this.messages = [
        {
          role: 'assistant',
          content: '您好！我是AI智能助手，可以为您提供试剂库存管理和化学品安全方面的专业建议。请问有什么可以帮助您的吗？'
        }
      ]
    },
    
    getMessageIcon(role) {
      return role === 'user' ? 'el-icon-user' : 'el-icon-robot'
    },
    
    getMessageLabel(role) {
      return role === 'user' ? '您' : 'AI助手'
    },
    
    scrollToBottom() {
      const chatHistory = this.$refs.chatHistory
      if (chatHistory) {
        chatHistory.scrollTop = chatHistory.scrollHeight
      }
    },
    
    handleShiftEnter() {
      // Shift+Enter 换行，不做任何处理，让默认行为生效
    }
  }
}
</script>

<style scoped>
.ai-assistant {
  width: 100%;
}

.chat-card {
  height: 600px;
  display: flex;
  flex-direction: column;
}

.card-header {
  font-weight: bold;
  color: #409EFF;
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: calc(100% - 60px);
}

.chat-history {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 10px;
}

.message {
  margin-bottom: 15px;
  display: flex;
}

.message.user {
  justify-content: flex-end;
}

.message.assistant {
  justify-content: flex-start;
}

.message-content {
  max-width: 70%;
  padding: 10px 15px;
  border-radius: 8px;
  position: relative;
}

.message.user .message-content {
  background-color: #409EFF;
  color: white;
}

.message.assistant .message-content {
  background-color: white;
  color: #333;
  border: 1px solid #e4e7ed;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.message-header {
  display: flex;
  align-items: center;
  margin-bottom: 5px;
  font-size: 12px;
  color: #909399;
}

.message.user .message-header {
  color: rgba(255,255,255,0.8);
}

.message-header i {
  margin-right: 5px;
}

.message-body {
  line-height: 1.5;
  word-wrap: break-word;
}

.chat-input {
  margin-bottom: 10px;
}

.input-actions {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.quick-actions {
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.quick-title {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.quick-actions .el-button {
  margin-right: 8px;
  margin-bottom: 5px;
}
</style>