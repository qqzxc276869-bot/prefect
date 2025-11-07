<template>
  <div>
    <el-card>
      <div slot="header">
        <span>废弃物管理</span>
        <el-button type="primary" size="small" style="float: right;" @click="showAddDialog">登记废弃物</el-button>
      </div>
      
      <el-form :inline="true">
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="已登记" value="REGISTERED"></el-option>
            <el-option label="暂存中" value="STORED"></el-option>
            <el-option label="已收运" value="COLLECTED"></el-option>
            <el-option label="已处置" value="DISPOSED"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadList">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="wasteList" border style="margin-top: 20px;">
        <el-table-column prop="disposalNo" label="报废单号" width="180"></el-table-column>
        <el-table-column prop="categoryName" label="废弃物类别" width="150"></el-table-column>
        <el-table-column prop="wasteType" label="类型" width="100">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.wasteType === 'LIQUID'" type="primary" size="mini">废液</el-tag>
            <el-tag v-else-if="scope.row.wasteType === 'SOLID'" type="warning" size="mini">固废</el-tag>
            <el-tag v-else-if="scope.row.wasteType === 'GAS'" type="danger" size="mini">废气</el-tag>
            <el-tag v-else type="info" size="mini">空容器</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="mainComponents" label="主要成分" width="200" show-overflow-tooltip></el-table-column>
        <el-table-column prop="quantity" label="数量" width="100">
          <template slot-scope="scope">
            {{ scope.row.quantity }} {{ scope.row.unit }}
          </template>
        </el-table-column>
        <el-table-column prop="applicantName" label="登记人" width="100"></el-table-column>
        <el-table-column prop="disposalStatus" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.disposalStatus === 'REGISTERED'" type="info">已登记</el-tag>
            <el-tag v-else-if="scope.row.disposalStatus === 'STORED'" type="warning">暂存中</el-tag>
            <el-tag v-else-if="scope.row.disposalStatus === 'COLLECTED'" type="primary">已收运</el-tag>
            <el-tag v-else type="success">已处置</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="登记时间" width="180"></el-table-column>
        <el-table-column label="操作" width="250">
          <template slot-scope="scope">
            <el-button size="mini" @click="viewDetail(scope.row)">查看</el-button>
            <el-button size="mini" type="primary" @click="generateLabel(scope.row)">生成标签</el-button>
            <el-button v-if="scope.row.disposalStatus === 'REGISTERED'" size="mini" type="warning" @click="updateStatus(scope.row, 'STORED')">转暂存</el-button>
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
    
    <!-- 登记废弃物对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="700px">
      <el-form :model="currentItem" label-width="120px">
        <el-form-item label="废弃物类别" required>
          <el-select v-model="currentItem.wasteCategoryId" style="width: 100%;" @change="onCategoryChange">
            <el-option 
              v-for="cat in categories" 
              :key="cat.id" 
              :label="`${cat.categoryName}(${cat.hazardLevel})`" 
              :value="cat.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="废弃物类型" required>
          <el-radio-group v-model="currentItem.wasteType">
            <el-radio label="LIQUID">废液</el-radio>
            <el-radio label="SOLID">固废</el-radio>
            <el-radio label="GAS">废气</el-radio>
            <el-radio label="EMPTY_CONTAINER">空容器</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="来源类型">
          <el-select v-model="currentItem.sourceType" style="width: 100%;">
            <el-option label="过期" value="EXPIRED"></el-option>
            <el-option label="损坏" value="DAMAGED"></el-option>
            <el-option label="实验产生" value="EXPERIMENT"></el-option>
            <el-option label="用完" value="EMPTY"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="主要成分" required>
          <el-input type="textarea" :rows="2" v-model="currentItem.mainComponents" placeholder="请详细描述废弃物的主要成分"></el-input>
        </el-form-item>
        <el-form-item label="数量" required>
          <el-col :span="16">
            <el-input-number v-model="currentItem.quantity" :min="0" :precision="2" style="width: 100%;"></el-input-number>
          </el-col>
          <el-col :span="8" style="padding-left: 10px;">
            <el-select v-model="currentItem.unit" style="width: 100%;">
              <el-option label="千克(kg)" value="kg"></el-option>
              <el-option label="升(L)" value="L"></el-option>
              <el-option label="个(pcs)" value="pcs"></el-option>
            </el-select>
          </el-col>
        </el-form-item>
        <el-form-item label="容器类型">
          <el-input v-model="currentItem.containerType" placeholder="例如：25L塑料桶"></el-input>
        </el-form-item>
        <el-form-item label="容器数量">
          <el-input-number v-model="currentItem.containerCount" :min="1" style="width: 100%;"></el-input-number>
        </el-form-item>
        <el-form-item label="暂存位置">
          <el-input v-model="currentItem.storageLocation"></el-input>
        </el-form-item>
        <el-form-item label="备注">
          <el-input type="textarea" :rows="2" v-model="currentItem.remark"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveItem">保存</el-button>
      </span>
    </el-dialog>
    
    <!-- 详情对话框 -->
    <el-dialog title="废弃物详情" :visible.sync="detailDialogVisible" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="报废单号">{{ detailItem.disposalNo }}</el-descriptions-item>
        <el-descriptions-item label="类别">{{ detailItem.categoryName }}</el-descriptions-item>
        <el-descriptions-item label="类型">
          <el-tag v-if="detailItem.wasteType === 'LIQUID'" type="primary" size="small">废液</el-tag>
          <el-tag v-else-if="detailItem.wasteType === 'SOLID'" type="warning" size="small">固废</el-tag>
          <el-tag v-else-if="detailItem.wasteType === 'GAS'" type="danger" size="small">废气</el-tag>
          <el-tag v-else type="info" size="small">空容器</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="detailItem.disposalStatus === 'REGISTERED'" type="info">已登记</el-tag>
          <el-tag v-else-if="detailItem.disposalStatus === 'STORED'" type="warning">暂存中</el-tag>
          <el-tag v-else-if="detailItem.disposalStatus === 'COLLECTED'" type="primary">已收运</el-tag>
          <el-tag v-else type="success">已处置</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="主要成分" :span="2">{{ detailItem.mainComponents }}</el-descriptions-item>
        <el-descriptions-item label="数量">{{ detailItem.quantity }} {{ detailItem.unit }}</el-descriptions-item>
        <el-descriptions-item label="容器">{{ detailItem.containerType }} × {{ detailItem.containerCount }}</el-descriptions-item>
        <el-descriptions-item label="暂存位置" :span="2">{{ detailItem.storageLocation }}</el-descriptions-item>
        <el-descriptions-item label="登记人">{{ detailItem.applicantName }}</el-descriptions-item>
        <el-descriptions-item label="登记时间">{{ detailItem.createTime }}</el-descriptions-item>
        <el-descriptions-item label="收运公司" v-if="detailItem.collectionCompany">{{ detailItem.collectionCompany }}</el-descriptions-item>
        <el-descriptions-item label="收运日期" v-if="detailItem.collectionDate">{{ detailItem.collectionDate }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailItem.remark }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script>
