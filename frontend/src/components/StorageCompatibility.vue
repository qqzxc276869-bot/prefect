<template>
  <div>
    <el-card>
      <div slot="header">
        <span>存储兼容性规则管理</span>
        <el-button type="primary" size="small" style="float: right;" @click="showAddDialog">
          <i class="el-icon-plus"></i> 添加规则
        </el-button>
      </div>

      <!-- 搜索栏 -->
      <el-form :inline="true" style="margin-bottom: 20px;">
        <el-form-item label="危险品类型">
          <el-input v-model="searchText" placeholder="搜索危险品类型" clearable style="width: 200px;"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadRules">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="ruleList" border v-loading="loading">
        <el-table-column prop="id" label="ID" width="60"></el-table-column>
        <el-table-column prop="hazardType1" label="危险品类型1" width="150"></el-table-column>
        <el-table-column prop="hazardType2" label="危险品类型2" width="150"></el-table-column>
        <el-table-column prop="incompatibilityLevel" label="不兼容等级" width="120">
          <template slot-scope="scope">
            <el-tag :type="getLevelType(scope.row.incompatibilityLevel)">
              {{ getLevelText(scope.row.incompatibilityLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip></el-table-column>
        <el-table-column prop="safetyDistance" label="安全距离(米)" width="120"></el-table-column>
        <el-table-column prop="enabled" label="状态" width="80">
          <template slot-scope="scope">
            <el-tag :type="scope.row.enabled ? 'success' : 'info'">
              {{ scope.row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" @click="editRule(scope.row)">编辑</el-button>
            <el-button 
              size="mini" 
              :type="scope.row.enabled ? 'warning' : 'success'" 
              @click="toggleEnabled(scope.row)">
              {{ scope.row.enabled ? '禁用' : '启用' }}
            </el-button>
            <el-button size="mini" type="danger" @click="deleteRule(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        style="margin-top: 20px; text-align: right;"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="currentPage"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total">
      </el-pagination>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="600px">
      <el-form :model="form" :rules="rules" ref="ruleForm" label-width="120px">
        <el-form-item label="危险品类型1" prop="hazardType1">
          <el-select v-model="form.hazardType1" placeholder="请选择" style="width: 100%;">
            <el-option label="易燃液体" value="易燃液体"></el-option>
            <el-option label="易燃固体" value="易燃固体"></el-option>
            <el-option label="强酸" value="强酸"></el-option>
            <el-option label="强碱" value="强碱"></el-option>
            <el-option label="氧化剂" value="氧化剂"></el-option>
            <el-option label="还原剂" value="还原剂"></el-option>
            <el-option label="毒性物质" value="毒性物质"></el-option>
            <el-option label="腐蚀性物质" value="腐蚀性物质"></el-option>
            <el-option label="金属盐" value="金属盐"></el-option>
            <el-option label="有机溶剂" value="有机溶剂"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="危险品类型2" prop="hazardType2">
          <el-select v-model="form.hazardType2" placeholder="请选择" style="width: 100%;">
            <el-option label="易燃液体" value="易燃液体"></el-option>
            <el-option label="易燃固体" value="易燃固体"></el-option>
            <el-option label="强酸" value="强酸"></el-option>
            <el-option label="强碱" value="强碱"></el-option>
            <el-option label="氧化剂" value="氧化剂"></el-option>
            <el-option label="还原剂" value="还原剂"></el-option>
            <el-option label="毒性物质" value="毒性物质"></el-option>
            <el-option label="腐蚀性物质" value="腐蚀性物质"></el-option>
            <el-option label="金属盐" value="金属盐"></el-option>
            <el-option label="有机溶剂" value="有机溶剂"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="不兼容等级" prop="incompatibilityLevel">
          <el-select v-model="form.incompatibilityLevel" placeholder="请选择" style="width: 100%;">
            <el-option label="高度不兼容" value="HIGH"></el-option>
            <el-option label="中度不兼容" value="MEDIUM"></el-option>
            <el-option label="低度不兼容" value="LOW"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="安全距离(米)" prop="safetyDistance">
          <el-input-number v-model="form.safetyDistance" :min="0" :max="100" :step="0.5" style="width: 100%;"></el-input-number>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input type="textarea" v-model="form.description" :rows="4" placeholder="请描述两种物质的不兼容性及可能产生的危险"></el-input>
        </el-form-item>
        <el-form-item label="启用状态">
          <el-switch v-model="form.enabled"></el-switch>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRule">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'StorageCompatibility',
  data() {
    return {
      loading: false,
      ruleList: [],
      searchText: '',
      currentPage: 1,
      pageSize: 10,
      total: 0,
      dialogVisible: false,
      dialogTitle: '添加兼容性规则',
      form: {
        id: null,
        hazardType1: '',
        hazardType2: '',
        incompatibilityLevel: '',
        safetyDistance: 1.0,
        description: '',
        enabled: true
      },
      rules: {
        hazardType1: [{ required: true, message: '请选择危险品类型1', trigger: 'change' }],
        hazardType2: [{ required: true, message: '请选择危险品类型2', trigger: 'change' }],
        incompatibilityLevel: [{ required: true, message: '请选择不兼容等级', trigger: 'change' }],
        description: [{ required: true, message: '请输入描述', trigger: 'blur' }]
      }
    }
  },
  mounted() {
    this.loadRules()
  },
  methods: {
    async loadRules() {
      this.loading = true
      try {
        // 这里应该调用后端API，暂时使用模拟数据
        // const res = await getStorageRules({ page: this.currentPage, size: this.pageSize })
        
        // 模拟数据
        setTimeout(() => {
          this.ruleList = [
            { id: 1, hazardType1: '易燃液体', hazardType2: '强酸', incompatibilityLevel: 'HIGH', description: '有机溶剂与强酸反应可能引发火灾或爆炸', safetyDistance: 2.0, enabled: true },
            { id: 2, hazardType1: '易燃液体', hazardType2: '强碱', incompatibilityLevel: 'HIGH', description: '有机溶剂与强碱反应可能引发火灾', safetyDistance: 2.0, enabled: true },
            { id: 3, hazardType1: '易燃液体', hazardType2: '氧化剂', incompatibilityLevel: 'MEDIUM', description: '易燃液体与氧化剂接触可能发生氧化反应', safetyDistance: 1.5, enabled: true },
            { id: 4, hazardType1: '强酸', hazardType2: '强碱', incompatibilityLevel: 'HIGH', description: '酸碱中和反应剧烈，可能产生大量热量', safetyDistance: 1.5, enabled: true },
            { id: 5, hazardType1: '强酸', hazardType2: '金属盐', incompatibilityLevel: 'MEDIUM', description: '酸与活泼金属盐反应可能产生有毒气体', safetyDistance: 1.0, enabled: true }
          ]
          this.total = this.ruleList.length
          this.loading = false
        }, 500)
      } catch (error) {
        this.$message.error('加载数据失败')
        this.loading = false
      }
    },
    showAddDialog() {
      this.dialogTitle = '添加兼容性规则'
      this.form = {
        id: null,
        hazardType1: '',
        hazardType2: '',
        incompatibilityLevel: '',
        safetyDistance: 1.0,
        description: '',
        enabled: true
      }
      this.dialogVisible = true
    },
    editRule(row) {
      this.dialogTitle = '编辑兼容性规则'
      this.form = { ...row }
      this.dialogVisible = true
    },
    saveRule() {
      this.$refs.ruleForm.validate(valid => {
        if (valid) {
          // 这里应该调用后端API保存数据
          if (this.form.id) {
            this.$message.success('规则更新成功')
          } else {
            this.$message.success('规则添加成功')
          }
          this.dialogVisible = false
          this.loadRules()
        }
      })
    },
    toggleEnabled(row) {
      this.$confirm(`确定要${row.enabled ? '禁用' : '启用'}该规则吗？`, '提示', {
        type: 'warning'
      }).then(() => {
        // 调用后端API
        row.enabled = !row.enabled
        this.$message.success('操作成功')
      }).catch(() => {})
    },
    deleteRule(row) {
      this.$confirm('确定要删除该规则吗？', '提示', {
        type: 'warning'
      }).then(() => {
        // 调用后端API删除
        this.$message.success('删除成功')
        this.loadRules()
      }).catch(() => {})
    },
    resetSearch() {
      this.searchText = ''
      this.loadRules()
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.loadRules()
    },
    handleCurrentChange(val) {
      this.currentPage = val
      this.loadRules()
    },
    getLevelType(level) {
      const types = {
        'HIGH': 'danger',
        'MEDIUM': 'warning',
        'LOW': 'info'
      }
      return types[level] || 'info'
    },
    getLevelText(level) {
      const texts = {
        'HIGH': '高度不兼容',
        'MEDIUM': '中度不兼容',
        'LOW': '低度不兼容'
      }
      return texts[level] || level
    }
  }
}
</script>

<style scoped>
.el-table {
  margin-top: 20px;
}
</style>


