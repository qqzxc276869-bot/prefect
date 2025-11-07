<template>
  <div>
    <el-card>
      <div slot="header">
        <span>GHS/MSDS 管理</span>
        <el-button type="primary" size="small" style="float: right;" @click="showAddDialog">添加MSDS</el-button>
      </div>
      
      <el-form :inline="true">
        <el-form-item label="试剂名称">
          <el-input v-model="searchForm.reagentName" placeholder="请输入试剂名称" clearable></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadList">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="ghsList" border style="margin-top: 20px;">
        <el-table-column prop="reagentName" label="试剂名称" width="150"></el-table-column>
        <el-table-column prop="signalWord" label="信号词" width="100">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.signalWord === 'Danger'" type="danger">危险</el-tag>
            <el-tag v-else-if="scope.row.signalWord === 'Warning'" type="warning">警告</el-tag>
            <el-tag v-else type="info">无</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="hazardClass" label="危险类别" width="150"></el-table-column>
        <el-table-column prop="ghsPictograms" label="GHS象形图" width="200">
          <template slot-scope="scope">
            <el-tag v-for="(item, index) in parseJson(scope.row.ghsPictograms)" :key="index" size="small" style="margin-right: 5px;">
              {{ item }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="aiExtracted" label="AI提取" width="80">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.aiExtracted" type="success" size="mini">是</el-tag>
            <el-tag v-else type="info" size="mini">否</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="verified" label="已验证" width="80">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.verified" type="success" size="mini">是</el-tag>
            <el-tag v-else type="warning" size="mini">否</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template slot-scope="scope">
            <el-button size="mini" @click="viewDetail(scope.row)">查看</el-button>
            <el-button size="mini" type="primary" @click="editItem(scope.row)">编辑</el-button>
            <el-button v-if="!scope.row.verified" size="mini" type="success" @click="verifyItem(scope.row)">验证</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        style="margin-top: 20px; text-align: center;"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pagination.current"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pagination.size"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total">
      </el-pagination>
    </el-card>
    
    <!-- 详情对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="800px">
      <el-form :model="currentItem" label-width="150px">
        <el-form-item label="试剂名称">
          <el-input v-model="currentItem.reagentName" disabled></el-input>
        </el-form-item>
        <el-form-item label="信号词">
          <el-select v-model="currentItem.signalWord" style="width: 100%;">
            <el-option label="Danger - 危险" value="Danger"></el-option>
            <el-option label="Warning - 警告" value="Warning"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="危险类别">
          <el-input v-model="currentItem.hazardClass"></el-input>
        </el-form-item>
        <el-form-item label="H-phrases危险说明">
          <el-input type="textarea" :rows="3" v-model="currentItem.hPhrases" placeholder="请输入JSON数组格式"></el-input>
        </el-form-item>
        <el-form-item label="P-phrases防范说明">
          <el-input type="textarea" :rows="3" v-model="currentItem.pPhrases" placeholder="请输入JSON数组格式"></el-input>
        </el-form-item>
        <el-form-item label="急救措施">
          <el-input type="textarea" :rows="2" v-model="currentItem.firstAidMeasures"></el-input>
        </el-form-item>
        <el-form-item label="消防措施">
          <el-input type="textarea" :rows="2" v-model="currentItem.fireFightingMeasures"></el-input>
        </el-form-item>
        <el-form-item label="操作与储存">
          <el-input type="textarea" :rows="2" v-model="currentItem.handlingAndStorage"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveItem">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getGhsList, saveGhs, verifyGhs } from '@/api/ghs'

export default {
  name: 'GhsManagement',
  data() {
    return {
      searchForm: {
        reagentName: ''
      },
      ghsList: [],
      pagination: {
        current: 1,
        size: 10,
        total: 0
      },
      dialogVisible: false,
      dialogTitle: '',
      currentItem: {
        reagentId: null,
        reagentName: '',
        signalWord: '',
        hazardClass: '',
        hPhrases: '',
        pPhrases: '',
        firstAidMeasures: '',
        fireFightingMeasures: '',
        handlingAndStorage: ''
      }
    }
  },
  mounted() {
    this.loadList()
  },
  methods: {
    loadList() {
      const params = {
        current: this.pagination.current,
        size: this.pagination.size,
        reagentName: this.searchForm.reagentName
      }
      getGhsList(params).then(response => {
        if (response.code === 200) {
          this.ghsList = response.data.records
          this.pagination.total = response.data.total
        }
      })
    },
    resetSearch() {
      this.searchForm = { reagentName: '' }
      this.pagination.current = 1
      this.loadList()
    },
    showAddDialog() {
      this.dialogTitle = '添加MSDS'
      this.currentItem = {
        reagentId: null,
        reagentName: '',
        signalWord: '',
        hazardClass: '',
        hPhrases: '[]',
        pPhrases: '[]',
        firstAidMeasures: '',
        fireFightingMeasures: '',
        handlingAndStorage: ''
      }
      this.dialogVisible = true
    },
    viewDetail(row) {
      this.dialogTitle = '查看MSDS详情'
      this.currentItem = { ...row }
      this.dialogVisible = true
    },
    editItem(row) {
      this.dialogTitle = '编辑MSDS'
      this.currentItem = { ...row }
      this.dialogVisible = true
    },
    saveItem() {
      saveGhs(this.currentItem).then(response => {
        if (response.code === 200) {
          this.$message.success('保存成功')
          this.dialogVisible = false
          this.loadList()
        } else {
          this.$message.error(response.message || '保存失败')
        }
      })
    },
    verifyItem(row) {
      this.$confirm('确认验证该MSDS数据吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        verifyGhs(row.id).then(response => {
          if (response.code === 200) {
            this.$message.success('验证成功')
            this.loadList()
          }
        })
      })
    },
    handleSizeChange(val) {
      this.pagination.size = val
      this.loadList()
    },
    handleCurrentChange(val) {
      this.pagination.current = val
      this.loadList()
    },
    parseJson(str) {
      try {
        return JSON.parse(str || '[]')
      } catch (e) {
        return []
      }
    }
  }
}
</script>

<style scoped>
</style>








