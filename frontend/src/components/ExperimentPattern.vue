<template>
  <el-card>
    <div slot="header">实验模式分析</div>
    <el-alert title="分析实验室的试剂使用模式，识别常用组合和实验周期" type="info" style="margin-bottom: 20px;" :closable="false"></el-alert>
    <el-tabs v-model="activeTab">
      <el-tab-pane label="使用模式" name="pattern">
        <el-table :data="patterns" border>
          <el-table-column prop="name" label="模式名称"></el-table-column>
          <el-table-column prop="reagents" label="试剂组合" show-overflow-tooltip></el-table-column>
          <el-table-column prop="frequency" label="使用频率"></el-table-column>
          <el-table-column prop="lastUsed" label="最后使用"></el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="周期分析" name="cycle">
        <div id="cycleChart" style="height: 400px;"></div>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<script>
export default {
  name: 'ExperimentPattern',
  data() {
    return {
      activeTab: 'pattern',
      patterns: [
        { name: '有机合成实验', reagents: '乙醇, 甲醇, 丙酮', frequency: '每周3次', lastUsed: '2024-06-05' },
        { name: '酸碱滴定实验', reagents: '盐酸, 氢氧化钠, 酚酞', frequency: '每周2次', lastUsed: '2024-06-04' }
      ]
    }
  }
}
</script>


