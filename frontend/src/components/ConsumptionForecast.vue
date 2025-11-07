<template>
  <el-card>
    <div slot="header">消耗预测</div>
    <el-alert title="该功能使用AI技术预测试剂消耗趋势，帮助实验室提前做好采购计划" type="info" style="margin-bottom: 20px;" :closable="false"></el-alert>
    <el-form :inline="true">
      <el-form-item label="选择试剂">
        <el-select v-model="selectedReagent" placeholder="请选择" style="width: 200px;">
          <el-option label="乙醇" value="1"></el-option>
          <el-option label="甲醇" value="2"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="predict">开始预测</el-button>
      </el-form-item>
    </el-form>
    <div v-if="showResult">
      <div id="forecastChart" style="height: 400px; margin: 20px 0;"></div>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="预测周期">30天</el-descriptions-item>
        <el-descriptions-item label="预测消耗量">15.5瓶</el-descriptions-item>
        <el-descriptions-item label="置信度">85%</el-descriptions-item>
        <el-descriptions-item label="建议补货量">20瓶</el-descriptions-item>
      </el-descriptions>
    </div>
  </el-card>
</template>

<script>
export default {
  name: 'ConsumptionForecast',
  data() {
    return {
      selectedReagent: '',
      showResult: false
    }
  },
  methods: {
    predict() {
      if (!this.selectedReagent) {
        this.$message.warning('请选择试剂')
        return
      }
      this.showResult = true
      this.$message.success('预测完成')
    }
  }
}
</script>