import { getWasteList, createWaste, updateWasteStatus, generateWasteLabel } from '@/api/waste'
import { getWasteCategories } from '@/api/waste'

export default {
  name: 'WasteManagement',
  data() {
    return {
      searchForm: {
        status: ''
      },
      wasteList: [],
      categories: [],
      pagination: {
        current: 1,
        size: 10,
        total: 0
      },
      dialogVisible: false,
      dialogTitle: '',
      currentItem: {
        wasteCategoryId: null,
        wasteType: 'LIQUID',
        sourceType: 'EXPERIMENT',
        mainComponents: '',
        quantity: 0,
        unit: 'L',
        containerType: '',
        containerCount: 1,
        storageLocation: '',
        remark: ''
      },
      detailDialogVisible: false,
      detailItem: {}
    }
  },
  mounted() {
    this.loadList()
    this.loadCategories()
  },
  methods: {
    loadList() {
      const params = {
        current: this.pagination.current,
        size: this.pagination.size,
        status: this.searchForm.status
      }
      getWasteList(params).then(response => {
        if (response.code === 200) {
          this.wasteList = response.data.records
          this.pagination.total = response.data.total
        }
      })
    },
    loadCategories() {
      getWasteCategories().then(response => {
        if (response.code === 200) {
          this.categories = response.data
        }
      })
    },
    resetSearch() {
      this.searchForm = { status: '' }
      this.pagination.current = 1
      this.loadList()
    },
    showAddDialog() {
      this.dialogTitle = '登记废弃物'
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      this.currentItem = {
        wasteCategoryId: null,
        wasteType: 'LIQUID',
        sourceType: 'EXPERIMENT',
        mainComponents: '',
        quantity: 0,
        unit: 'L',
        containerType: '',
        containerCount: 1,
        storageLocation: '',
        remark: '',
        applicantId: userInfo.id,
        applicantName: userInfo.realName
      }
      this.dialogVisible = true
    },
    onCategoryChange(categoryId) {
      const category = this.categories.find(c => c.id === categoryId)
      if (category && category.storageRequirements) {
        this.currentItem.storageLocation = category.storageRequirements
      }
    },
    saveItem() {
      if (!this.currentItem.wasteCategoryId || !this.currentItem.mainComponents || !this.currentItem.quantity) {
        this.$message.warning('请填写必填项')
        return
      }
      createWaste(this.currentItem).then(response => {
        if (response.code === 200) {
          this.$message.success('登记成功')
          this.dialogVisible = false
          this.loadList()
        } else {
          this.$message.error(response.message || '登记失败')
        }
      })
    },
    viewDetail(row) {
      this.detailItem = { ...row }
      this.detailDialogVisible = true
    },
    generateLabel(row) {
      generateWasteLabel(row.id).then(response => {
        if (response.code === 200) {
          this.$message.success('标签生成成功')
          // 可以在这里实现标签打印功能
          console.log('标签数据：', response.data)
          // 实际应用中可以调用打印API或下载标签PDF
        } else {
          this.$message.error(response.message || '生成失败')
        }
      })
    },
    updateStatus(row, newStatus) {
      updateWasteStatus({
        id: row.id,
        status: newStatus
      }).then(response => {
        if (response.code === 200) {
          this.$message.success('状态更新成功')
          this.loadList()
        } else {
          this.$message.error(response.message || '更新失败')
        }
      })
    },
    handleSizeChange(val) {
      this.pagination.size = val
      this.loadList()
    },
    handleCurrentChange(val) {
      this.pagination.current = val
      this.loadList()
    }
  }
}
</script>

<style scoped>
</style>








