<template>
  <div class="container">
    <el-container style="height: 100%;">
      <!-- 头部 -->
      <el-header>
        <div class="header-content">
          <h2 class="header-title">实验室化学试剂库存管理系统 - 系统管理员</h2>
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
            <!-- 基础管理 -->
            <el-submenu index="basic">
              <template slot="title">
                <i class="el-icon-s-tools"></i>
                <span>基础管理</span>
                <el-badge v-if="(warningCount > 0 && !warningsViewed) || (pendingApplicationCount > 0 && !applicationsViewed)" is-dot class="menu-badge" />
              </template>
              <el-menu-item index="dashboard">
                <i class="el-icon-data-line"></i>
                <span>数据总览</span>
              </el-menu-item>
              <el-menu-item index="users">
                <i class="el-icon-user"></i>
                <span>用户管理</span>
              </el-menu-item>
              <el-menu-item index="inventory">
                <i class="el-icon-document"></i>
                <span>库存查看</span>
                <el-badge v-if="warningCount > 0 && !warningsViewed" :value="warningCount" class="menu-badge" />
              </el-menu-item>
              <el-menu-item index="applications">
                <i class="el-icon-s-order"></i>
                <span>申请记录</span>
                <el-badge v-if="pendingApplicationCount > 0 && !applicationsViewed" :value="pendingApplicationCount" class="menu-badge" />
              </el-menu-item>
              <el-menu-item index="category">
                <i class="el-icon-folder"></i>
                <span>试剂分类</span>
              </el-menu-item>
              <el-menu-item index="location">
                <i class="el-icon-location"></i>
                <span>存放位置</span>
              </el-menu-item>
              <el-menu-item index="records">
                <i class="el-icon-tickets"></i>
                <span>出入库记录</span>
              </el-menu-item>
            <el-menu-item index="announcements">
              <i class="el-icon-bell"></i>
              <span>公告发布</span>
            </el-menu-item>
            </el-submenu>

            <!-- AI助手 -->
            <el-menu-item index="ai-assistant">
              <i class="el-icon-chat-dot-round"></i>
              <span>AI智能助手</span>
            </el-menu-item>

            <!-- 生命周期追踪 -->
            <el-submenu index="lifecycle">
              <template slot="title">
                <i class="el-icon-link"></i>
                <span>生命周期</span>
              </template>
              <el-menu-item index="qrcode">
                <i class="el-icon-picture-outline"></i>
                <span>二维码管理</span>
              </el-menu-item>
              <el-menu-item index="location-map">
                <i class="el-icon-map-location"></i>
                <span>库位可视化</span>
              </el-menu-item>
            </el-submenu>

            <!-- 智能分析 -->
            <el-submenu index="analytics">
              <template slot="title">
                <i class="el-icon-data-analysis"></i>
                <span>智能分析</span>
              </template>
              <el-menu-item index="forecasting">
                <i class="el-icon-trend-charts"></i>
                <span>消耗预测</span>
              </el-menu-item>
            </el-submenu>
          </el-menu>
        </el-aside>
        
        <!-- 内容区 -->
        <el-main>
          <!-- 数据总览 -->
          <div v-show="activeMenu === 'dashboard'">
            <el-row :gutter="20">
              <el-col :span="6">
                <el-card class="stat-card">
                  <div class="stat-content-wrapper">
                    <div class="stat-icon-mini" style="background: #ecf5ff; color: #409EFF;">
                      <i class="el-icon-user"></i>
                    </div>
                    <div class="stat-info">
                      <div class="stat-label">用户总数</div>
                      <div class="stat-value">{{ userCount }}</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card class="stat-card">
                  <div class="stat-content-wrapper">
                    <div class="stat-icon-mini" style="background: #f0f9eb; color: #67C23A;">
                      <i class="el-icon-document"></i>
                    </div>
                    <div class="stat-info">
                      <div class="stat-label">库存种类</div>
                      <div class="stat-value">{{ inventoryCount }}</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card class="stat-card">
                  <div class="stat-content-wrapper">
                    <div class="stat-icon-mini" style="background: #fdf6ec; color: #E6A23C;">
                      <i class="el-icon-warning"></i>
                    </div>
                    <div class="stat-info">
                      <div class="stat-label">预警数量</div>
                      <div class="stat-value">{{ warningCount }}</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card class="stat-card">
                  <div class="stat-content-wrapper">
                    <div class="stat-icon-mini" style="background: #fef0f0; color: #F56C6C;">
                      <i class="el-icon-s-order"></i>
                    </div>
                    <div class="stat-info">
                      <div class="stat-label">申请总数</div>
                      <div class="stat-value">{{ applicationCount }}</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
            </el-row>
            
            <!-- 图表区域 -->
            <el-row :gutter="20" style="margin-top: 20px;">
              <el-col :span="12">
                <el-card>
                  <div slot="header">用户角色分布</div>
                  <ECharts :option="userRoleChart" height="250px"></ECharts>
                </el-card>
              </el-col>
              <el-col :span="12">
                <el-card>
                  <div slot="header">库存状态统计</div>
                  <ECharts :option="inventoryStatusChart" height="250px"></ECharts>
                </el-card>
              </el-col>
            </el-row>
            
            <el-row :gutter="20" style="margin-top: 20px;">
              <el-col :span="12">
                <el-card>
                  <div slot="header">申请状态分布</div>
                  <ECharts :option="applicationStatusChart" height="250px"></ECharts>
                </el-card>
              </el-col>
              <el-col :span="12">
                <el-card>
                  <div slot="header">月度申请趋势</div>
                  <ECharts :option="monthlyTrendChart" height="250px"></ECharts>
                </el-card>
              </el-col>
            </el-row>
            
            <el-row :gutter="20" style="margin-top: 20px;">
              <el-col :span="24">
                <el-card>
                  <div slot="header">最新预警</div>
                  <el-table :data="warningList.slice(0, 5)" border>
                    <el-table-column prop="reagentName" label="试剂名称"></el-table-column>
                    <el-table-column prop="quantity" label="库存数量"></el-table-column>
                    <el-table-column prop="expiryDate" label="有效期"></el-table-column>
                    <el-table-column prop="status" label="状态">
                      <template slot-scope="scope">
                        <el-tag v-if="scope.row.status === 'LOW'" type="warning">库存不足</el-tag>
                        <el-tag v-else-if="scope.row.status === 'EXPIRING'" type="warning">即将过期</el-tag>
                        <el-tag v-else type="danger">已过期</el-tag>
                      </template>
                    </el-table-column>
                  </el-table>
                </el-card>
              </el-col>
            </el-row>
          </div>
          
          <!-- 用户管理 -->
          <div v-show="activeMenu === 'users'">
            <el-card>
              <div slot="header">
                <span>用户管理</span>
                <el-button type="primary" size="small" style="float: right;" @click="showAddUserDialog">添加用户</el-button>
              </div>
              <el-table :data="userList" border>
                <el-table-column prop="username" label="用户名" width="120"></el-table-column>
                <el-table-column prop="realName" label="真实姓名" width="120"></el-table-column>
                <el-table-column prop="role" label="角色" width="100">
                  <template slot-scope="scope">
                    <el-tag v-if="scope.row.role === 'ADMIN'" type="danger">系统管理员</el-tag>
                    <el-tag v-else-if="scope.row.role === 'TEACHER'" type="success">老师</el-tag>
                    <el-tag v-else type="info">学生</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="department" label="部门/班级" width="150"></el-table-column>
                <el-table-column prop="email" label="邮箱" width="180"></el-table-column>
                <el-table-column prop="phone" label="手机号" width="130"></el-table-column>
                <el-table-column prop="status" label="状态" width="80">
                  <template slot-scope="scope">
                    <el-tag v-if="scope.row.status === 1" type="success">启用</el-tag>
                    <el-tag v-else type="danger">禁用</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="createTime" label="创建时间" width="180"></el-table-column>
                <el-table-column label="操作" width="220">
                  <template slot-scope="scope">
                    <el-button size="mini" @click="showEditUserDialog(scope.row)">编辑</el-button>
                    <el-button 
                      v-if="scope.row.role !== 'ADMIN'"
                      size="mini" 
                      :type="scope.row.status === 1 ? 'warning' : 'success'"
                      @click="toggleUserStatus(scope.row)"
                    >
                      {{ scope.row.status === 1 ? '禁用' : '启用' }}
                    </el-button>
                    <el-button 
                      v-if="scope.row.role !== 'ADMIN'"
                      size="mini" 
                      type="danger" 
                      @click="deleteUserItem(scope.row)"
                    >删除</el-button>
                    <el-tag v-if="scope.row.role === 'ADMIN'" type="info" size="small">受保护</el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </div>
          
          <!-- 库存查看 -->
          <div v-show="activeMenu === 'inventory'">
            <el-card>
              <div slot="header">
                <span>库存查看</span>
                <div style="float: right; display: flex; gap: 10px; align-items: center;">
                  <el-select v-model="adminFilterStatus" @change="handleAdminFilterChange" size="small" placeholder="状态" style="width: 120px;" clearable>
                    <el-option label="全部" value=""></el-option>
                    <el-option label="正常" value="NORMAL"></el-option>
                    <el-option label="库存不足" value="LOW"></el-option>
                    <el-option label="即将过期" value="EXPIRING"></el-option>
                    <el-option label="已过期" value="EXPIRED"></el-option>
                  </el-select>
                  <el-select v-model="adminSortField" @change="handleAdminFilterChange" size="small" placeholder="排序方式" style="width: 140px;" clearable>
                    <el-option label="更新时间" value="update_time"></el-option>
                    <el-option label="有效期" value="expiry_date"></el-option>
                    <el-option label="库存数量" value="quantity"></el-option>
                    <el-option label="预警阈值" value="warning_threshold"></el-option>
                  </el-select>
                  <el-input
                    v-model="searchName"
                    placeholder="输入试剂名称搜索"
                    style="width: 200px;"
                    @change="loadInventory"
                    size="small"
                    clearable
                  >
                    <el-button slot="append" icon="el-icon-search" @click="loadInventory"></el-button>
                  </el-input>
                </div>
              </div>
              <el-table :data="inventoryList" border>
                <el-table-column prop="reagentName" label="试剂名称"></el-table-column>
                <el-table-column prop="specification" label="规格型号"></el-table-column>
                <el-table-column prop="categoryName" label="分类"></el-table-column>
                <el-table-column prop="quantity" label="库存数量"></el-table-column>
                <el-table-column prop="unit" label="单位"></el-table-column>
                <el-table-column prop="locationName" label="存放位置"></el-table-column>
                <el-table-column prop="expiryDate" label="有效期"></el-table-column>
                <el-table-column prop="status" label="状态">
                  <template slot-scope="scope">
                    <el-tag v-if="scope.row.status === 'NORMAL'" type="success">正常</el-tag>
                    <el-tag v-else-if="scope.row.status === 'LOW'" type="warning">库存不足</el-tag>
                    <el-tag v-else-if="scope.row.status === 'EXPIRING'" type="warning">即将过期</el-tag>
                    <el-tag v-else type="danger">已过期</el-tag>
                  </template>
                </el-table-column>
              </el-table>
              <el-pagination
                @size-change="handleAdminSizeChange"
                @current-change="handleAdminPageChange"
                :current-page="adminPage"
                :page-sizes="[10, 20, 50, 100]"
                :page-size="adminPageSize"
                layout="total, sizes, prev, pager, next, jumper"
                :total="adminTotal"
                style="margin-top: 20px; text-align: right;">
              </el-pagination>
            </el-card>
          </div>
          
          <!-- 申请记录 -->
          <div v-show="activeMenu === 'applications'">
            <el-card>
              <div slot="header">申请记录</div>
              <el-table :data="applicationList" border>
                <el-table-column prop="applicationNo" label="申请单号" width="180"></el-table-column>
                <el-table-column prop="reagentName" label="试剂名称" width="150"></el-table-column>
                <el-table-column prop="quantity" label="申请数量" width="100"></el-table-column>
                <el-table-column prop="applicantName" label="申请人" width="100"></el-table-column>
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
                <el-table-column prop="reviewerName" label="审核人" width="100"></el-table-column>
              </el-table>
            </el-card>
          </div>
          
          <!-- 试剂分类 -->
          <div v-show="activeMenu === 'category'">
            <el-card>
              <div slot="header">
                <span>试剂分类</span>
                <el-button type="primary" size="small" style="float: right;" @click="showAddCategoryDialog">添加分类</el-button>
              </div>
              <el-table :data="categoryList" border>
                <el-table-column prop="name" label="分类名称"></el-table-column>
                <el-table-column prop="code" label="分类编码"></el-table-column>
                <el-table-column prop="description" label="描述"></el-table-column>
                <el-table-column prop="createTime" label="创建时间"></el-table-column>
                <el-table-column label="操作" width="100">
                  <template slot-scope="scope">
                    <el-button size="mini" type="danger" @click="deleteCategory(scope.row)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </div>
          
          <!-- 存放位置 -->
          <div v-show="activeMenu === 'location'">
            <el-card>
              <div slot="header">
                <span>存放位置</span>
                <el-button type="primary" size="small" style="float: right;" @click="showAddLocationDialog">添加位置</el-button>
              </div>
              <el-table :data="locationList" border>
                <el-table-column prop="roomName" label="房间名称"></el-table-column>
                <el-table-column prop="cabinetNo" label="柜号"></el-table-column>
                <el-table-column prop="shelfNo" label="货架号"></el-table-column>
                <el-table-column prop="fullLocation" label="完整位置"></el-table-column>
                <el-table-column prop="description" label="描述"></el-table-column>
                <el-table-column label="操作" width="100">
                  <template slot-scope="scope">
                    <el-button size="mini" type="danger" @click="deleteLocation(scope.row)">删除</el-button>
                  </template>
                </el-table-column>
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
                    <el-table-column prop="reagentName" label="试剂名称" width="150"></el-table-column>
                    <el-table-column prop="quantity" label="入库数量" width="100"></el-table-column>
                    <el-table-column prop="supplier" label="供应商" width="180"></el-table-column>
                    <el-table-column prop="operatorName" label="操作人" width="100"></el-table-column>
                    <el-table-column prop="createTime" label="入库时间" width="180"></el-table-column>
                    <el-table-column prop="remark" label="备注" min-width="200"></el-table-column>
                  </el-table>
                  <el-pagination
                    @current-change="handleStockInPageChange"
                    :current-page="stockInPage"
                    :page-size="stockInPageSize"
                    layout="total, prev, pager, next"
                    :total="stockInTotal"
                    style="margin-top: 20px; text-align: right;"
                  ></el-pagination>
                </el-tab-pane>
                <el-tab-pane label="出库记录" name="out">
                  <el-table :data="stockOutRecords" border>
                    <el-table-column prop="reagentName" label="试剂名称" width="150"></el-table-column>
                    <el-table-column prop="quantity" label="出库数量" width="100"></el-table-column>
                    <el-table-column prop="recipientName" label="领用人" width="120"></el-table-column>
                    <el-table-column prop="operatorName" label="操作人" width="100"></el-table-column>
                    <el-table-column prop="createTime" label="出库时间" width="180"></el-table-column>
                    <el-table-column prop="purpose" label="用途" min-width="150"></el-table-column>
                    <el-table-column prop="remark" label="备注" min-width="150"></el-table-column>
                  </el-table>
                  <el-pagination
                    @current-change="handleStockOutPageChange"
                    :current-page="stockOutPage"
                    :page-size="stockOutPageSize"
                    layout="total, prev, pager, next"
                    :total="stockOutTotal"
                    style="margin-top: 20px; text-align: right;"
                  ></el-pagination>
                </el-tab-pane>
              </el-tabs>
            </el-card>
          </div>

          <!-- 公告发布 -->
          <div v-show="activeMenu === 'announcements'">
            <el-row :gutter="20">
              <el-col :span="10">
                <el-card>
                  <div slot="header">
                    <span><i class="el-icon-edit"></i> 发布公告</span>
                  </div>
                  <el-form :model="announcementForm" :rules="announcementRules" ref="announcementForm" label-width="80px">
                    <el-form-item label="标题" prop="title">
                      <el-input v-model="announcementForm.title" placeholder="请输入公告标题"></el-input>
                    </el-form-item>
                    <el-form-item label="受众" prop="audience">
                      <el-select v-model="announcementForm.audience" style="width: 100%;">
                        <el-option label="全部人员" value="ALL"></el-option>
                        <el-option label="仅老师" value="TEACHER"></el-option>
                        <el-option label="仅学生" value="STUDENT"></el-option>
                      </el-select>
                    </el-form-item>
                    <el-form-item label="级别" prop="priority">
                      <el-select v-model="announcementForm.priority" style="width: 100%;">
                        <el-option label="普通提醒" value="INFO"></el-option>
                        <el-option label="重要通知" value="WARN"></el-option>
                        <el-option label="紧急通知" value="URGENT"></el-option>
                      </el-select>
                    </el-form-item>
                    <el-form-item label="内容" prop="content">
                      <el-input type="textarea" :rows="6" v-model="announcementForm.content" placeholder="请输入公告内容"></el-input>
                    </el-form-item>
                    <el-form-item>
                      <el-button type="primary" :loading="announcementSubmitting" @click="submitAnnouncement">发布公告</el-button>
                      <el-button @click="resetAnnouncementForm">重置</el-button>
                    </el-form-item>
                  </el-form>
                </el-card>
              </el-col>
              <el-col :span="14">
                <AnnouncementList :can-publish="true" @publish="handlePublishAnnouncement" ref="announcementList" />
              </el-col>
            </el-row>
          </div>

          <!-- 二维码管理 -->
          <div v-show="activeMenu === 'qrcode'">
            <QrcodeManagement />
          </div>

          <!-- 库位可视化 -->
          <div v-show="activeMenu === 'location-map'">
            <LocationMap />
          </div>

          <!-- 消耗预测 -->
          <div v-show="activeMenu === 'forecasting'">
            <ConsumptionForecast />
          </div>


        </el-main>
      </el-container>
    </el-container>
    
    <!-- 添加/编辑用户对话框 -->
    <el-dialog :title="userDialogTitle" :visible.sync="userDialogVisible" width="500px">
      <el-form :model="userForm" :rules="userRules" ref="userForm" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" :disabled="userForm.id !== null"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!userForm.id">
          <el-input v-model="userForm.password" type="password"></el-input>
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="userForm.realName"></el-input>
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role" style="width: 100%;" :disabled="userForm.id && userForm.originalRole === 'ADMIN'">
            <el-option label="学生" value="STUDENT"></el-option>
            <el-option label="老师" value="TEACHER"></el-option>
            <el-option 
              label="系统管理员" 
              value="ADMIN" 
              :disabled="hasAdmin && (!userForm.id || userForm.originalRole !== 'ADMIN')"
            ></el-option>
          </el-select>
          <div v-if="userForm.id && userForm.originalRole === 'ADMIN'" style="color: #999; font-size: 12px; margin-top: 5px;">
            管理员角色不可修改
          </div>
        </el-form-item>
        <el-form-item label="部门/班级">
          <el-input v-model="userForm.department"></el-input>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="userForm.email"></el-input>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="userForm.phone"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="userDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveUser">确定</el-button>
      </span>
    </el-dialog>
    
    <!-- 添加分类对话框 -->
    <el-dialog title="添加分类" :visible.sync="categoryDialogVisible" width="400px">
      <el-form :model="categoryForm" label-width="100px">
        <el-form-item label="分类名称">
          <el-input v-model="categoryForm.name"></el-input>
        </el-form-item>
        <el-form-item label="分类编码">
          <el-input v-model="categoryForm.code"></el-input>
        </el-form-item>
        <el-form-item label="描述">
          <el-input type="textarea" v-model="categoryForm.description"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="categoryDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCategory">确定</el-button>
      </span>
    </el-dialog>
    
    <!-- 添加位置对话框 -->
    <el-dialog title="添加位置" :visible.sync="locationDialogVisible" width="400px">
      <el-form :model="locationForm" label-width="100px">
        <el-form-item label="房间名称">
          <el-input v-model="locationForm.roomName"></el-input>
        </el-form-item>
        <el-form-item label="柜号">
          <el-input v-model="locationForm.cabinetNo"></el-input>
        </el-form-item>
        <el-form-item label="货架号">
          <el-input v-model="locationForm.shelfNo"></el-input>
        </el-form-item>
        <el-form-item label="完整位置">
          <el-input v-model="locationForm.fullLocation"></el-input>
        </el-form-item>
        <el-form-item label="描述">
          <el-input type="textarea" v-model="locationForm.description"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="locationDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveLocation">确定</el-button>
      </span>
    </el-dialog>
    
  </div>
