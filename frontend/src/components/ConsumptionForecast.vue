<template>
  <el-card>
    <div slot="header">
      <span><i class="el-icon-data-analysis"></i> AI智能消耗预测</span>
    </div>
    <el-alert 
      title="该功能使用AI技术分析历史消耗数据，智能预测试剂消耗趋势，帮助实验室提前做好采购计划" 
      type="info" 
      style="margin-bottom: 20px;" 
      :closable="false"
    ></el-alert>
    
    <el-form :inline="true" style="margin-bottom: 20px;">
      <el-form-item label="选择试剂">
        <el-select 
          v-model="selectedReagent" 
          placeholder="请选择试剂" 
          style="width: 250px;"
          filterable
          :loading="reagentLoading"
        >
          <el-option 
            v-for="reagent in reagentList" 
            :key="reagent.id" 
            :label="reagent.name" 
            :value="reagent.id"
          >
            <span>{{ reagent.name }}</span>
            <span style="color: #8492a6; font-size: 12px; margin-left: 10px;">{{ reagent.specification || '' }}</span>
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button 
          type="primary" 
          @click="predict" 
          :loading="predicting"
          icon="el-icon-magic-stick"
        >AI智能预测</el-button>
        <el-button @click="clearResult" v-if="forecastResult">清空结果</el-button>
      </el-form-item>
    </el-form>
    
    <!-- 预测结果 -->
    <div v-if="forecastResult && !predicting">
      <el-divider content-position="left">AI预测结果</el-divider>
      
      <!-- 核心预测指标 -->
      <el-row :gutter="20" style="margin-bottom: 20px;">
        <el-col :span="6">
          <el-card shadow="hover" style="text-align: center;">
            <div style="font-size: 24px; color: #409EFF; font-weight: bold;">
              {{ forecastResult.daysUntilDepletion || '-' }}
            </div>
            <div style="color: #909399; margin-top: 5px;">预计耗尽天数</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" style="text-align: center;">
            <div style="font-size: 24px; color: #67C23A; font-weight: bold;">
              {{ forecastResult.predictedDepletionDate || '-' }}
            </div>
            <div style="color: #909399; margin-top: 5px;">预计耗尽日期</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" style="text-align: center;">
            <div style="font-size: 24px; color: #E6A23C; font-weight: bold;">
              {{ forecastResult.confidenceLevel ? forecastResult.confidenceLevel + '%' : '-' }}
            </div>
            <div style="color: #909399; margin-top: 5px;">预测置信度</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" style="text-align: center;">
            <div style="font-size: 24px; color: #F56C6C; font-weight: bold;">
              {{ forecastResult.recommendedOrderQuantity || '-' }}
            </div>
            <div style="color: #909399; margin-top: 5px;">建议订购量</div>
          </el-card>
        </el-col>
      </el-row>
      
      <!-- 详细信息 -->
      <el-descriptions :column="2" border style="margin-bottom: 20px;">
        <el-descriptions-item label="试剂名称">{{ forecastResult.reagentName }}</el-descriptions-item>
        <el-descriptions-item label="当前库存">{{ forecastResult.currentStock }}</el-descriptions-item>
        <el-descriptions-item label="日均消耗量">{{ forecastResult.averageDailyConsumption }}</el-descriptions-item>
        <el-descriptions-item label="供应商到货周期">{{ forecastResult.supplierLeadTime }}天</el-descriptions-item>
        <el-descriptions-item label="建议下单日期" :span="2">
          <span :style="{color: isUrgent ? '#F56C6C' : '#67C23A', fontWeight: 'bold'}">
            {{ forecastResult.recommendedOrderDate || '-' }}
          </span>
          <el-tag v-if="isUrgent" type="danger" size="mini" style="margin-left: 10px;">紧急</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="预测模型" :span="2">
          <el-tag :type="forecastResult.predictionModel === 'AI_MODEL' ? 'success' : 'info'">
            {{ forecastResult.predictionModel === 'AI_MODEL' ? 'AI智能模型' : '线性趋势' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
      
      <!-- AI分析结果 -->
      <div v-if="aiAnalysis">
        <el-card v-if="aiAnalysis.trendAnalysis" style="margin-bottom: 15px;">
          <div slot="header">
            <i class="el-icon-trends"></i> 消耗趋势分析
          </div>
          <div style="line-height: 1.8; color: #606266;">{{ aiAnalysis.trendAnalysis }}</div>
        </el-card>
        
        <el-card v-if="aiAnalysis.riskFactors && aiAnalysis.riskFactors.length > 0" style="margin-bottom: 15px;">
          <div slot="header">
            <i class="el-icon-warning"></i> 风险因素
          </div>
          <ul style="margin: 0; padding-left: 20px;">
            <li v-for="(risk, index) in aiAnalysis.riskFactors" :key="index" style="margin-bottom: 8px; color: #E6A23C;">
              {{ risk }}
            </li>
          </ul>
        </el-card>
        
        <el-card v-if="aiAnalysis.suggestions && aiAnalysis.suggestions.length > 0">
          <div slot="header">
            <i class="el-icon-light-rain"></i> AI采购建议
          </div>
          <ul style="margin: 0; padding-left: 20px;">
            <li v-for="(suggestion, index) in aiAnalysis.suggestions" :key="index" style="margin-bottom: 8px; color: #606266;">
              {{ suggestion }}
            </li>
          </ul>
        </el-card>
      </div>
    </div>
    
    <!-- 加载状态 -->
    <div v-if="predicting" style="text-align: center; padding: 40px;">
      <i class="el-icon-loading" style="font-size: 32px; color: #409EFF;"></i>
      <p style="margin-top: 15px; color: #909399;">AI正在分析历史数据，生成智能预测...</p>
    </div>
  </el-card>
</template>

<script>
import { generateForecast } from '@/api/forecasting'
import { getReagentList } from '@/api/reagent'

export default {
  name: 'ConsumptionForecast',
  data() {
    return {
      selectedReagent: '',
      reagentList: [],
      reagentLoading: false,
      predicting: false,
      forecastResult: null,
      aiAnalysis: null
    }
  },
  computed: {
    isUrgent() {
      if (!this.forecastResult || !this.forecastResult.recommendedOrderDate) {
        return false
      }
      const orderDate = new Date(this.forecastResult.recommendedOrderDate)
      const today = new Date()
      const daysDiff = Math.ceil((orderDate - today) / (1000 * 60 * 60 * 24))
      return daysDiff <= 7
    }
  },
  mounted() {
    this.loadReagents()
  },
  methods: {
    async loadReagents() {
      this.reagentLoading = true
      try {
        const res = await getReagentList()
        this.reagentList = res.data || []
      } catch (error) {
        this.$message.error('加载试剂列表失败：' + (error.message || '未知错误'))
      } finally {
        this.reagentLoading = false
      }
    },
    
    async predict() {
      if (!this.selectedReagent) {
        this.$message.warning('请选择试剂')
        return
      }
      
      this.predicting = true
      this.forecastResult = null
      this.aiAnalysis = null
      
      try {
        const res = await generateForecast(this.selectedReagent)
        if (res.code === 200) {
          this.forecastResult = res.data
          
          // 解析AI分析结果
          if (this.forecastResult.predictionBasis) {
            try {
              const basis = JSON.parse(this.forecastResult.predictionBasis)
              if (basis.aiAnalysis) {
                this.aiAnalysis = basis.aiAnalysis
              }
            } catch (e) {
              console.warn('解析AI分析结果失败', e)
            }
          }
          
          this.$message.success('AI预测完成！')
        } else {
          this.$message.error(res.message || '预测失败')
        }
      } catch (error) {
        this.$message.error('预测失败：' + (error.message || '未知错误'))
      } finally {
        this.predicting = false
      }
    },
    
    clearResult() {
      this.forecastResult = null
      this.aiAnalysis = null
    }
  }
}
</script>

<style scoped>
.el-card {
  margin-bottom: 10px;
}
</style>




