<template>
  <div class="ai-demo">
    <h2>AI智能助手演示</h2>
    
    <!-- AI助手组件 -->
    <ai-assistant 
      :show-close="false"
      :quick-actions="quickActions"
      @close="handleClose"
    />
    
    <!-- 功能演示区域 -->
    <el-card class="demo-card" style="margin-top: 20px;">
      <div slot="header">
        <span>快捷功能演示</span>
      </div>
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card>
            <div slot="header">
              <span>库存管理建议</span>
            </div>
            <el-form :model="inventoryForm" size="small">
              <el-form-item label="试剂名称">
                <el-input v-model="inventoryForm.reagentName" placeholder="例如：盐酸"></el-input>
              </el-form-item>
              <el-form-item label="当前库存">
                <el-input-number v-model="inventoryForm.currentStock" :min="0"></el-input-number>
              </el-form-item>
              <el-form-item label="单位">
                <el-select v-model="inventoryForm.unit">
                  <el-option label="瓶" value="瓶"></el-option>
                  <el-option label="升" value="升"></el-option>
                  <el-option label="毫升" value="毫升"></el-option>
                  <el-option label="克" value="克"></el-option>
                </el-select>
              </el-form-item>
              <el-button 
                type="primary" 
                size="small" 
                @click="getInventoryAdvice"
                :loading="loading"
                icon="el-icon-lightning"
              >获取建议</el-button>
            </el-form>
          </el-card>
        </el-col>
        
        <el-col :span="8">
          <el-card>
            <div slot="header">
              <span>安全处理建议</span>
            </div>
            <el-form :model="safetyForm" size="small">
              <el-form-item label="化学品名称">
                <el-input v-model="safetyForm.chemicalName" placeholder="例如：硫酸"></el-input>
              </el-form-item>
              <el-form-item label="处理场景">
                <el-input 
                  v-model="safetyForm.scenario" 
                  type="textarea"
                  :rows="3"
                  placeholder="例如：需要稀释浓硫酸用于实验"
                ></el-input>
              </el-form-item>
              <el-button 
                type="warning" 
                size="small" 
                @click="getSafetyAdvice"
                :loading="loading"
                icon="el-icon-warning"
              >获取安全建议</el-button>
            </el-form>
          </el-card>
        </el-col>
        
        <el-col :span="8">
          <el-card>
            <div slot="header">
              <span>自定义对话</span>
            </div>
            <el-input
              v-model="customQuestion"
              type="textarea"
              :rows="5"
              placeholder="输入您的问题..."
            ></el-input>
            <el-button 
              type="success" 
              size="small" 
              style="margin-top: 10px;"
              @click="sendCustomQuestion"
              :loading="loading"
              icon="el-icon-chat-dot-round"
            >发送问题</el-button>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
    
    <!-- 结果显示 -->
    <el-dialog
      :title="resultTitle"
      :visible.sync="resultDialogVisible"
      width="60%"
      top="5vh"
    >
      <div class="result-content">
        <el-input
          v-model="resultContent"
          type="textarea"
          :rows="15"
          readonly
          class="result-textarea"
        ></el-input>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="resultDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="copyResult">复制内容</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import AiAssistant from '@/components/AiAssistant.vue'
import { generateInventoryAdvice, generateSafetyAdvice, chatWithAI } from '@/api/ai'

export default {
  name: 'AIDemo',
  components: {
    AiAssistant
  },
  data() {
    return {
      loading: false,
      inventoryForm: {
        reagentName: '盐酸',
        currentStock: 5,
        unit: '瓶'
      },
      safetyForm: {
        chemicalName: '硫酸',
        scenario: '需要稀释浓硫酸用于实验'
      },
      customQuestion: '',
      resultDialogVisible: false,
      resultTitle: '',
      resultContent: '',
      quickActions: [
        {
          key: 'inventory',
          label: '库存建议',
          handler: () => {
            this.inventoryForm.reagentName = '乙醇'
            this.inventoryForm.currentStock = 3
            this.inventoryForm.unit = '瓶'
            this.getInventoryAdvice()
          }
        },
        {
          key: 'safety',
          label: '安全建议',
          handler: () => {
            this.safetyForm.chemicalName = '氢氧化钠'
            this.safetyForm.scenario = '固体氢氧化钠的称量和溶解操作'
            this.getSafetyAdvice()
          }
        },
        {
          key: 'general',
          label: '通用对话',
          prompt: '实验室化学试剂管理的基本原则是什么？'
        }
      ]
    }
  },
  methods: {
    async getInventoryAdvice() {
      if (!this.inventoryForm.reagentName || !this.inventoryForm.unit) {
        this.$message.warning('请填写完整信息')
        return
      }
      
      this.loading = true
      try {
        const response = await generateInventoryAdvice(
          this.inventoryForm.reagentName,
          this.inventoryForm.currentStock,
          this.inventoryForm.unit
        )
        this.showResult('库存管理建议', response.data)
      } catch (error) {
        this.$message.error('获取建议失败：' + error.message)
      } finally {
        this.loading = false
      }
    },
    
    async getSafetyAdvice() {
      if (!this.safetyForm.chemicalName || !this.safetyForm.scenario) {
        this.$message.warning('请填写完整信息')
        return
      }
      
      this.loading = true
      try {
        const response = await generateSafetyAdvice(
          this.safetyForm.chemicalName,
          this.safetyForm.scenario
        )
        this.showResult('安全处理建议', response.data)
      } catch (error) {
        this.$message.error('获取建议失败：' + error.message)
      } finally {
        this.loading = false
      }
    },
    
    async sendCustomQuestion() {
      if (!this.customQuestion.trim()) {
        this.$message.warning('请输入问题')
        return
      }
      
      this.loading = true
      try {
        const messages = [
          { role: 'user', content: this.customQuestion.trim() }
        ]
        const response = await chatWithAI(null, messages)
        this.showResult('AI回复', response.data)
      } catch (error) {
        this.$message.error('发送问题失败：' + error.message)
      } finally {
        this.loading = false
      }
    },
    
    showResult(title, content) {
      this.resultTitle = title
      this.resultContent = content
      this.resultDialogVisible = true
    },
    
    copyResult() {
      const textarea = document.createElement('textarea')
      textarea.value = this.resultContent
      document.body.appendChild(textarea)
      textarea.select()
      document.execCommand('copy')
      document.body.removeChild(textarea)
      this.$message.success('内容已复制到剪贴板')
    },
    
    handleClose() {
      console.log('AI助手已关闭')
    }
  }
}
</script>

<style scoped>
.ai-demo {
  padding: 20px;
}

.demo-card {
  margin-top: 20px;
}

.result-content {
  max-height: 400px;
  overflow-y: auto;
}

.result-textarea {
  font-family: 'Microsoft YaHei', Arial, sans-serif;
  line-height: 1.6;
}
</style>