<template>
  <div class="container">
    <el-container style="height: 100%;">
      <!-- 头部 -->
      <el-header>
        <div class="header-content">
          <h2 class="header-title">实验室化学试剂库存管理系统 - 老师/管理员端</h2>
          <div class="header-right">
            <span class="user-info">欢迎，{{ userInfo.realName }}</span>
            <el-button size="small" type="danger" plain @click="handleLogout">退出登录</el-button>
          </div>
        </div>
      </el-header>
      
      <!-- 主体 -->
      <el-container>
        <!-- 侧边栏 -->
        <el-aside width="240px">
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
            <i class="el-icon-sold-out"></i>
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
            <el-menu-item index="announcements">
              <i class="el-icon-bell"></i>
              <span>系统公告</span>
            </el-menu-item>
            <el-menu-item index="feedback">
              <i class="el-icon-chat-line-square"></i>
              <span>反馈处理</span>
            </el-menu-item>
            <el-menu-item index="ai-assistant">
              <i class="el-icon-chat-dot-round"></i>
              <span>AI智能助手</span>
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
              <div slot="header">
                入库登记
                <el-button size="mini" style="float:right;" @click="handleStockInHint">AI智能填充</el-button>
              </div>
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
                <el-form-item label="存放位置" required>
                  <el-select v-model="stockInForm.locationId" placeholder="请选择存放位置" style="width: 100%;">
                    <el-option
                      v-for="item in locationList"
                      :key="item.id"
                      :label="item.fullLocation"
                      :value="item.id"
                    ></el-option>
                  </el-select>
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
                <el-table-column label="操作" width="320">
                  <template slot-scope="scope">
                    <el-button size="mini" @click="handlePrecheck(scope.row)">AI预审</el-button>
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
            <el-card style="margin-bottom: 20px;">
              <div slot="header">
                <span>库存预警</span>
                <el-button size="small" type="primary" style="float: right;" @click="exportInventoryData">导出库存清单</el-button>
              </div>
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
            
            <el-card v-if="replenishSuggestions.length > 0">
              <div slot="header">
                <i class="el-icon-magic-stick"></i> AI补货建议
              </div>
              <el-table :data="replenishSuggestions" border>
                <el-table-column prop="reagentName" label="试剂名称" width="150"></el-table-column>
                <el-table-column prop="currentStock" label="当前库存" width="100">
                  <template slot-scope="scope">
                    {{ scope.row.currentStock }} {{ scope.row.unit }}
                  </template>
                </el-table-column>
                <el-table-column prop="avgDailyConsumption" label="日均消耗" width="100">
                  <template slot-scope="scope">
                    {{ scope.row.avgDailyConsumption }} {{ scope.row.unit }}
                  </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="100">
                  <template slot-scope="scope">
                    <el-tag v-if="scope.row.status === 'LOW'" type="warning">库存不足</el-tag>
                    <el-tag v-else-if="scope.row.status === 'EXPIRING'" type="warning">即将过期</el-tag>
                    <el-tag v-else type="danger">已过期</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="aiSuggestion" label="AI建议" min-width="300"></el-table-column>
              </el-table>
            </el-card>
          </div>
          
          <!-- 出入库记录 -->
          <div v-show="activeMenu === 'records'">
            <el-card>
              <div slot="header">
                <span>出入库记录</span>
                <el-button size="small" type="primary" style="float: right; margin-left: 10px;" @click="exportStockOutData">导出出库记录</el-button>
                <el-button size="small" type="success" style="float: right;" @click="exportStockInData">导出入库记录</el-button>
              </div>
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

          <!-- 系统公告 -->
          <div v-show="activeMenu === 'announcements'">
            <AnnouncementList :can-publish="false" />
          </div>

          <!-- 反馈处理 -->
          <div v-show="activeMenu === 'feedback'">
            <el-card>
              <div slot="header">
                <i class="el-icon-chat-line-square"></i> 待处理反馈
              </div>
              <el-table :data="feedbackList" border>
                <el-table-column prop="feedbackType" label="类型" width="100">
                  <template slot-scope="scope">
                    <el-tag v-if="scope.row.feedbackType === 'REAGENT'" type="warning">试剂问题</el-tag>
                    <el-tag v-else-if="scope.row.feedbackType === 'SYSTEM'" type="danger">系统问题</el-tag>
                    <el-tag v-else type="info">建议</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="userName" label="提交人" width="100"></el-table-column>
                <el-table-column prop="title" label="标题" width="150" show-overflow-tooltip></el-table-column>
                <el-table-column prop="content" label="内容" min-width="250" show-overflow-tooltip></el-table-column>
                <el-table-column prop="createTime" label="提交时间" width="180"></el-table-column>
                <el-table-column label="操作" width="200">
                  <template slot-scope="scope">
                    <el-button size="mini" type="success" @click="handleFeedbackItem(scope.row, 'RESOLVED')">已解决</el-button>
                    <el-button size="mini" type="warning" @click="handleFeedbackItem(scope.row, 'PROCESSING')">处理中</el-button>
                  </template>
                </el-table-column>
              </el-table>
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
import { stockInHint, approvePrecheck } from '@/api/ai'
import { getLocationList } from '@/api/base'
import { getAnnouncements } from '@/api/announcement'
import { getPendingFeedback, handleFeedback } from '@/api/feedback'
import { exportInventory, exportStockIn, exportStockOut, getFifoSuggestion, getReplenishSuggestions } from '@/api/export'
import AnnouncementList from '@/components/AnnouncementList.vue'

