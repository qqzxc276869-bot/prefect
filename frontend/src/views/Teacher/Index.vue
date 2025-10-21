<template>
  <div class="container">
    <el-container style="height: 100%;">
      <!-- 头部 -->
      <el-header style="background: #67C23A; color: white;">
        <div style="display: flex; justify-content: space-between; align-items: center; height: 100%;">
          <h2>实验室化学试剂库存管理系统 - 老师/管理员端</h2>
          <div>
            <span style="margin-right: 20px;">欢迎，{{ userInfo.realName }}</span>
            <el-button size="small" type="danger" @click="handleLogout">退出登录</el-button>
          </div>
        </div>
      </el-header>
      
      <!-- 主体 -->
      <el-container>
        <!-- 侧边栏 -->
        <el-aside width="200px" style="background: #f5f5f5;">
          <el-menu :default-active="activeMenu" @select="handleMenuSelect">
            <el-menu-item index="inventory">
              <i class="el-icon-document"></i>
              <span>库存管理</span>
            </el-menu-item>
            <el-menu-item index="stockIn">
              <i class="el-icon-circle-plus"></i>
              <span>入库登记</span>
            </el-menu-item>
            <el-menu-item index="applications">
              <i class="el-icon-s-order"></i>
              <span>申请审批</span>
            </el-menu-item>
            <el-menu-item index="stockOut">
              <i class="el-icon-bottom"></i>
              <span>出库管理</span>
            </el-menu-item>
            <el-menu-item index="warning">
              <i class="el-icon-warning"></i>
              <span>库存预警</span>
            </el-menu-item>
            <el-menu-item index="records">
              <i class="el-icon-tickets"></i>
              <span>出入库记录</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        
        <!-- 内容区 -->
        <el-main>
          <!-- 库存管理 -->
          <div v-show="activeMenu === 'inventory'">
            <el-card>
              <div slot="header">
                <span>库存管理</span>
                <el-input
                  v-model="searchName"
                  placeholder="输入试剂名称搜索"
                  style="width: 300px; float: right;"
                  @change="loadInventory"
                  clearable
                >
                  <el-button slot="append" icon="el-icon-search" @click="loadInventory"></el-button>
                </el-input>
              </div>
              <el-table :data="inventoryList" border>
                <el-table-column prop="reagentName" label="试剂名称" width="130"></el-table-column>
                <el-table-column prop="specification" label="规格" width="100"></el-table-column>
                <el-table-column prop="batchNo" label="批次号" width="120"></el-table-column>
                <el-table-column prop="quantity" label="库存数量" width="90"></el-table-column>
                <el-table-column prop="warningThreshold" label="预警阈值" width="90"></el-table-column>
                <el-table-column prop="locationName" label="存放位置" width="150"></el-table-column>
                <el-table-column prop="expiryDate" label="有效期" width="110"></el-table-column>
                <el-table-column prop="status" label="状态" width="90">
                  <template slot-scope="scope">
                    <el-tag v-if="scope.row.status === 'NORMAL'" type="success">正常</el-tag>
                    <el-tag v-else-if="scope.row.status === 'LOW'" type="warning">库存不足</el-tag>
                    <el-tag v-else-if="scope.row.status === 'EXPIRING'" type="warning">即将过期</el-tag>
                    <el-tag v-else type="danger">已过期</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="100">
                  <template slot-scope="scope">
                    <el-button size="mini" @click="showThresholdDialog(scope.row)">设置预警</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </div>
          
          <!-- 入库登记 -->
          <div v-show="activeMenu === 'stockIn'">
            <el-card>
              <div slot="header">入库登记</div>
              <el-form :model="stockInForm" ref="stockInForm" label-width="120px">
                <el-form-item label="试剂" required>
                  <el-select v-model="stockInForm.reagentId" placeholder="请选择试剂" style="width: 100%;">
                    <el-option
                      v-for="item in reagentList"
                      :key="item.id"
                      :label="item.name"
                      :value="item.id"
                    ></el-option>
                  </el-select>
                </el-form-item>
                <el-form-item label="批次号">
                  <el-input v-model="stockInForm.batchNo"></el-input>
                </el-form-item>
                <el-form-item label="入库数量" required>
                  <el-input-number v-model="stockInForm.quantity" :min="1" :max="99999"></el-input-number>
                </el-form-item>
                <el-form-item label="有效期">
                  <el-date-picker v-model="stockInForm.expiryDate" type="date" value-format="yyyy-MM-dd"></el-date-picker>
                </el-form-item>
                <el-form-item label="供应商">
                  <el-input v-model="stockInForm.supplier"></el-input>
                </el-form-item>
                <el-form-item label="采购单价">
                  <el-input-number v-model="stockInForm.purchasePrice" :min="0" :precision="2"></el-input-number>
                </el-form-item>
                <el-form-item label="备注">
                  <el-input type="textarea" v-model="stockInForm.remark"></el-input>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="submitStockIn">确认入库</el-button>
                  <el-button @click="resetStockInForm">重置</el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </div>
          
          <!-- 申请审批 -->
          <div v-show="activeMenu === 'applications'">
            <el-card>
              <div slot="header">申请审批</div>
              <el-table :data="pendingList" border>
                <el-table-column prop="applicationNo" label="申请单号" width="180"></el-table-column>
                <el-table-column prop="reagentName" label="试剂名称" width="150"></el-table-column>
                <el-table-column prop="quantity" label="申请数量" width="100"></el-table-column>
                <el-table-column prop="applicantName" label="申请人" width="100"></el-table-column>
                <el-table-column prop="purpose" label="用途" min-width="200" show-overflow-tooltip></el-table-column>
                <el-table-column prop="createTime" label="申请时间" width="180"></el-table-column>
                <el-table-column label="操作" width="200">
                  <template slot-scope="scope">
                    <el-button size="mini" type="success" @click="handleReview(scope.row, 'APPROVED')">通过</el-button>
                    <el-button size="mini" type="danger" @click="handleReview(scope.row, 'REJECTED')">拒绝</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </div>
          
          <!-- 出库管理 -->
          <div v-show="activeMenu === 'stockOut'">
            <el-card>
              <div slot="header">出库管理</div>
              <el-form :model="stockOutForm" ref="stockOutForm" label-width="120px">
                <el-form-item label="关联申请单">
                  <el-select v-model="stockOutForm.applicationId" placeholder="选择申请单（可选）" clearable style="width: 100%;">
                    <el-option
                      v-for="item in approvedList"
                      :key="item.id"
                      :label="`${item.applicationNo} - ${item.reagentName} - ${item.applicantName}`"
                      :value="item.id"
                      @click.native="selectApplication(item)"
                    ></el-option>
                  </el-select>
                </el-form-item>
                <el-form-item label="库存" required>
                  <el-select v-model="stockOutForm.inventoryId" placeholder="请选择库存" style="width: 100%;">
                    <el-option
                      v-for="item in inventoryList"
                      :key="item.id"
                      :label="`${item.reagentName} - ${item.batchNo} - 库存：${item.quantity}`"
                      :value="item.id"
                    ></el-option>
                  </el-select>
                </el-form-item>
                <el-form-item label="出库数量" required>
                  <el-input-number v-model="stockOutForm.quantity" :min="1" :max="99999"></el-input-number>
                </el-form-item>
                <el-form-item label="领用人" required>
                  <el-input v-model="stockOutForm.recipientName"></el-input>
                </el-form-item>
                <el-form-item label="用途">
                  <el-input type="textarea" v-model="stockOutForm.purpose"></el-input>
                </el-form-item>
                <el-form-item label="备注">
                  <el-input type="textarea" v-model="stockOutForm.remark"></el-input>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="submitStockOut">确认出库</el-button>
                  <el-button @click="resetStockOutForm">重置</el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </div>
          
          <!-- 库存预警 -->
          <div v-show="activeMenu === 'warning'">
            <el-card>
              <div slot="header">库存预警</div>
              <el-table :data="warningList" border>
                <el-table-column prop="reagentName" label="试剂名称" width="150"></el-table-column>
                <el-table-column prop="specification" label="规格" width="120"></el-table-column>
                <el-table-column prop="quantity" label="当前库存" width="100"></el-table-column>
                <el-table-column prop="warningThreshold" label="预警阈值" width="100"></el-table-column>
                <el-table-column prop="expiryDate" label="有效期" width="120"></el-table-column>
                <el-table-column prop="locationName" label="存放位置" width="180"></el-table-column>
                <el-table-column prop="status" label="状态" width="100">
                  <template slot-scope="scope">
                    <el-tag v-if="scope.row.status === 'LOW'" type="warning">库存不足</el-tag>
                    <el-tag v-else-if="scope.row.status === 'EXPIRING'" type="warning">即将过期</el-tag>
                    <el-tag v-else type="danger">已过期</el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </div>
          
          <!-- 出入库记录 -->
          <div v-show="activeMenu === 'records'">
            <el-card>
              <div slot="header">出入库记录</div>
              <el-tabs v-model="recordTab">
                <el-tab-pane label="入库记录" name="in">
                  <el-table :data="stockInRecords" border>
                    <el-table-column prop="batchNo" label="批次号" width="150"></el-table-column>
                    <el-table-column prop="quantity" label="入库数量" width="100"></el-table-column>
                    <el-table-column prop="supplier" label="供应商" width="180"></el-table-column>
                    <el-table-column prop="operatorName" label="操作人" width="100"></el-table-column>
                    <el-table-column prop="createTime" label="入库时间" width="180"></el-table-column>
                    <el-table-column prop="remark" label="备注" min-width="200"></el-table-column>
                  </el-table>
                </el-tab-pane>
                <el-tab-pane label="出库记录" name="out">
                  <el-table :data="stockOutRecords" border>
                    <el-table-column prop="quantity" label="出库数量" width="100"></el-table-column>
                    <el-table-column prop="recipientName" label="领用人" width="100"></el-table-column>
                    <el-table-column prop="operatorName" label="操作人" width="100"></el-table-column>
                    <el-table-column prop="purpose" label="用途" min-width="200"></el-table-column>
                    <el-table-column prop="createTime" label="出库时间" width="180"></el-table-column>
                    <el-table-column prop="remark" label="备注" min-width="150"></el-table-column>
                  </el-table>
                </el-tab-pane>
              </el-tabs>
            </el-card>
          </div>
        </el-main>
      </el-container>
    </el-container>
    
    <!-- 设置预警阈值对话框 -->
    <el-dialog title="设置预警阈值" :visible.sync="thresholdDialogVisible" width="400px">
      <el-form :model="thresholdForm" label-width="100px">
        <el-form-item label="试剂名称">
          <span>{{ thresholdForm.reagentName }}</span>
        </el-form-item>
        <el-form-item label="预警阈值">
          <el-input-number v-model="thresholdForm.warningThreshold" :min="0" :max="99999"></el-input-number>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="thresholdDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="updateThreshold">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getInventoryList, getWarningList, updateThreshold } from '@/api/inventory'
