<template>
  <div class="container">
    <el-container style="height: 100%;">
      <el-header>
        <div class="header-content">
          <h2 class="header-title">实验室化学试剂库存管理系统 - 学生端</h2>
          <div class="header-right">
            <span class="user-info">欢迎，{{ userInfo.realName }}</span>
            <el-button size="small" type="danger" plain @click="handleLogout">退出登录</el-button>
          </div>
        </div>
      </el-header>

      <el-container>
        <el-aside width="240px">
          <el-menu :default-active="activeMenu" @select="handleMenuSelect">
            <el-menu-item index="inventory">
              <i class="el-icon-document"></i>
              <span>库存查询</span>
            </el-menu-item>
            <el-menu-item index="apply">
              <i class="el-icon-edit"></i>
              <span>试剂申领</span>
            </el-menu-item>
            <el-menu-item index="myApplications">
              <i class="el-icon-tickets"></i>
              <span>我的申请</span>
            </el-menu-item>
            <el-menu-item index="announcements">
              <i class="el-icon-bell"></i>
              <span>系统公告</span>
            </el-menu-item>
            <el-menu-item index="feedback">
              <i class="el-icon-chat-line-square"></i>
              <span>问题反馈</span>
            </el-menu-item>
            <el-menu-item index="ai-assistant">
              <i class="el-icon-chat-dot-round"></i>
              <span>AI智能助手</span>
            </el-menu-item>
          </el-menu>
        </el-aside>

        <el-main>
          <div v-show="activeMenu === 'inventory'">
            <el-card>
              <div slot="header">
                <span>库存查询</span>
                <el-button size="mini" type="primary" style="float: right; margin-left: 10px;" @click="handleSemanticSearch">AI智能搜</el-button>
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
                <el-table-column prop="reagentName" label="试剂名称" width="150"></el-table-column>
                <el-table-column prop="specification" label="规格型号" width="120"></el-table-column>
                <el-table-column prop="categoryName" label="分类" width="100"></el-table-column>
                <el-table-column prop="quantity" label="库存数量" width="100"></el-table-column>
                <el-table-column prop="unit" label="单位" width="80"></el-table-column>
                <el-table-column prop="locationName" label="存放位置" width="180"></el-table-column>
                <el-table-column prop="expiryDate" label="有效期" width="120"></el-table-column>
                <el-table-column prop="status" label="状态" width="100">
                  <template slot-scope="scope">
                    <el-tag v-if="scope.row.status === 'NORMAL'" type="success">正常</el-tag>
                    <el-tag v-else-if="scope.row.status === 'LOW'" type="warning">库存不足</el-tag>
                    <el-tag v-else-if="scope.row.status === 'EXPIRING'" type="warning">即将过期</el-tag>
                    <el-tag v-else type="danger">已过期</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="100">
                  <template slot-scope="scope">
                    <el-button size="mini" type="primary" @click="showApplyDialog(scope.row)">申领</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </div>

          <div v-show="activeMenu === 'apply'">
            <el-card>
              <div slot="header">
                试剂申领
                <el-button size="mini" style="float: right;" @click="handleApplyOptimize">AI优化</el-button>
              </div>
              <el-form :model="applyForm" :rules="applyRules" ref="applyForm" label-width="100px">
                <el-form-item label="试剂名称" prop="reagentId">
                  <el-select v-model="applyForm.reagentId" placeholder="请选择试剂" style="width: 100%;">
                    <el-option
                        v-for="item in inventoryList"
                        :key="item.reagentId"
                        :label="item.reagentName"
                        :value="item.reagentId"
                    ></el-option>
                  </el-select>
                </el-form-item>
                <el-form-item label="申请数量" prop="quantity">
                  <el-input-number
                      v-model="applyForm.quantity"
                      :min="0.01"
                      :max="9999"
                      :precision="2"
                      :step="0.1"
                  ></el-input-number>
                </el-form-item>
                <el-form-item label="用途说明" prop="purpose">
                  <el-input type="textarea" v-model="applyForm.purpose" :rows="5"></el-input>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="submitApply">提交申请</el-button>
                  <el-button @click="resetApplyForm">重置</el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </div>

          <div v-show="activeMenu === 'myApplications'">
            <el-card>
              <div slot="header">我的申请</div>
              <el-table :data="myApplicationList" border>
                <el-table-column prop="applicationNo" label="申请单号" width="180"></el-table-column>
                <el-table-column prop="reagentName" label="试剂名称" width="150"></el-table-column>
                <el-table-column prop="quantity" label="申请数量" width="100"></el-table-column>
                <el-table-column prop="purpose" label="用途" min-width="200" show-overflow-tooltip></el-table-column>
                <el-table-column prop="createTime" label="申请时间" width="180"></el-table-column>
                <el-table-column prop="status" label="状态" width="100">
                  <template slot-scope="scope">
                    <el-tag v-if="scope.row.status === 'PENDING'" type="warning">待审核</el-tag>
                    <el-tag v-else-if="scope.row.status === 'APPROVED'" type="success">已通过</el-tag>
                    <el-tag v-else-if="scope.row.status === 'REJECTED'" type="danger">已拒绝</el-tag>
                    <el-tag v-else type="info">已完成</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="reviewRemark" label="审核备注" min-width="150" show-overflow-tooltip></el-table-column>
              </el-table>
            </el-card>
          </div>

          <div v-show="activeMenu === 'announcements'">
            <AnnouncementList :can-publish="false" />
          </div>

          <div v-show="activeMenu === 'feedback'">
            <el-row :gutter="20">
              <el-col :span="10">
                <el-card>
                  <div slot="header">
                    <i class="el-icon-edit"></i> 提交反馈
                  </div>
                  <el-form :model="feedbackForm" :rules="feedbackRules" ref="feedbackForm" label-width="100px">
                    <el-form-item label="反馈类型" prop="feedbackType">
                      <el-select v-model="feedbackForm.feedbackType" style="width: 100%;">
                        <el-option label="试剂问题" value="REAGENT"></el-option>
                        <el-option label="系统问题" value="SYSTEM"></el-option>
                        <el-option label="建议" value="SUGGESTION"></el-option>
                      </el-select>
                    </el-form-item>
                    <el-form-item label="标题" prop="title">
                      <el-input v-model="feedbackForm.title" placeholder="请输入标题"></el-input>
                    </el-form-item>
                    <el-form-item label="内容" prop="content">
                      <el-input type="textarea" :rows="6" v-model="feedbackForm.content" placeholder="请详细描述问题或建议"></el-input>
                    </el-form-item>
                    <el-form-item>
                      <el-button type="primary" @click="submitFeedback">提交反馈</el-button>
                      <el-button @click="resetFeedbackForm">重置</el-button>
                    </el-form-item>
                  </el-form>
                </el-card>
              </el-col>
              <el-col :span="14">
                <el-card>
                  <div slot="header">
                    <i class="el-icon-tickets"></i> 我的反馈记录
                  </div>
                  <el-table :data="myFeedbackList" border max-height="500">
                    <el-table-column prop="feedbackType" label="类型" width="100">
                      <template slot-scope="scope">
                        <el-tag v-if="scope.row.feedbackType === 'REAGENT'" type="warning">试剂问题</el-tag>
                        <el-tag v-else-if="scope.row.feedbackType === 'SYSTEM'" type="danger">系统问题</el-tag>
                        <el-tag v-else type="info">建议</el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column prop="title" label="标题" width="150" show-overflow-tooltip></el-table-column>
                    <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip></el-table-column>
                    <el-table-column prop="status" label="状态" width="100">
                      <template slot-scope="scope">
                        <el-tag v-if="scope.row.status === 'PENDING'" type="warning">待处理</el-tag>
                        <el-tag v-else-if="scope.row.status === 'PROCESSING'" type="primary">处理中</el-tag>
                        <el-tag v-else-if="scope.row.status === 'RESOLVED'" type="success">已解决</el-tag>
                        <el-tag v-else type="info">已关闭</el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column prop="handleRemark" label="处理备注" width="150" show-overflow-tooltip></el-table-column>
                    <el-table-column prop="createTime" label="提交时间" width="180"></el-table-column>
                  </el-table>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </el-main>
      </el-container>
    </el-container>

    <el-dialog title="AI 申领优化建议" :visible.sync="aiOptDialogVisible" width="600px" append-to-body>
      <div v-if="aiOptData">
        <el-form label-position="top" size="small">
          <el-row :gutter="20">
            <el-col :span="12" v-if="aiOptData.standardizedName">
              <el-form-item label="标准名称">
                <el-tag type="info">{{ aiOptData.standardizedName }}</el-tag>
              </el-form-item>
            </el-col>
            <el-col :span="12" v-if="aiOptData.casNo">
              <el-form-item label="CAS号">
                <el-tag type="info">{{ aiOptData.casNo }}</el-tag>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="建议申请数量" v-if="aiOptData.suggestedQuantity">
            <span style="font-weight: bold; color: #409EFF; font-size: 16px;">
              {{ aiOptData.suggestedQuantity }} {{ aiOptData.unit }}
            </span>
          </el-form-item>

          <el-form-item label="请选择用途说明模板" v-if="aiOptData.purposeTemplates && aiOptData.purposeTemplates.length">
            <el-radio-group v-model="selectedPurposeTemplate" style="display: flex; flex-direction: column; gap: 10px; width: 100%;">
              <el-radio
                  v-for="(item, index) in aiOptData.purposeTemplates"
                  :key="index"
                  :label="item"
                  border
                  style="margin-left: 0; width: 100%; white-space: normal; height: auto; padding: 10px; line-height: 1.5;">
                {{ item }}
              </el-radio>
            </el-radio-group>
          </el-form-item>
          <div v-else class="el-upload__tip">暂无用途建议</div>

          <el-alert
              v-if="aiOptData.warnings && aiOptData.warnings.length"
              title="安全合规提示"
              type="warning"
              :closable="false"
              show-icon
              style="margin-top: 15px;">
            <div v-for="(w, i) in aiOptData.warnings" :key="i" style="margin-top: 5px;">{{ w }}</div>
          </el-alert>
        </el-form>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="aiOptDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="applyAiSuggestion">应用建议</el-button>
      </span>
    </el-dialog>

  </div>
