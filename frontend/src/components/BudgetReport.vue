<template>
  <div>
    <el-card>
      <div slot="header">
        <span>预算报表</span>
        <el-button type="success" size="small" style="float: right;" @click="exportReport">
          <i class="el-icon-download"></i> 导出报表
        </el-button>
      </div>

      <!-- 筛选条件 -->
      <el-form :inline="true" style="margin-bottom: 20px;">
        <el-form-item label="课题组">
          <el-select v-model="selectedGroup" placeholder="选择课题组" clearable style="width: 200px;" @change="loadData">
            <el-option v-for="group in groupList" :key="group.id" :label="group.groupName" :value="group.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="年份">
          <el-select v-model="selectedYear" placeholder="选择年份" style="width: 120px;" @change="loadData">
            <el-option label="2024" value="2024"></el-option>
            <el-option label="2023" value="2023"></el-option>
            <el-option label="2022" value="2022"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
        </el-form-item>
      </el-form>

      <!-- 预算概览卡片 -->
      <el-row :gutter="20" style="margin-bottom: 20px;">
        <el-col :span="6">
          <el-card shadow="hover">
            <div style="text-align: center;">
              <div style="color: #909399; font-size: 14px;">总预算</div>
              <div style="color: #303133; font-size: 28px; font-weight: bold; margin: 10px 0;">
                {{ summary.totalBudget | formatMoney }}
              </div>
              <div style="color: #909399; font-size: 12px;">Total Budget</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <div style="text-align: center;">
              <div style="color: #909399; font-size: 14px;">已使用</div>
              <div style="color: #E6A23C; font-size: 28px; font-weight: bold; margin: 10px 0;">
                {{ summary.usedBudget | formatMoney }}
              </div>
              <div style="color: #909399; font-size: 12px;">Used Budget</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <div style="text-align: center;">
              <div style="color: #909399; font-size: 14px;">可用预算</div>
              <div style="color: #67C23A; font-size: 28px; font-weight: bold; margin: 10px 0;">
                {{ summary.availableBudget | formatMoney }}
              </div>
              <div style="color: #909399; font-size: 12px;">Available Budget</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <div style="text-align: center;">
              <div style="color: #909399; font-size: 14px;">使用率</div>
              <div style="color: #F56C6C; font-size: 28px; font-weight: bold; margin: 10px 0;">
                {{ summary.usageRate }}%
              </div>
              <div style="color: #909399; font-size: 12px;">Usage Rate</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 图表区域 -->
      <el-row :gutter="20" style="margin-bottom: 20px;">
        <el-col :span="12">
          <el-card>
            <div slot="header">预算使用趋势</div>
            <div id="budgetTrendChart" style="height: 300px;"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <div slot="header">支出分类占比</div>
            <div id="expenseCategoryChart" style="height: 300px;"></div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 课题组预算明细表 -->
      <el-card style="margin-bottom: 20px;">
        <div slot="header">课题组预算明细</div>
        <el-table :data="groupBudgetList" border>
          <el-table-column prop="groupName" label="课题组名称" width="200"></el-table-column>
          <el-table-column prop="piName" label="负责人" width="100"></el-table-column>
          <el-table-column prop="totalBudget" label="总预算(元)" width="120">
            <template slot-scope="scope">{{ scope.row.totalBudget | formatMoney }}</template>
          </el-table-column>
          <el-table-column prop="usedBudget" label="已使用(元)" width="120">
            <template slot-scope="scope">{{ scope.row.usedBudget | formatMoney }}</template>
          </el-table-column>
          <el-table-column prop="availableBudget" label="可用(元)" width="120">
            <template slot-scope="scope">{{ scope.row.availableBudget | formatMoney }}</template>
          </el-table-column>
          <el-table-column label="使用率" width="150">
            <template slot-scope="scope">
              <el-progress 
                :percentage="parseFloat(scope.row.usageRate)" 
                :color="getProgressColor(scope.row.usageRate)">
              </el-progress>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template slot-scope="scope">
              <el-tag :type="getBudgetStatus(scope.row.usageRate).type" size="small">
                {{ getBudgetStatus(scope.row.usageRate).text }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template slot-scope="scope">
              <el-button size="mini" type="text" @click="viewGroupDetail(scope.row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 交易记录 -->
      <el-card>
        <div slot="header">
          <span>预算交易记录</span>
          <span style="float: right; color: #909399; font-size: 12px;">
            共 {{ transactionList.length }} 条记录
          </span>
        </div>
        <el-table :data="transactionList" border max-height="400">
          <el-table-column prop="id" label="ID" width="60"></el-table-column>
          <el-table-column prop="groupName" label="课题组" width="150"></el-table-column>
          <el-table-column prop="transactionType" label="类型" width="100">
            <template slot-scope="scope">
              <el-tag :type="scope.row.transactionType === 'ALLOCATION' ? 'success' : 'warning'" size="small">
                {{ scope.row.transactionType === 'ALLOCATION' ? '预算分配' : '支出' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="amount" label="金额(元)" width="120">
            <template slot-scope="scope">
              <span :style="{color: scope.row.amount > 0 ? '#67C23A' : '#F56C6C'}">
                {{ scope.row.amount > 0 ? '+' : '' }}{{ scope.row.amount | formatMoney }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="说明" show-overflow-tooltip></el-table-column>
          <el-table-column prop="createTime" label="交易时间" width="160"></el-table-column>
        </el-table>
      </el-card>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog title="课题组预算详情" :visible.sync="detailDialogVisible" width="800px">
      <div v-if="currentGroup">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="课题组名称">{{ currentGroup.groupName }}</el-descriptions-item>
          <el-descriptions-item label="负责人">{{ currentGroup.piName }}</el-descriptions-item>
          <el-descriptions-item label="部门">{{ currentGroup.department }}</el-descriptions-item>
          <el-descriptions-item label="预算年份">{{ currentGroup.budgetYear }}</el-descriptions-item>
          <el-descriptions-item label="总预算">{{ currentGroup.totalBudget | formatMoney }} 元</el-descriptions-item>
          <el-descriptions-item label="已使用">{{ currentGroup.usedBudget | formatMoney }} 元</el-descriptions-item>
          <el-descriptions-item label="可用预算">{{ currentGroup.availableBudget | formatMoney }} 元</el-descriptions-item>
          <el-descriptions-item label="使用率">{{ currentGroup.usageRate }}%</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import * as echarts from 'echarts'

export default {
  name: 'BudgetReport',
  data() {
    return {
      selectedGroup: null,
      selectedYear: '2024',
      groupList: [],
      summary: {
        totalBudget: 0,
        usedBudget: 0,
        availableBudget: 0,
        usageRate: 0
      },
      groupBudgetList: [],
      transactionList: [],
      detailDialogVisible: false,
      currentGroup: null,
      trendChart: null,
      pieChart: null
    }
  },
  filters: {
    formatMoney(value) {
      if (!value) return '0.00'
      return parseFloat(value).toFixed(2).replace(/\d(?=(\d{3})+\.)/g, '$&,')
    }
  },
  mounted() {
    this.loadGroupList()
    this.loadData()
  },
  beforeDestroy() {
    if (this.trendChart) {
      this.trendChart.dispose()
    }
    if (this.pieChart) {
      this.pieChart.dispose()
    }
  },
  methods: {
    loadGroupList() {
      // 模拟数据
      this.groupList = [
        { id: 1, groupName: '有机化学合成课题组' },
        { id: 2, groupName: '分析化学课题组' },
        { id: 3, groupName: '生物化学课题组' }
      ]
    },
    loadData() {
      // 模拟数据
      this.summary = {
        totalBudget: 1200000,
        usedBudget: 355000,
        availableBudget: 845000,
        usageRate: 29.58
      }

      this.groupBudgetList = [
        {
          id: 1,
          groupName: '有机化学合成课题组',
          piName: '张教授',
          department: '化学系',
          totalBudget: 500000,
          usedBudget: 125000,
          availableBudget: 375000,
          usageRate: '25.00',
          budgetYear: 2024
        },
        {
          id: 2,
          groupName: '分析化学课题组',
          piName: '张教授',
          department: '化学系',
          totalBudget: 300000,
          usedBudget: 80000,
          availableBudget: 220000,
          usageRate: '26.67',
          budgetYear: 2024
        },
        {
          id: 3,
          groupName: '生物化学课题组',
          piName: '李老师',
          department: '生物系',
          totalBudget: 400000,
          usedBudget: 150000,
          availableBudget: 250000,
          usageRate: '37.50',
          budgetYear: 2024
        }
      ]

      this.transactionList = [
        { id: 1, groupName: '有机化学合成课题组', transactionType: 'ALLOCATION', amount: 500000, description: '2024年度预算分配', createTime: '2024-01-01 00:00:00' },
        { id: 2, groupName: '有机化学合成课题组', transactionType: 'EXPENSE', amount: -25000, description: '试剂采购支出', createTime: '2024-02-01 10:00:00' },
        { id: 3, groupName: '有机化学合成课题组', transactionType: 'EXPENSE', amount: -50000, description: '设备购置支出', createTime: '2024-03-15 14:00:00' },
        { id: 4, groupName: '分析化学课题组', transactionType: 'ALLOCATION', amount: 300000, description: '2024年度预算分配', createTime: '2024-01-01 00:00:00' },
        { id: 5, groupName: '生物化学课题组', transactionType: 'ALLOCATION', amount: 400000, description: '2024年度预算分配', createTime: '2024-01-01 00:00:00' }
      ]

      this.$nextTick(() => {
        this.initCharts()
      })
    },
    initCharts() {
      this.initTrendChart()
      this.initPieChart()
    },
    initTrendChart() {
      const chartDom = document.getElementById('budgetTrendChart')
      if (!chartDom) return
      
      this.trendChart = echarts.init(chartDom)
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['预算总额', '已使用', '可用预算']
        },
        xAxis: {
          type: 'category',
          data: ['1月', '2月', '3月', '4月', '5月', '6月']
        },
        yAxis: {
          type: 'value',
          axisLabel: {
            formatter: '{value}万'
          }
        },
        series: [
          {
            name: '预算总额',
            type: 'line',
            data: [120, 120, 120, 120, 120, 120],
            smooth: true
          },
          {
            name: '已使用',
            type: 'line',
            data: [5, 12, 20, 28, 32, 35.5],
            smooth: true
          },
          {
            name: '可用预算',
            type: 'line',
            data: [115, 108, 100, 92, 88, 84.5],
            smooth: true
          }
        ]
      }
      this.trendChart.setOption(option)
    },
    initPieChart() {
      const chartDom = document.getElementById('expenseCategoryChart')
      if (!chartDom) return
      
      this.pieChart = echarts.init(chartDom)
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c}万 ({d}%)'
        },
        legend: {
          orient: 'vertical',
          right: 10,
          top: 'center'
        },
        series: [
          {
            type: 'pie',
            radius: '60%',
            data: [
              { value: 15, name: '试剂采购' },
              { value: 10, name: '设备购置' },
              { value: 5, name: '耗材采购' },
              { value: 3.5, name: '其他支出' }
            ],
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            }
          }
        ]
      }
      this.pieChart.setOption(option)
    },
    viewGroupDetail(row) {
      this.currentGroup = row
      this.detailDialogVisible = true
    },
    exportReport() {
      this.$message.success('报表导出功能开发中...')
    },
    getProgressColor(rate) {
      const value = parseFloat(rate)
      if (value < 50) return '#67C23A'
      if (value < 80) return '#E6A23C'
      return '#F56C6C'
    },
    getBudgetStatus(rate) {
      const value = parseFloat(rate)
      if (value < 50) return { type: 'success', text: '充足' }
      if (value < 80) return { type: 'warning', text: '适中' }
      if (value < 95) return { type: 'danger', text: '紧张' }
      return { type: 'info', text: '耗尽' }
    }
  }
}
</script>

<style scoped>
.el-progress {
  line-height: 1;
}
</style>


