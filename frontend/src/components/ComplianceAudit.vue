<template>
  <el-card>
    <div slot="header">合规审计</div>
    <el-alert title="审计试剂管理的合规性，检测异常操作和违规行为" type="info" style="margin-bottom: 20px;" :closable="false"></el-alert>
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="text-align: center;">
            <div style="font-size: 14px; color: #909399;">合规评分</div>
            <div style="font-size: 36px; font-weight: bold; color: #67C23A; margin: 10px 0;">98</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="text-align: center;">
            <div style="font-size: 14px; color: #909399;">异常记录</div>
            <div style="font-size: 36px; font-weight: bold; color: #E6A23C; margin: 10px 0;">3</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="text-align: center;">
            <div style="font-size: 14px; color: #909399;">待处理</div>
            <div style="font-size: 36px; font-weight: bold; color: #F56C6C; margin: 10px 0;">1</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="text-align: center;">
            <div style="font-size: 14px; color: #909399;">审计次数</div>
            <div style="font-size: 36px; font-weight: bold; color: #409EFF; margin: 10px 0;">156</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-table :data="auditList" border>
      <el-table-column prop="type" label="审计类型" width="120"></el-table-column>
      <el-table-column prop="content" label="审计内容"></el-table-column>
      <el-table-column prop="result" label="结果" width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.result === '合规' ? 'success' : 'warning'">{{ scope.row.result }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="time" label="审计时间" width="160"></el-table-column>
    </el-table>
  </el-card>
</template>

<script>
export default {
  name: 'ComplianceAudit',
  data() {
    return {
      auditList: [
        { type: '出入库审计', content: '乙醇出库记录完整', result: '合规', time: '2024-06-06 10:00:00' },
        { type: '存储审计', content: '危险品分类存放检查', result: '合规', time: '2024-06-06 09:00:00' },
        { type: '权限审计', content: '用户权限异常操作', result: '异常', time: '2024-06-05 15:30:00' }
      ]
    }
  }
}
</script>