import { getPendingApplications, getAllApplications, reviewApplication } from '@/api/application'
import { stockIn, stockOut, getStockInList, getStockOutList } from '@/api/stock'
import { getReagentList } from '@/api/reagent'

export default {
  name: 'TeacherIndex',
  data() {
    return {
      activeMenu: 'inventory',
      userInfo: this.$store.state.userInfo,
      searchName: '',
      inventoryList: [],
      reagentList: [],
      pendingList: [],
      approvedList: [],
      warningList: [],
      stockInRecords: [],
      stockOutRecords: [],
      recordTab: 'in',
      stockInForm: {
        reagentId: null,
        batchNo: '',
        quantity: 1,
        expiryDate: '',
        supplier: '',
        purchasePrice: 0,
        remark: ''
      },
      stockOutForm: {
        applicationId: null,
        inventoryId: null,
        quantity: 1,
        recipientId: null,
        recipientName: '',
        purpose: '',
        remark: ''
      },
      thresholdDialogVisible: false,
      thresholdForm: {
        id: null,
        reagentName: '',
        warningThreshold: 10
      }
    }
  },
  mounted() {
    this.loadInventory()
    this.loadReagents()
  },
  methods: {
    handleMenuSelect(index) {
      this.activeMenu = index
      if (index === 'applications') {
        this.loadPendingApplications()
      } else if (index === 'warning') {
        this.loadWarningList()
      } else if (index === 'records') {
        this.loadStockRecords()
      } else if (index === 'stockOut') {
        this.loadApprovedApplications()
      }
    },
    loadInventory() {
      getInventoryList({ name: this.searchName }).then(res => {
        this.inventoryList = res.data
      })
    },
    loadReagents() {
      getReagentList().then(res => {
        this.reagentList = res.data
      })
    },
    loadPendingApplications() {
      getPendingApplications().then(res => {
        this.pendingList = res.data
      })
    },
    loadApprovedApplications() {
      getAllApplications().then(res => {
        this.approvedList = res.data.filter(item => item.status === 'APPROVED')
      })
    },
    loadWarningList() {
      getWarningList().then(res => {
        this.warningList = res.data
      })
    },
    loadStockRecords() {
      getStockInList({ page: 1, size: 100 }).then(res => {
        this.stockInRecords = res.data.records || []
      })
      getStockOutList({ page: 1, size: 100 }).then(res => {
        this.stockOutRecords = res.data.records || []
      })
    },
    submitStockIn() {
      stockIn(this.stockInForm).then(() => {
        this.$message.success('入库成功')
        this.resetStockInForm()
        this.loadInventory()
      })
    },
    resetStockInForm() {
      this.stockInForm = {
        reagentId: null,
        batchNo: '',
        quantity: 1,
        expiryDate: '',
        supplier: '',
        purchasePrice: 0,
        remark: ''
      }
    },
    selectApplication(app) {
      this.stockOutForm.recipientName = app.applicantName
      this.stockOutForm.recipientId = app.applicantId
      this.stockOutForm.quantity = app.quantity
      this.stockOutForm.purpose = app.purpose
    },
    submitStockOut() {
      stockOut(this.stockOutForm).then(() => {
        this.$message.success('出库成功')
        this.resetStockOutForm()
        this.loadInventory()
      })
    },
    resetStockOutForm() {
      this.stockOutForm = {
        applicationId: null,
        inventoryId: null,
        quantity: 1,
        recipientId: null,
        recipientName: '',
        purpose: '',
        remark: ''
      }
    },
    handleReview(row, status) {
      const message = status === 'APPROVED' ? '确定通过该申请吗？' : '确定拒绝该申请吗？'
      this.$prompt(message, '审批备注', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPlaceholder: '请输入审批备注（选填）'
      }).then(({ value }) => {
        reviewApplication(row.id, {
          status: status,
          remark: value || ''
        }).then(() => {
          this.$message.success('审批成功')
          this.loadPendingApplications()
        })
      })
    },
    showThresholdDialog(row) {
      this.thresholdForm = {
        id: row.id,
        reagentName: row.reagentName,
        warningThreshold: row.warningThreshold
      }
      this.thresholdDialogVisible = true
    },
    updateThreshold() {
      updateThreshold(this.thresholdForm.id, {
        warningThreshold: this.thresholdForm.warningThreshold
      }).then(() => {
        this.$message.success('设置成功')
        this.thresholdDialogVisible = false
        this.loadInventory()
      })
    },
    handleLogout() {
      this.$confirm('确定要退出登录吗？', '提示', {
        type: 'warning'
      }).then(() => {
        this.$store.dispatch('logout')
        this.$router.push('/login')
      })
    }
  }
}
</script>

<style scoped>
.el-header {
  line-height: 60px;
  padding: 0 20px;
}

.el-aside {
  height: calc(100vh - 60px);
}

.el-menu {
  border-right: none;
}
</style>