</template>

<script>
import { getUserList, addUser, updateUser, updateUserStatus, deleteUser } from '@/api/user'
import { getInventoryList, getWarningList } from '@/api/inventory'
import { getAllApplications } from '@/api/application'
import { getCategoryList, addCategory, deleteCategory as delCategory, getLocationList, addLocation, deleteLocation as delLocation } from '@/api/base'
import { getAnnouncements, createAnnouncement, deleteAnnouncement } from '@/api/announcement'
import { getStockInList, getStockOutList } from '@/api/stock'
import { exportInventory, exportStockIn, exportStockOut } from '@/api/export'
import ECharts from '@/components/ECharts.vue'
import QrcodeManagement from '@/components/QrcodeManagement.vue'
import LocationMap from '@/components/LocationMap.vue'
import ConsumptionForecast from '@/components/ConsumptionForecast.vue'
import AnnouncementList from '@/components/AnnouncementList.vue'

export default {
  name: 'AdminIndex',
  components: {
    ECharts,
    AnnouncementList,
    QrcodeManagement,
    LocationMap,
    ConsumptionForecast,
  },
  data() {
    return {
      activeMenu: 'dashboard',
      userInfo: this.$store.state.userInfo,
      searchName: '',
      userCount: 0,
      inventoryCount: 0,
      warningCount: 0,
      warningsViewed: false,
      applicationCount: 0,
      pendingApplicationCount: 0,
      applicationsViewed: false,
      userList: [],
      inventoryList: [],
      adminFilterStatus: '',
      adminSortField: 'update_time',
      adminPage: 1,
      adminPageSize: 10,
      adminTotal: 0,
      warningList: [],
      applicationList: [],
      categoryList: [],
      locationList: [],
      announcementList: [],
      stockInRecords: [],
      stockInPage: 1,
      stockInPageSize: 10,
      stockInTotal: 0,
      stockOutRecords: [],
      stockOutPage: 1,
      stockOutPageSize: 10,
      stockOutTotal: 0,
      recordTab: 'in',
      userDialogVisible: false,
      userDialogTitle: '添加用户',
      hasAdmin: false,
      userForm: {
        id: null,
        username: '',
        password: '',
        realName: '',
        role: 'STUDENT',
        department: '',
        email: '',
        phone: '',
        originalRole: null
      },
      userRules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
        realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
        role: [{ required: true, message: '请选择角色', trigger: 'change' }]
      },
      categoryDialogVisible: false,
      categoryForm: {
        name: '',
        code: '',
        description: ''
      },
      locationDialogVisible: false,
      locationForm: {
        roomName: '',
        cabinetNo: '',
        shelfNo: '',
        fullLocation: '',
        description: ''
      },
      // 图表配置
      userRoleChart: {},
      inventoryStatusChart: {},
      applicationStatusChart: {},
      monthlyTrendChart: {},
      announcementForm: {
        title: '',
        content: '',
        audience: 'ALL',
        priority: 'INFO'
      },
      announcementRules: {
        title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
        content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }]
      },
      announcementSubmitting: false,
      announcementLoading: false
    }
  },
  mounted() {
    this.loadDashboard()
  },
  methods: {
    loadDashboard() {
      // 加载仪表盘数据
      this.loadUsers()
      this.loadInventory()
      this.loadApplications()
    },
    
    handleMenuSelect(index) {
      this.activeMenu = index
      if (index === 'users') {
        this.loadUsers()
      } else if (index === 'inventory') {
        this.loadInventory()
        this.warningsViewed = true
      } else if (index === 'applications') {
        this.loadApplications()
        this.applicationsViewed = true
      } else if (index === 'category') {
        this.loadCategories()
      } else if (index === 'location') {
        this.loadLocations()
      } else if (index === 'announcements') {
        this.loadAnnouncements()
      } else if (index === 'records') {
        this.loadStockRecords()
      } else if (index === 'dashboard') {
        this.loadDashboard()
      } else if (index === 'ai-assistant') {
        this.$router.push('/ai-assistant')
      }
    },
    loadDashboard() {
      getUserList().then(res => {
        this.userCount = res.data.length
        this.generateUserRoleChart(res.data)
      })
      getInventoryList({}).then(res => {
        this.inventoryCount = res.data.length
        this.generateInventoryStatusChart(res.data)
      })
      getWarningList().then(res => {
        const list = res.data || []
        if (list.length > this.warningCount) {
          this.warningsViewed = false
        }
        this.warningList = list
        this.warningCount = list.length
      })
      getAllApplications().then(res => {
        const list = res.data || []
        const pendingCount = list.filter(a => a.status === 'PENDING').length
        if (pendingCount > this.pendingApplicationCount) {
          this.applicationsViewed = false
        }
        this.applicationCount = list.length
        this.pendingApplicationCount = pendingCount
        this.generateApplicationStatusChart(res.data)
        this.generateMonthlyTrendChart(res.data)
      })
    },
    loadUsers() {
      getUserList().then(res => {
        this.userList = res.data
        // 检查是否已有管理员
        this.hasAdmin = res.data.some(user => user.role === 'ADMIN')
      })
    },
    loadInventory() {
      const params = {
        name: this.searchName,
        page: this.adminPage,
        size: this.adminPageSize
      }
      // 状态筛选（独立条件）
      if (this.adminFilterStatus) {
        params.status = this.adminFilterStatus
      }
      // 排序
      if (this.adminSortField) {
        params.sortField = this.adminSortField
        params.sortOrder = 'DESC'
      }
      getInventoryList(params).then(res => {
        if (res.data && res.data.records) {
          this.inventoryList = res.data.records
          this.adminTotal = res.data.total || 0
        } else {
          this.inventoryList = res.data || []
          this.adminTotal = this.inventoryList.length
        }
      })
    },
    handleAdminFilterChange() {
      this.adminPage = 1
      this.loadInventory()
    },
    handleAdminSizeChange(val) {
      this.adminPageSize = val
      this.adminPage = 1
      this.loadInventory()
    },
    handleAdminPageChange(val) {
      this.adminPage = val
      this.loadInventory()
    },
    loadApplications() {
      getAllApplications().then(res => {
        this.applicationList = res.data
        this.pendingApplicationCount = res.data.filter(a => a.status === 'PENDING').length
      })
    },
    loadCategories() {
      getCategoryList().then(res => {
        this.categoryList = res.data
      })
    },
    loadLocations() {
      getLocationList().then(res => {
        this.locationList = res.data
      })
    },
    loadAnnouncements() {
      if (this.$refs.announcementList) {
        this.$refs.announcementList.loadAnnouncements()
      }
    },
    handlePublishAnnouncement() {
      // 触发发布公告表单显示（如果需要）
    },
    submitAnnouncement() {
      this.$refs.announcementForm.validate(valid => {
        if (!valid) return
        const payload = {
          ...this.announcementForm,
          creatorId: this.userInfo.id,
          creatorName: this.userInfo.realName
        }
        this.announcementSubmitting = true
        createAnnouncement(payload).then(() => {
          this.$message.success('公告发布成功')
          this.resetAnnouncementForm()
          this.loadAnnouncements()
          // 刷新公告列表组件
          if (this.$refs.announcementList) {
            this.$refs.announcementList.loadAnnouncements()
          }
        }).catch(err => {
          this.$message.error('发布失败：' + (err.message || '未知错误'))
        }).finally(() => {
          this.announcementSubmitting = false
        })
      })
    },
    resetAnnouncementForm() {
      this.$refs.announcementForm && this.$refs.announcementForm.resetFields()
      this.announcementForm = {
        title: '',
        content: '',
        audience: 'ALL',
        priority: 'INFO'
      }
    },
    deleteAnnouncementItem(row) {
      // 这个方法现在由AnnouncementList组件内部处理
    },
    viewAnnouncement(row) {
      this.$alert(row.content, row.title, {
        confirmButtonText: '已读',
        callback: () => {}
      })
    },
    getAudienceLabel(value) {
      const map = {
        'ALL': '全部人员',
        'TEACHER': '老师',
        'STUDENT': '学生'
      }
      return map[value] || '全部人员'
    },
    getPriorityLabel(value) {
      const map = {
        'INFO': '普通提醒',
        'WARN': '重要通知',
        'URGENT': '紧急通知'
      }
      return map[value] || '普通提醒'
    },
    getPriorityTag(value) {
      const map = {
        'INFO': 'info',
        'WARN': 'warning',
        'URGENT': 'danger'
      }
      return map[value] || 'info'
    },
    showAddUserDialog() {
      this.userDialogTitle = '添加用户'
      this.userForm = {
        id: null,
        username: '',
        password: '',
        realName: '',
        role: 'STUDENT',
        department: '',
        email: '',
        phone: '',
        originalRole: null
      }
      this.userDialogVisible = true
    },
    showEditUserDialog(row) {
      this.userDialogTitle = '编辑用户'
      this.userForm = {
        id: row.id,
        username: row.username,
        password: '',
        realName: row.realName,
        role: row.role,
        department: row.department,
        email: row.email,
        phone: row.phone,
        originalRole: row.role  // 记录原始角色
      }
      this.userDialogVisible = true
    },
    saveUser() {
      this.$refs.userForm.validate(valid => {
        if (valid) {
          if (this.userForm.id) {
            updateUser(this.userForm).then(() => {
              this.$message.success('更新成功')
              this.userDialogVisible = false
              this.loadUsers()
            })
          } else {
            addUser(this.userForm).then(() => {
              this.$message.success('添加成功')
              this.userDialogVisible = false
              this.loadUsers()
            })
          }
        }
      })
    },
    toggleUserStatus(row) {
      const status = row.status === 1 ? 0 : 1
      updateUserStatus(row.id, status).then(() => {
        this.$message.success('操作成功')
        this.loadUsers()
      })
    },
    deleteUserItem(row) {
      this.$confirm('确定要删除该用户吗？', '提示', {
        type: 'warning'
      }).then(() => {
        deleteUser(row.id).then(() => {
          this.$message.success('删除成功')
          this.loadUsers()
        })
      }).catch(() => {})
    },
    showAddCategoryDialog() {
      this.categoryForm = {
        name: '',
        code: '',
        description: ''
      }
      this.categoryDialogVisible = true
    },
    saveCategory() {
      addCategory(this.categoryForm).then(() => {
        this.$message.success('添加成功')
        this.categoryDialogVisible = false
        this.loadCategories()
      })
    },
    deleteCategory(row) {
      this.$confirm('确定要删除该分类吗？', '提示', {
        type: 'warning'
      }).then(() => {
        delCategory(row.id).then(() => {
          this.$message.success('删除成功')
          this.loadCategories()
        })
      }).catch(() => {})
    },
    showAddLocationDialog() {
      this.locationForm = {
        roomName: '',
        cabinetNo: '',
        shelfNo: '',
        fullLocation: '',
        description: ''
      }
      this.locationDialogVisible = true
    },
    saveLocation() {
      addLocation(this.locationForm).then(() => {
        this.$message.success('添加成功')
        this.locationDialogVisible = false
        this.loadLocations()
      })
    },
    deleteLocation(row) {
      this.$confirm('确定要删除该位置吗？', '提示', {
        type: 'warning'
      }).then(() => {
        delLocation(row.id).then(() => {
          this.$message.success('删除成功')
          this.loadLocations()
        })
      }).catch(() => {})
    },
    loadStockRecords() {
      this.loadStockInRecords()
      this.loadStockOutRecords()
    },
    loadStockInRecords() {
      getStockInList({ page: this.stockInPage, size: this.stockInPageSize }).then(res => {
        this.stockInRecords = res.data.records || []
        this.stockInTotal = res.data.total || 0
      })
    },
    loadStockOutRecords() {
      getStockOutList({ page: this.stockOutPage, size: this.stockOutPageSize }).then(res => {
        this.stockOutRecords = res.data.records || []
        this.stockOutTotal = res.data.total || 0
      })
    },
    handleStockInPageChange(val) {
      this.stockInPage = val
      this.loadStockInRecords()
    },
    handleStockOutPageChange(val) {
      this.stockOutPage = val
      this.loadStockOutRecords()
    },
    exportInventoryData() {
      exportInventory().then(blob => {
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = '库存清单.xlsx'
        link.click()
        window.URL.revokeObjectURL(url)
        this.$message.success('导出成功')
      }).catch(err => {
        console.error('导出失败:', err)
        this.$message.error('导出失败')
      })
    },
    exportStockInData() {
      exportStockIn().then(blob => {
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = '入库记录.xlsx'
        link.click()
        window.URL.revokeObjectURL(url)
        this.$message.success('导出成功')
      }).catch(err => {
        console.error('导出失败:', err)
        this.$message.error('导出失败')
      })
    },
    exportStockOutData() {
      exportStockOut().then(blob => {
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = '出库记录.xlsx'
        link.click()
        window.URL.revokeObjectURL(url)
        this.$message.success('导出成功')
      }).catch(err => {
        console.error('导出失败:', err)
        this.$message.error('导出失败')
      })
    },
    handleLogout() {
      this.$confirm('确定要退出登录吗？', '提示', {
        type: 'warning'
      }).then(() => {
        this.$store.dispatch('logout')
        this.$router.push('/login')
      }).catch(() => {})
    },
    
    // 生成用户角色分布饼图
    generateUserRoleChart(userList) {
      const roleCount = {
        'ADMIN': 0,
        'TEACHER': 0,
        'STUDENT': 0
      }
      
      userList.forEach(user => {
        roleCount[user.role]++
      })
      
      this.userRoleChart = {
        title: {
          text: '用户角色分布',
          left: 'center',
          textStyle: {
            fontSize: 14
          },
          top: 10
        },
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          data: ['系统管理员', '老师', '学生'],
          top: 30
        },
        series: [
          {
            name: '用户角色',
            type: 'pie',
            radius: '45%',
            center: ['50%', '60%'],
            data: [
              { value: roleCount.ADMIN, name: '系统管理员', itemStyle: { color: '#F56C6C' } },
              { value: roleCount.TEACHER, name: '老师', itemStyle: { color: '#67C23A' } },
              { value: roleCount.STUDENT, name: '学生', itemStyle: { color: '#409EFF' } }
            ],
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            }
          }
        ]
      }
    },
    
    // 生成库存状态柱状图
    generateInventoryStatusChart(inventoryList) {
      const statusCount = {
        'NORMAL': 0,
        'LOW': 0,
        'EXPIRING': 0,
        'EXPIRED': 0
      }
      
      inventoryList.forEach(inventory => {
        statusCount[inventory.status]++
      })
      
      this.inventoryStatusChart = {
        title: {
          text: '库存状态统计',
          left: 'center',
          textStyle: {
            fontSize: 16
          }
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        xAxis: {
          type: 'category',
          data: ['正常', '库存不足', '即将过期', '已过期']
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '库存数量',
            type: 'bar',
            data: [
              { value: statusCount.NORMAL, itemStyle: { color: '#67C23A' } },
              { value: statusCount.LOW, itemStyle: { color: '#E6A23C' } },
              { value: statusCount.EXPIRING, itemStyle: { color: '#F56C6C' } },
              { value: statusCount.EXPIRED, itemStyle: { color: '#909399' } }
            ],
            barWidth: '60%'
          }
        ]
      }
    },
    
    // 生成申请状态分布饼图
    generateApplicationStatusChart(applicationList) {
      const statusCount = {
        'PENDING': 0,
        'APPROVED': 0,
        'REJECTED': 0,
        'COMPLETED': 0
      }
      
      applicationList.forEach(application => {
        statusCount[application.status]++
      })
      
      this.applicationStatusChart = {
        title: {
          text: '申请状态分布',
          left: 'center',
          textStyle: {
            fontSize: 16
          }
        },
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          data: ['待审核', '已通过', '已拒绝', '已完成']
        },
        series: [
          {
            name: '申请状态',
            type: 'pie',
            radius: '50%',
            center: ['50%', '60%'],
            data: [
              { value: statusCount.PENDING, name: '待审核', itemStyle: { color: '#E6A23C' } },
              { value: statusCount.APPROVED, name: '已通过', itemStyle: { color: '#67C23A' } },
              { value: statusCount.REJECTED, name: '已拒绝', itemStyle: { color: '#F56C6C' } },
              { value: statusCount.COMPLETED, name: '已完成', itemStyle: { color: '#409EFF' } }
            ],
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            }
          }
        ]
      }
    },
    
    // 生成月度申请趋势图
    generateMonthlyTrendChart(applicationList) {
      // 获取最近6个月的数据
      const months = []
      const counts = []
      
      for (let i = 5; i >= 0; i--) {
        const date = new Date()
        date.setMonth(date.getMonth() - i)
        const month = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`
        months.push(month)
        
        // 统计该月的申请数量
        const count = applicationList.filter(app => {
          const appDate = new Date(app.createTime)
          const appMonth = `${appDate.getFullYear()}-${String(appDate.getMonth() + 1).padStart(2, '0')}`
          return appMonth === month
        }).length
        
        counts.push(count)
      }
      
      this.monthlyTrendChart = {
        title: {
          text: '月度申请趋势',
          left: 'center',
          textStyle: {
            fontSize: 16
          }
        },
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: months
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '申请数量',
            type: 'line',
            data: counts,
            smooth: true,
            lineStyle: {
              color: '#4f46e5'
            },
            itemStyle: {
              color: '#4f46e5'
            },
            areaStyle: {
              color: {
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [
                  { offset: 0, color: 'rgba(79, 70, 229, 0.3)' },
                  { offset: 1, color: 'rgba(79, 70, 229, 0.1)' }
                ]
              }
            }
          }
        ]
      }
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

.stat-card {
  padding: 0;
  overflow: hidden;
}

.stat-content-wrapper {
  display: flex;
  align-items: center;
  padding: 24px;
}

.stat-icon-mini {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-right: 16px;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 4px;
  font-weight: 500;
}

.el-card {
  margin-bottom: 24px;
  transition: transform 0.2s;
}

.el-card:hover {
  transform: translateY(-2px);
}
.menu-badge {
  margin-left: 8px;
}

.menu-badge /deep/ .el-badge__content {
  line-height: 18px;
}
</style>