</template>

<script>
import { getInventoryList } from '@/api/inventory'
import { submitApplication, getMyApplications } from '@/api/application'
import { semanticSearch, optimizeApplyForm } from '@/api/ai'
import { getAnnouncements } from '@/api/announcement'
import { submitFeedback as submitFeedbackApi, getMyFeedback } from '@/api/feedback'
import AnnouncementList from '@/components/AnnouncementList.vue'

export default {
  name: 'StudentIndex',
  components: {
    AnnouncementList
  },
  data() {
    return {
      activeMenu: 'inventory',
      userInfo: this.$store.state.userInfo,
      searchName: '',
      inventoryList: [],
      applyForm: {
        reagentId: null,
        quantity: 1, // 默认为数字
        purpose: ''
      },
      applyRules: {
        reagentId: [{ required: true, message: '请选择试剂', trigger: 'change' }],
        quantity: [{ required: true, message: '请输入申请数量', trigger: 'blur' }],
        purpose: [{ required: true, message: '请输入用途说明', trigger: 'blur' }]
      },
      myApplicationList: [],
      announcementList: [],
      feedbackForm: {
        feedbackType: 'SYSTEM',
        title: '',
        content: ''
      },
      feedbackRules: {
        feedbackType: [{ required: true, message: '请选择反馈类型', trigger: 'change' }],
        title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
        content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
      },
      myFeedbackList: [],
      // AI优化相关
      aiOptDialogVisible: false,
      aiOptData: {},
      selectedPurposeTemplate: ''
    }
  },
  mounted() {
    this.loadInventory()
    this.loadAnnouncements()
  },
  methods: {
    handleMenuSelect(index) {
      this.activeMenu = index
      if (index === 'myApplications') {
        this.loadMyApplications()
      } else if (index === 'inventory' || index === 'apply') {
        this.loadInventory()
      } else if (index === 'announcements') {
        this.loadAnnouncements()
      } else if (index === 'feedback') {
        this.loadMyFeedback()
      } else if (index === 'ai-assistant') {
        this.$router.push('/ai-assistant')
      }
    },
    loadInventory() {
      getInventoryList({ name: this.searchName }).then(res => {
        this.inventoryList = res.data
      })
    },
    handleSemanticSearch() {
      if (!this.searchName) {
        this.$message.warning('请输入要搜索的关键词')
        return
      }
      semanticSearch({ query: this.searchName, topK: 8, model: 'qwen-plus-2025-07-28' }).then(res => {
        const items = res.data || []
        if (!items.length) {
          this.$message.info('未找到更好的智能建议，已使用常规搜索')
          this.loadInventory()
          return
        }
        const names = items.map(i => `${i.name}${i.casNo ? '（' + i.casNo + '）' : ''}${i.reason ? ' - ' + i.reason : ''}`)
        this.$alert(names.join('\n'), 'AI智能推荐', { confirmButtonText: '确定' })
      }).catch(err => {
        this.$message.error('AI智能搜索失败：' + (err.message || ''))
      })
    },
    showApplyDialog(row) {
      this.activeMenu = 'apply'
      this.applyForm.reagentId = row.reagentId
    },
    submitApply() {
      this.$refs.applyForm.validate(valid => {
        if (valid) {
          submitApplication(this.applyForm).then(() => {
            this.$message.success('申请提交成功')
            this.resetApplyForm()
          })
        }
      })
    },
    handleApplyOptimize() {
      const selected = this.inventoryList.find(x => x.reagentId === this.applyForm.reagentId)
      if (!selected) {
        this.$message.warning('请先选择一种试剂');
        return;
      }
      const name = selected.reagentName
      const unit = selected.unit || ''

      const loading = this.$loading({
        lock: true,
        text: 'AI正在分析优化建议...',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
      });

      optimizeApplyForm({
        name,
        casNo: '',
        quantity: this.applyForm.quantity,
        unit,
        purpose: this.applyForm.purpose,
        model: 'qwen-plus-2025-07-28'
      }).then(res => {
        loading.close();
        const data = res.data || {}

        // 检查是否有有效数据
        const hasInfo = data.standardizedName || data.casNo || data.suggestedQuantity || (data.purposeTemplates && data.purposeTemplates.length) || (data.warnings && data.warnings.length)
        if (!hasInfo) {
          this.$message.info('AI暂无更优建议')
          return
        }

        this.aiOptData = data
        // 默认选中第一个模板，如果没有则为空
        if (data.purposeTemplates && data.purposeTemplates.length) {
          this.selectedPurposeTemplate = data.purposeTemplates[0]
        } else {
          this.selectedPurposeTemplate = ''
        }

        this.aiOptDialogVisible = true
      }).catch(err => {
        loading.close();
        this.$message.error('AI申领优化失败：' + (err.message || ''))
      })
    },
    applyAiSuggestion() {
      // 检查并应用 AI 建议的数值
      if (this.aiOptData.suggestedQuantity) {
        // parseFloat 确保它是数字，尽管 v-model 通常会处理
        this.applyForm.quantity = parseFloat(this.aiOptData.suggestedQuantity)
      }
      if (this.selectedPurposeTemplate) {
        this.applyForm.purpose = this.selectedPurposeTemplate
      }
      this.aiOptDialogVisible = false
      this.$message.success('已应用AI建议')
    },
    resetApplyForm() {
      this.$refs.applyForm.resetFields()
      this.applyForm.quantity = 1 // 重置为默认值
    },
    loadMyApplications() {
      getMyApplications().then(res => {
        this.myApplicationList = res.data
      })
    },
    loadAnnouncements() {
      getAnnouncements({ role: 'STUDENT' }).then(res => {
        this.announcementList = res.data || []
      })
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
    submitFeedback() {
      this.$refs.feedbackForm.validate(valid => {
        if (valid) {
          submitFeedbackApi(this.feedbackForm).then(() => {
            this.$message.success('反馈提交成功')
            this.resetFeedbackForm()
            this.loadMyFeedback()
          })
        }
      })
    },
    resetFeedbackForm() {
      this.$refs.feedbackForm && this.$refs.feedbackForm.resetFields()
      this.feedbackForm = {
        feedbackType: 'SYSTEM',
        title: '',
        content: ''
      }
    },
    loadMyFeedback() {
      getMyFeedback().then(res => {
        this.myFeedbackList = res.data || []
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