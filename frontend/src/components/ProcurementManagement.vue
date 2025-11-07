<template>
  <div>
    <el-card>
      <div slot="header">
        <span>采购管理</span>
        <el-button type="primary" size="small" style="float: right;" @click="showAddDialog">
          <i class="el-icon-plus"></i> 新建采购申请
        </el-button>
      </div>

      <!-- 搜索栏 -->
      <el-form :inline="true" style="margin-bottom: 20px;">
        <el-form-item label="采购单号">
          <el-input v-model="searchNo" placeholder="搜索采购单号" clearable style="width: 150px;"></el-input>
        </el-form-item>
        <el-form-item label="试剂名称">
          <el-input v-model="searchName" placeholder="搜索试剂名称" clearable style="width: 150px;"></el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchStatus" placeholder="选择状态" clearable style="width: 120px;">
            <el-option label="待审批" value="PENDING"></el-option>
            <el-option label="已批准" value="APPROVED"></el-option>
            <el-option label="已拒绝" value="REJECTED"></el-option>
            <el-option label="已订购" value="ORDERED"></el-option>
            <el-option label="已收货" value="RECEIVED"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadList">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="list" border v-loading="loading">
        <el-table-column prop="requestNo" label="采购单号" width="140"></el-table-column>
        <el-table-column prop="reagentName" label="试剂名称" width="150"></el-table-column>
        <el-table-column prop="quantity" label="数量" width="80"></el-table-column>
        <el-table-column prop="unit" label="单位" width="60"></el-table-column>
        <el-table-column prop="estimatedPrice" label="预估价格(元)" width="120"></el-table-column>
        <el-table-column prop="supplier" label="供应商" width="120"></el-table-column>
        <el-table-column prop="urgency" label="紧急程度" width="90">
          <template slot-scope="scope">
            <el-tag :type="getUrgencyType(scope.row.urgency)" size="small">
              {{ getUrgencyText(scope.row.urgency) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="requesterName" label="申请人" width="90"></el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" type="text" @click="viewDetail(scope.row)">查看</el-button>
            <el-button 
              size="mini" 
              type="success" 
              v-if="scope.row.status === 'PENDING'"
              @click="approveProcurement(scope.row)">审批</el-button>
            <el-button 
              size="mini" 
              type="primary" 
              v-if="scope.row.status === 'APPROVED'"
              @click="orderProcurement(scope.row)">订购</el-button>
            <el-button 
              size="mini" 
              type="warning" 
              v-if="scope.row.status === 'ORDERED'"
              @click="receiveProcurement(scope.row)">确认收货</el-button>
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

    <!-- 新建/编辑采购对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="600px">
      <el-form :model="form" :rules="rules" ref="procurementForm" label-width="100px">
        <el-form-item label="试剂名称" prop="reagentName">
          <el-input v-model="form.reagentName" placeholder="请输入试剂名称"></el-input>
        </el-form-item>
        <el-form-item label="数量" prop="quantity">
          <el-input-number v-model="form.quantity" :min="1" style="width: 100%;"></el-input-number>
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="form.unit" placeholder="如: 瓶、盒、kg"></el-input>
        </el-form-item>
        <el-form-item label="预估价格" prop="estimatedPrice">
          <el-input-number v-model="form.estimatedPrice" :min="0" :precision="2" style="width: 100%;"></el-input-number>
        </el-form-item>
        <el-form-item label="供应商" prop="supplier">
          <el-input v-model="form.supplier" placeholder="请输入供应商名称"></el-input>
        </el-form-item>
        <el-form-item label="紧急程度" prop="urgency">
          <el-select v-model="form.urgency" placeholder="请选择" style="width: 100%;">
            <el-option label="普通" value="NORMAL"></el-option>
            <el-option label="紧急" value="HIGH"></el-option>
            <el-option label="特急" value="URGENT"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="采购原因" prop="reason">
          <el-input type="textarea" v-model="form.reason" :rows="3" placeholder="请说明采购原因"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveProcurement">提交</el-button>
      </span>
    </el-dialog>

    <!-- 审批对话框 -->
    <el-dialog title="审批采购申请" :visible.sync="approveDialogVisible" width="500px">
      <el-form :model="approveForm" label-width="100px">
        <el-form-item label="审批结果">
          <el-radio-group v-model="approveForm.result">
            <el-radio label="APPROVED">批准</el-radio>
            <el-radio label="REJECTED">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审批意见">
          <el-input type="textarea" v-model="approveForm.remark" :rows="4" placeholder="请输入审批意见"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitApprove">确定</el-button>
      </span>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog title="采购详情" :visible.sync="detailDialogVisible" width="700px">
      <el-descriptions :column="2" border v-if="currentRow">
        <el-descriptions-item label="采购单号">{{ currentRow.requestNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentRow.status)">{{ getStatusText(currentRow.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="试剂名称" :span="2">{{ currentRow.reagentName }}</el-descriptions-item>
        <el-descriptions-item label="数量">{{ currentRow.quantity }} {{ currentRow.unit }}</el-descriptions-item>
        <el-descriptions-item label="预估价格">{{ currentRow.estimatedPrice }} 元</el-descriptions-item>
        <el-descriptions-item label="供应商">{{ currentRow.supplier }}</el-descriptions-item>
        <el-descriptions-item label="紧急程度">
          <el-tag :type="getUrgencyType(currentRow.urgency)" size="small">
            {{ getUrgencyText(currentRow.urgency) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请人">{{ currentRow.requesterName }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentRow.createTime }}</el-descriptions-item>
        <el-descriptions-item label="采购原因" :span="2">{{ currentRow.reason }}</el-descriptions-item>
        <el-descriptions-item label="审批人" v-if="currentRow.approverName">{{ currentRow.approverName }}</el-descriptions-item>
        <el-descriptions-item label="审批时间" v-if="currentRow.approvalTime">{{ currentRow.approvalTime }}</el-descriptions-item>
        <el-descriptions-item label="审批意见" :span="2" v-if="currentRow.approvalRemark">{{ currentRow.approvalRemark }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'ProcurementManagement',
  data() {
    return {
      loading: false,
      list: [],
      searchNo: '',
      searchName: '',
      searchStatus: '',
      currentPage: 1,
      pageSize: 10,
      total: 0,
      dialogVisible: false,
      approveDialogVisible: false,
      detailDialogVisible: false,
      dialogTitle: '新建采购申请',
      currentRow: null,
      form: {
        reagentName: '',
        quantity: 1,
        unit: '瓶',
        estimatedPrice: 0,
        supplier: '',
        urgency: 'NORMAL',
        reason: ''
      },
      approveForm: {
        id: null,
        result: 'APPROVED',
        remark: ''
      },
      rules: {
        reagentName: [{ required: true, message: '请输入试剂名称', trigger: 'blur' }],
        quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }],
        unit: [{ required: true, message: '请输入单位', trigger: 'blur' }],
        estimatedPrice: [{ required: true, message: '请输入预估价格', trigger: 'blur' }],
        supplier: [{ required: true, message: '请输入供应商', trigger: 'blur' }],
        reason: [{ required: true, message: '请输入采购原因', trigger: 'blur' }]
      }
    }
  },
  mounted() {
    this.loadList()
  },
  methods: {
    async loadList() {
      this.loading = true
      setTimeout(() => {
        // 模拟数据
        this.list = [
          {
            id: 1,
            requestNo: 'PRO20240501001',
            reagentName: '乙醇',
            quantity: 20,
            unit: '瓶',
            estimatedPrice: 600.00,
            supplier: '国药集团',
            urgency: 'NORMAL',
            requesterName: '王小明',
            reason: '实验室常用溶剂即将用完',
            status: 'RECEIVED',
            approverName: '张教授',
            approvalTime: '2024-05-02 09:00:00',
            approvalRemark: '同意采购',
            createTime: '2024-05-01 14:00:00'
          },
          {
            id: 2,
            requestNo: 'PRO20240515001',
            reagentName: '硝酸银',
            quantity: 5,
            unit: '瓶',
            estimatedPrice: 1000.00,
            supplier: '上海试剂三厂',
            urgency: 'HIGH',
            requesterName: '刘小红',
            reason: '课题实验急需',
            status: 'ORDERED',
            approverName: '张教授',
            approvalTime: '2024-05-16 10:00:00',
            approvalRemark: '同意，尽快订购',
            createTime: '2024-05-15 16:00:00'
          },
          {
            id: 3,
            requestNo: 'PRO20240601001',
            reagentName: '葡萄糖',
            quantity: 10,
            unit: '瓶',
            estimatedPrice: 150.00,
            supplier: '国药集团',
            urgency: 'NORMAL',
            requesterName: '陈小华',
            reason: '生物实验补充试剂',
            status: 'PENDING',
            createTime: '2024-06-01 10:00:00'
          }
        ]
        this.total = this.list.length
        this.loading = false
      }, 500)
    },
    showAddDialog() {
      this.dialogTitle = '新建采购申请'
      this.form = {
        reagentName: '',
        quantity: 1,
        unit: '瓶',
        estimatedPrice: 0,
        supplier: '',
        urgency: 'NORMAL',
        reason: ''
      }
      this.dialogVisible = true
    },
    saveProcurement() {
      this.$refs.procurementForm.validate(valid => {
        if (valid) {
          // 调用后端API
          this.$message.success('采购申请提交成功')
          this.dialogVisible = false
          this.loadList()
        }
      })
    },
    approveProcurement(row) {
      this.approveForm = {
        id: row.id,
        result: 'APPROVED',
        remark: ''
      }
      this.approveDialogVisible = true
    },
    submitApprove() {
      // 调用后端API
      this.$message.success('审批成功')
      this.approveDialogVisible = false
      this.loadList()
    },
    orderProcurement(row) {
      this.$confirm('确认已向供应商下单？', '提示', {
        type: 'warning'
      }).then(() => {
        // 调用后端API
        row.status = 'ORDERED'
        this.$message.success('已标记为已订购')
      }).catch(() => {})
    },
    receiveProcurement(row) {
      this.$confirm('确认试剂已收货并入库？', '提示', {
        type: 'warning'
      }).then(() => {
        // 调用后端API
        row.status = 'RECEIVED'
        this.$message.success('已确认收货')
      }).catch(() => {})
    },
    viewDetail(row) {
      this.currentRow = row
      this.detailDialogVisible = true
    },
    resetSearch() {
      this.searchNo = ''
      this.searchName = ''
      this.searchStatus = ''
      this.loadList()
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.loadList()
    },
    handleCurrentChange(val) {
      this.currentPage = val
      this.loadList()
    },
    getStatusType(status) {
      const types = {
        'PENDING': 'warning',
        'APPROVED': 'success',
        'REJECTED': 'danger',
        'ORDERED': 'primary',
        'RECEIVED': 'info'
      }
      return types[status] || 'info'
    },
    getStatusText(status) {
      const texts = {
        'PENDING': '待审批',
        'APPROVED': '已批准',
        'REJECTED': '已拒绝',
        'ORDERED': '已订购',
        'RECEIVED': '已收货'
      }
      return texts[status] || status
    },
    getUrgencyType(urgency) {
      const types = {
        'NORMAL': '',
        'HIGH': 'warning',
        'URGENT': 'danger'
      }
      return types[urgency] || ''
    },
    getUrgencyText(urgency) {
      const texts = {
        'NORMAL': '普通',
        'HIGH': '紧急',
        'URGENT': '特急'
      }
      return texts[urgency] || urgency
    }
  }
}
</script>