export default {
  name: 'TeacherIndex',
  components: {
    AnnouncementList
  },
  data() {
    return {
      activeMenu: 'inventory',
      userInfo: this.$store.state.userInfo,
      searchName: '',
      inventoryList: [],
      reagentList: [],
      locationList: [],
      pendingList: [],
      approvedList: [],
      warningList: [],
      announcementList: [],
      feedbackList: [],
      stockInRecords: [],
      stockOutRecords: [],
      recordTab: 'in',
      replenishSuggestions: [],
      stockInForm: {
        reagentId: null,
        batchNo: '',
        quantity: 1,
        expiryDate: '',
        supplier: '',
        purchasePrice: 0,
        locationId: null,
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
    this.loadLocations()
    this.loadAnnouncements()
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
      } else if (index === 'announcements') {
        this.loadAnnouncements()
      } else if (index === 'feedback') {
        this.loadFeedback()
      } else if (index === 'ai-assistant') {
        this.$router.push('/ai-assistant')
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
    loadLocations() {
      getLocationList().then(res => {
        this.locationList = res.data
      })
    },
    loadAnnouncements() {
      getAnnouncements({ role: 'TEACHER' }).then(res => {
        this.announcementList = res.data || []
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
      // 加载AI补货建议
      this.loadReplenishSuggestions()
    },
    loadReplenishSuggestions() {
      getReplenishSuggestions({ model: 'qwen-plus-2025-07-28' }).then(res => {
        this.replenishSuggestions = res.data || []
      }).catch(() => {
        this.replenishSuggestions = []
      })
    },
    loadFeedback() {
      getPendingFeedback().then(res => {
        this.feedbackList = res.data || []
      })
    },
    handleFeedbackItem(row, status) {
      this.$prompt('请输入处理备注', '处理反馈', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPlaceholder: '请输入处理备注'
      }).then(({ value }) => {
        handleFeedback(row.id, {
          status: status,
          remark: value || ''
        }).then(() => {
          this.$message.success('处理成功')
          this.loadFeedback()
        })
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
    handleStockInHint() {
      const reagent = this.reagentList.find(x => x.id === this.stockInForm.reagentId)
      const name = reagent ? reagent.name : ''
      const unit = reagent ? (reagent.unit || '') : ''
      stockInHint({ name, batchNo: this.stockInForm.batchNo, quantity: this.stockInForm.quantity, unit, model: 'qwen-plus-2025-07-28' })
        .then(res => {
          const d = res.data || {}
          const tips = []
          if (d.casNo) tips.push('CAS号：' + d.casNo)
          if (d.specification) tips.push('建议规格：' + d.specification)
          if (d.dangerLevel) tips.push('危险等级：' + d.dangerLevel)
          if (d.defaultExpiryMonths) tips.push('建议有效期（月）：' + d.defaultExpiryMonths)
          if (d.recommendedLocation) tips.push('位置建议：' + d.recommendedLocation)
          if (d.batchUnique === false) tips.push('⚠️ 警告：批次号已存在，建议更换')
          if (d.reasoning) tips.push('\n推理：' + d.reasoning)
          
          // 使用$confirm代替$alert，提供更明确的应用/取消选项
          this.$confirm(tips.join('\n') || 'AI暂无建议', 'AI入库提示', {
            confirmButtonText: '应用建议',
            cancelButtonText: '取消',
            type: 'info',
            distinguishCancelAndClose: true
          }).then(() => {
            // 点击"应用建议"
            let applied = []
            
            // 应用有效期
            if (!this.stockInForm.expiryDate && d.defaultExpiryMonths) {
              const date = new Date()
              date.setMonth(date.getMonth() + Number(d.defaultExpiryMonths))
              const y = date.getFullYear()
              const m = String(date.getMonth() + 1).padStart(2, '0')
              const da = String(date.getDate()).padStart(2, '0')
              this.stockInForm.expiryDate = `${y}-${m}-${da}`
              applied.push('有效期')
            }
            
            // 应用备注（追加推理信息）
            if (d.reasoning && !this.stockInForm.remark) {
              this.stockInForm.remark = 'AI建议：' + d.reasoning
              applied.push('备注')
            }
            
            // 批次警告
            if (d.batchUnique === false) {
              this.$message.warning('⚠️ 当前批次号已存在，请更换批次号')
            }
            
            // 显示应用结果
            if (applied.length > 0) {
              this.$message.success('已应用：' + applied.join('、'))
            } else {
              this.$message.info('暂无可应用的建议（相关字段已填写）')
            }
          }).catch(action => {
            // 点击"取消"或关闭
            if (action === 'cancel') {
              this.$message.info('已取消应用建议')
            }
          })
        })
        .catch(err => {
          this.$message.error('AI入库提示失败：' + (err.message || ''))
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
        locationId: null,
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
      if (!this.stockOutForm.inventoryId) {
        this.$message.warning('请选择库存')
        return
      }
      
      // 获取选中的库存信息
      const selectedInventory = this.inventoryList.find(x => x.id === this.stockOutForm.inventoryId)
      if (selectedInventory && selectedInventory.reagentId) {
        // 获取FIFO建议
        getFifoSuggestion(selectedInventory.reagentId).then(res => {
          const suggestion = res.data || {}
          if (suggestion.hasSuggestion && suggestion.inventoryId !== this.stockOutForm.inventoryId) {
            // 显示FIFO提示
            this.$confirm(suggestion.message + '\n\n是否继续当前出库操作？', 'FIFO提示', {
              confirmButtonText: '继续出库',
              cancelButtonText: '取消',
              type: 'warning'
            }).then(() => {
              this.doStockOut()
            })
          } else {
            this.doStockOut()
          }
        }).catch(() => {
          this.doStockOut()
        })
      } else {
        this.doStockOut()
      }
    },
    doStockOut() {
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
    handlePrecheck(row) {
      approvePrecheck({ applicationId: row.id, model: 'qwen-plus-2025-07-28' }).then(res => {
        const d = res.data || {}
        const tips = []
        tips.push('库存是否充足：' + (d.stockEnough ? '是' : '否'))
        if (d.fifoSuggestion) tips.push('FIFO建议：' + d.fifoSuggestion)
        if (d.substitutes && d.substitutes.length) tips.push('替代建议：' + d.substitutes.join('；'))
        if (d.cautions && d.cautions.length) tips.push('注意事项：' + d.cautions.join('；'))
        if (d.summary) tips.push('摘要：' + d.summary)
        this.$alert(tips.join('\n'), 'AI预审结果', { confirmButtonText: '确定' })
      }).catch(err => {
        this.$message.error('AI预审失败：' + (err.message || ''))
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
    getAudienceLabel(value) {
      const map = {
        ALL: '全部人员',
        TEACHER: '老师',
        STUDENT: '学生'
      }
      return map[value] || '全部人员'
    },
    getPriorityLabel(value) {
      const map = {
        INFO: '普通提醒',
        WARN: '重要通知',
        URGENT: '紧急通知'
      }
      return map[value] || '普通提醒'
    },
    getPriorityTag(value) {
      const map = {
        INFO: 'info',
        WARN: 'warning',
        URGENT: 'danger'
      }
      return map[value] || 'info'
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
    exportInventoryData() {
      exportInventory().then(res => {
        const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = '库存清单.xlsx'
        link.click()
        window.URL.revokeObjectURL(url)
        this.$message.success('导出成功')
      }).catch(() => {
        this.$message.error('导出失败')
      })
    },
    exportStockInData() {
      exportStockIn().then(res => {
        const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = '入库记录.xlsx'
        link.click()
        window.URL.revokeObjectURL(url)
        this.$message.success('导出成功')
      }).catch(() => {
        this.$message.error('导出失败')
      })
    },
    exportStockOutData() {
      exportStockOut().then(res => {
        const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = '出库记录.xlsx'
        link.click()
        window.URL.revokeObjectURL(url)
        this.$message.success('导出成功')
      }).catch(() => {
        this.$message.error('导出失败')
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
  background: white;
  z-index: 10;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
  width: 100%;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  margin-right: 20px;
  font-size: 14px;
  color: #64748b;
}

.el-aside {
  background: white;
  border-right: 1px solid #e5e7eb;
}

.el-main {
  background-color: #f8fafc;
}

.el-card {
  margin-bottom: 24px;
  transition: transform 0.2s;
}

.el-card:hover {
  transform: translateY(-2px);
}
</style>







