<template>
  <div class="container">
    <el-container style="height: 100%;">
      <!-- 头部 -->
      <el-header style="background: #409EFF; color: white;">
        <div style="display: flex; justify-content: space-between; align-items: center; height: 100%;">
          <h2>实验室化学试剂库存管理系统 - 学生端</h2>
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
          </el-menu>
        </el-aside>
        
        <!-- 内容区 -->
        <el-main>
          <!-- 库存查询 -->
          <div v-show="activeMenu === 'inventory'">
            <el-card>
              <div slot="header">
                <span>库存查询</span>
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
          
          <!-- 试剂申领 -->
          <div v-show="activeMenu === 'apply'">
            <el-card>
              <div slot="header">试剂申领</div>
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
                  <el-input-number v-model="applyForm.quantity" :min="1" :max="9999"></el-input-number>
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
          
          <!-- 我的申请 -->
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
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
import { getInventoryList } from '@/api/inventory'
import { submitApplication, getMyApplications } from '@/api/application'

export default {
  name: 'StudentIndex',
  data() {
    return {
      activeMenu: 'inventory',
      userInfo: this.$store.state.userInfo,
      searchName: '',
      inventoryList: [],
      applyForm: {
        reagentId: null,
        quantity: 1,
        purpose: ''
      },
      applyRules: {
        reagentId: [{ required: true, message: '请选择试剂', trigger: 'change' }],
        quantity: [{ required: true, message: '请输入申请数量', trigger: 'blur' }],
        purpose: [{ required: true, message: '请输入用途说明', trigger: 'blur' }]
      },
      myApplicationList: []
    }
  },
  mounted() {
    this.loadInventory()
  },
  methods: {
    handleMenuSelect(index) {
      this.activeMenu = index
      if (index === 'myApplications') {
        this.loadMyApplications()
      } else if (index === 'inventory' || index === 'apply') {
        this.loadInventory()
      }
    },
    loadInventory() {
      getInventoryList({ name: this.searchName }).then(res => {
        this.inventoryList = res.data
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
    resetApplyForm() {
      this.$refs.applyForm.resetFields()
    },
    loadMyApplications() {
      getMyApplications().then(res => {
        this.myApplicationList = res.data
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






