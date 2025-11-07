<template>
  <div>
    <el-card>
      <div slot="header">
        <span>SOP文档管理</span>
        <el-button type="primary" size="small" style="float: right;" @click="showAddDialog">
          <i class="el-icon-plus"></i> 添加SOP
        </el-button>
      </div>

      <!-- 搜索栏 -->
      <el-form :inline="true" style="margin-bottom: 20px;">
        <el-form-item label="标题">
          <el-input v-model="searchTitle" placeholder="搜索标题" clearable style="width: 200px;"></el-input>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="searchCategory" placeholder="选择分类" clearable style="width: 150px;">
            <el-option label="安全管理" value="安全管理"></el-option>
            <el-option label="库存管理" value="库存管理"></el-option>
            <el-option label="应急管理" value="应急管理"></el-option>
            <el-option label="操作规范" value="操作规范"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchStatus" placeholder="选择状态" clearable style="width: 120px;">
            <el-option label="草稿" value="DRAFT"></el-option>
            <el-option label="已发布" value="PUBLISHED"></el-option>
            <el-option label="已归档" value="ARCHIVED"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadSopList">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="sopList" border v-loading="loading">
        <el-table-column prop="id" label="ID" width="60"></el-table-column>
        <el-table-column prop="sopNo" label="SOP编号" width="120"></el-table-column>
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip></el-table-column>
        <el-table-column prop="category" label="分类" width="100"></el-table-column>
        <el-table-column prop="version" label="版本" width="80"></el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="authorName" label="作者" width="100"></el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="160"></el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" type="text" @click="viewSop(scope.row)">查看</el-button>
            <el-button size="mini" type="primary" @click="editSop(scope.row)">编辑</el-button>
            <el-button 
              size="mini" 
              type="success" 
              v-if="scope.row.status === 'DRAFT'"
              @click="publishSop(scope.row)">发布</el-button>
            <el-button 
              size="mini" 
              type="warning" 
              v-if="scope.row.status === 'PUBLISHED'"
              @click="archiveSop(scope.row)">归档</el-button>
            <el-button size="mini" type="danger" @click="deleteSop(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        style="margin-top: 20px; text-align: right;"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="currentPage"
        :page-sizes="[10, 20, 50]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total">
      </el-pagination>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="800px">
      <el-form :model="form" :rules="rules" ref="sopForm" label-width="100px">
        <el-form-item label="SOP编号" prop="sopNo">
          <el-input v-model="form.sopNo" placeholder="如: SOP-2024-001"></el-input>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入SOP标题"></el-input>
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择分类" style="width: 100%;">
            <el-option label="安全管理" value="安全管理"></el-option>
            <el-option label="库存管理" value="库存管理"></el-option>
            <el-option label="应急管理" value="应急管理"></el-option>
            <el-option label="操作规范" value="操作规范"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="版本号" prop="version">
          <el-input v-model="form.version" placeholder="如: V1.0"></el-input>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input 
            type="textarea" 
            v-model="form.content" 
            :rows="10" 
            placeholder="请输入SOP内容，可以包含多个章节和步骤"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveSop">保存为草稿</el-button>
        <el-button type="success" @click="saveSop(true)">保存并发布</el-button>
      </span>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog title="SOP详情" :visible.sync="viewDialogVisible" width="900px">
      <div v-if="currentSop">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="SOP编号">{{ currentSop.sopNo }}</el-descriptions-item>
          <el-descriptions-item label="版本">{{ currentSop.version }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ currentSop.category }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentSop.status)">{{ getStatusText(currentSop.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="作者">{{ currentSop.authorName }}</el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ currentSop.publishTime || '未发布' }}</el-descriptions-item>
        </el-descriptions>
        <el-divider></el-divider>
        <h3>{{ currentSop.title }}</h3>
        <div style="white-space: pre-wrap; line-height: 1.8; padding: 20px; background: #f5f7fa; border-radius: 4px;">
          {{ currentSop.content }}
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'SopManagement',
  data() {
    return {
      loading: false,
      sopList: [],
      searchTitle: '',
      searchCategory: '',
      searchStatus: '',
      currentPage: 1,
      pageSize: 10,
      total: 0,
      dialogVisible: false,
      viewDialogVisible: false,
      dialogTitle: '添加SOP',
      currentSop: null,
      form: {
        id: null,
        sopNo: '',
        title: '',
        category: '',
        version: 'V1.0',
        content: '',
        status: 'DRAFT'
      },
      rules: {
        sopNo: [{ required: true, message: '请输入SOP编号', trigger: 'blur' }],
        title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
        category: [{ required: true, message: '请选择分类', trigger: 'change' }],
        version: [{ required: true, message: '请输入版本号', trigger: 'blur' }],
        content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
      }
    }
  },
  mounted() {
    this.loadSopList()
  },
  methods: {
    async loadSopList() {
      this.loading = true
      try {
        // 模拟数据
        setTimeout(() => {
          this.sopList = [
            {
              id: 1,
              sopNo: 'SOP-2024-001',
              title: '实验室安全操作规程',
              category: '安全管理',
              version: 'V1.0',
              status: 'PUBLISHED',
              authorName: '系统管理员',
              publishTime: '2024-01-15 10:00:00',
              content: '1. 目的：规范实验室安全操作，确保人员和设备安全。\n2. 范围：适用于所有实验室工作人员。\n3. 内容：\n3.1 进入实验室必须穿戴实验服、护目镜等个人防护装备。\n3.2 使用危险化学品前必须了解其安全特性。\n3.3 实验过程中严禁饮食、吸烟。\n3.4 实验结束后必须清理实验台面，关闭水电气源。'
            },
            {
              id: 2,
              sopNo: 'SOP-2024-002',
              title: '试剂入库操作规程',
              category: '库存管理',
              version: 'V1.0',
              status: 'PUBLISHED',
              authorName: '系统管理员',
              publishTime: '2024-01-20 11:00:00',
              content: '1. 目的：规范试剂入库流程，确保库存准确。\n2. 范围：适用于所有试剂入库操作。\n3. 内容：\n3.1 验收试剂包装完整性，核对品名、规格、数量。\n3.2 检查生产日期、有效期，拒收过期或临期试剂。\n3.3 在系统中录入试剂信息，打印标签并粘贴。\n3.4 按照分类和存储要求放置到指定位置。'
            },
            {
              id: 3,
              sopNo: 'SOP-2024-003',
              title: '危险化学品应急处理规程',
              category: '应急管理',
              version: 'V1.0',
              status: 'PUBLISHED',
              authorName: '系统管理员',
              publishTime: '2024-02-01 10:00:00',
              content: '1. 目的：规范危险化学品事故应急处理，减少损失。\n2. 范围：适用于所有危险化学品泄漏、火灾等事故。\n3. 内容：\n3.1 发现事故立即报警并通知相关人员。\n3.2 疏散无关人员，隔离事故区域。\n3.3 根据化学品特性选择合适的应急处理方法。\n3.4 事故处理后进行总结和记录。'
            }
          ]
          this.total = this.sopList.length
          this.loading = false
        }, 500)
      } catch (error) {
        this.$message.error('加载数据失败')
        this.loading = false
      }
    },
    showAddDialog() {
      this.dialogTitle = '添加SOP文档'
      this.form = {
        id: null,
        sopNo: '',
        title: '',
        category: '',
        version: 'V1.0',
        content: '',
        status: 'DRAFT'
      }
      this.dialogVisible = true
    },
    editSop(row) {
      this.dialogTitle = '编辑SOP文档'
      this.form = { ...row }
      this.dialogVisible = true
    },
    viewSop(row) {
      this.currentSop = row
      this.viewDialogVisible = true
    },
    saveSop(publish = false) {
      this.$refs.sopForm.validate(valid => {
        if (valid) {
          if (publish) {
            this.form.status = 'PUBLISHED'
          }
          // 调用后端API保存
          this.$message.success(publish ? 'SOP已发布' : 'SOP已保存为草稿')
          this.dialogVisible = false
          this.loadSopList()
        }
      })
    },
    publishSop(row) {
      this.$confirm('确定要发布该SOP文档吗？', '提示', {
        type: 'warning'
      }).then(() => {
        // 调用后端API
        row.status = 'PUBLISHED'
        this.$message.success('发布成功')
      }).catch(() => {})
    },
    archiveSop(row) {
      this.$confirm('确定要归档该SOP文档吗？', '提示', {
        type: 'warning'
      }).then(() => {
        // 调用后端API
        row.status = 'ARCHIVED'
        this.$message.success('归档成功')
      }).catch(() => {})
    },
    deleteSop(row) {
      this.$confirm('确定要删除该SOP文档吗？', '提示', {
        type: 'warning'
      }).then(() => {
        // 调用后端API删除
        this.$message.success('删除成功')
        this.loadSopList()
      }).catch(() => {})
    },
    resetSearch() {
      this.searchTitle = ''
      this.searchCategory = ''
      this.searchStatus = ''
      this.loadSopList()
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.loadSopList()
    },
    handleCurrentChange(val) {
      this.currentPage = val
      this.loadSopList()
    },
    getStatusType(status) {
      const types = {
        'DRAFT': 'info',
        'PUBLISHED': 'success',
        'ARCHIVED': 'warning'
      }
      return types[status] || 'info'
    },
    getStatusText(status) {
      const texts = {
        'DRAFT': '草稿',
        'PUBLISHED': '已发布',
        'ARCHIVED': '已归档'
      }
      return texts[status] || status
    }
  }
}
</script>

<style scoped>
.el-descriptions {
  margin-bottom: 20px;
}
</style>


