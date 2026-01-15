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
              <i class="el-icon-s-check"></i>
              <span>申领审批</span>
              <el-badge v-if="pendingApplicationsCount > 0 && !applicationsViewed" :value="pendingApplicationsCount" class="menu-badge" />
            </el-menu-item>
            <el-menu-item index="stockOut">
            <i class="el-icon-sold-out"></i>
            <span>出库管理</span>
          </el-menu-item>
          <el-menu-item index="warning">
            <i class="el-icon-warning"></i>
            <span>库存预警</span>
            <el-badge v-if="warningCount > 0 && !warningsViewed" :value="warningCount" class="menu-badge" />
            </el-menu-item>
            <el-menu-item index="records">
              <i class="el-icon-s-order"></i>
              <span>出入库记录</span>
              <el-badge v-if="!recordsViewed" is-dot class="menu-badge" />
            </el-menu-item>
            <el-menu-item index="announcements">
              <i class="el-icon-bell"></i>
              <span>系统公告</span>
            </el-menu-item>
            <el-menu-item index="feedback">
              <i class="el-icon-chat-line-square"></i>
              <span>意见反馈</span>
              <el-badge v-if="pendingFeedbackCount > 0 && !feedbackViewed" :value="pendingFeedbackCount" class="menu-badge" />
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
                <div style="float: right; display: flex; gap: 10px; align-items: center;">
                  <el-select v-model="filterStatus" @change="handleFilterChange" size="small" placeholder="状态" style="width: 120px;" clearable>
                    <el-option label="全部" value=""></el-option>
                    <el-option label="正常" value="NORMAL"></el-option>
                    <el-option label="库存不足" value="LOW"></el-option>
                    <el-option label="即将过期" value="EXPIRING"></el-option>
                    <el-option label="已过期" value="EXPIRED"></el-option>
                  </el-select>
                  <el-select v-model="sortField" @change="handleFilterChange" size="small" placeholder="排序方式" style="width: 140px;" clearable>
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
                <el-table-column prop="reagentName" label="试剂名称" min-width="130"></el-table-column>
                <el-table-column prop="specification" label="规格" width="100"></el-table-column>
                <el-table-column prop="batchNo" label="批次号" width="120"></el-table-column>
                <el-table-column prop="quantity" label="库存数量" width="90"></el-table-column>
                <el-table-column prop="warningThreshold" label="预警阈值" width="90"></el-table-column>
                <el-table-column prop="locationName" label="存放位置" min-width="150"></el-table-column>
                <el-table-column prop="expiryDate" label="有效期" width="110"></el-table-column>
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
                    <el-button size="mini" @click="showThresholdDialog(scope.row)">设置预警</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-pagination
                @size-change="handleInventorySizeChange"
                @current-change="handleInventoryPageChange"
                :current-page="inventoryPage"
                :page-sizes="[10, 20, 50, 100]"
                :page-size="inventoryPageSize"
                layout="total, sizes, prev, pager, next, jumper"
                :total="inventoryTotal"
                style="margin-top: 20px; text-align: right;"
              ></el-pagination>
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
                  <el-select 
                    v-model="stockInForm.reagentId" 
                    placeholder="请选择或输入新试剂名称" 
                    style="width: 100%;"
                    filterable
                    allow-create
                    default-first-option
                  >
                    <el-option
                      v-for="item in reagentList"
                      :key="item.id"
                      :label="item.name"
                      :value="item.id"
                    ></el-option>
                  </el-select>
                  <div v-if="isNewReagent" style="margin-top: 10px; padding: 10px; background: #f8f9fa; border-radius: 4px;">
                    <el-tag size="small" type="warning" style="margin-bottom: 10px;">新试剂录入</el-tag>
                    <el-row :gutter="10">
                      <el-col :span="12">
                        <el-form-item label="分类" label-width="60px" required>
                          <el-select v-model="stockInForm.categoryId" placeholder="选择分类" style="width: 100%;">
                            <el-option v-for="c in categoryList" :key="c.id" :label="c.name" :value="c.id"></el-option>
                          </el-select>
                        </el-form-item>
                      </el-col>
                      <el-col :span="12">
                        <el-form-item label="危险性" label-width="60px">
                          <el-select v-model="stockInForm.dangerLevel" placeholder="选择等级" style="width: 100%;">
                            <el-option label="一般" value="一般"></el-option>
                            <el-option label="易燃" value="易燃"></el-option>
                            <el-option label="腐蚀" value="腐蚀"></el-option>
                            <el-option label="剧毒" value="剧毒"></el-option>
                            <el-option label="易爆" value="易爆"></el-option>
                          </el-select>
                        </el-form-item>
                      </el-col>
                    </el-row>
                    <el-row :gutter="10">
                      <el-col :span="12">
                         <el-form-item label="CAS号" label-width="60px">
                           <el-input v-model="stockInForm.casNo" placeholder="CAS号"></el-input>
                         </el-form-item>
                      </el-col>
                      <el-col :span="12">
                         <el-form-item label="单位" label-width="60px" required>
                           <el-input v-model="stockInForm.unit" placeholder="如 g, ml"></el-input>
                         </el-form-item>
                      </el-col>
                    </el-row>
                    <el-row :gutter="10">
                      <el-col :span="24">
                         <el-form-item label="规格" label-width="60px">
                           <el-input v-model="stockInForm.specification" placeholder="如 500ml/瓶"></el-input>
                         </el-form-item>
                      </el-col>
                    </el-row>
                  </div>
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
              <el-tabs v-model="applicationTab" @tab-click="handleApplicationTabClick">
                <el-tab-pane label="待审批" name="pending">
                  <el-table :data="pendingList" border>
                    <el-table-column prop="applicationNo" label="申请单号" width="180"></el-table-column>
                    <el-table-column prop="reagentName" label="试剂名称" width="150"></el-table-column>
                    <el-table-column prop="quantity" label="申请数量" width="100"></el-table-column>
                    <el-table-column prop="applicantName" label="申请人" width="100"></el-table-column>
                    <el-table-column prop="purpose" label="用途" min-width="200" show-overflow-tooltip></el-table-column>
                    <el-table-column prop="createTime" label="申请时间" width="160"></el-table-column>
                    <el-table-column label="操作" width="280">
                      <template slot-scope="scope">
                        <el-button size="mini" @click="handlePrecheck(scope.row)">AI预审</el-button>
                        <el-button size="mini" type="success" @click="handleReview(scope.row, 'APPROVED')">通过</el-button>
                        <el-button size="mini" type="danger" @click="handleReview(scope.row, 'REJECTED')">拒绝</el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                </el-tab-pane>
                <el-tab-pane label="审批记录" name="processed">
                  <el-table :data="processedList" border>
                    <el-table-column prop="applicationNo" label="申请单号" width="180"></el-table-column>
                    <el-table-column prop="reagentName" label="试剂名称" width="150"></el-table-column>
                    <el-table-column prop="quantity" label="申请数量" width="100"></el-table-column>
                    <el-table-column prop="applicantName" label="申请人" width="100"></el-table-column>
                    <el-table-column prop="status" label="状态" width="100">
                      <template slot-scope="scope">
                        <el-tag v-if="scope.row.status === 'APPROVED'" type="success">已通过</el-tag>
                        <el-tag v-else-if="scope.row.status === 'COMPLETED'" type="success">已完成</el-tag>
                        <el-tag v-else-if="scope.row.status === 'REJECTED'" type="danger">已拒绝</el-tag>
                        <el-tag v-else type="info">已取消</el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column prop="reviewTime" label="审批时间" width="160"></el-table-column>
                    <el-table-column prop="reviewRemark" label="审批备注" min-width="150" show-overflow-tooltip></el-table-column>
                  </el-table>
                </el-tab-pane>
              </el-tabs>
            </el-card>
          </div>
          
          <!-- 出库管理 -->
          <div v-show="activeMenu === 'stockOut'">
            <el-card>
              <div slot="header">出库管理</div>
              <el-form :model="stockOutForm" ref="stockOutForm" label-width="120px">
                <el-form-item label="关联申请单">
                  <el-select v-model="stockOutForm.applicationId" placeholder="选择申请单（可选）" clearable style="width: 100%;" @change="handleApplicationChange">
                    <el-option
                      v-for="item in approvedList"
                      :key="item.id"
                      :label="`${item.applicationNo} - ${item.reagentName} - ${item.applicantName}`"
                      :value="item.id"
                    ></el-option>
                  </el-select>
                </el-form-item>
                <el-form-item label="库存" required>
                  <el-select v-model="stockOutForm.inventoryId" placeholder="请选择库存" style="width: 100%;">
                    <el-option
                      v-for="item in stockOutInventoryList"
                      :key="item.id"
                      :label="`${item.reagentName} - ${item.batchNo} - 库存：${item.quantity} - 位置：${item.locationName}`"
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
              <el-tabs v-model="warningTab" style="margin-bottom: 15px;">
                <el-tab-pane label="全部" name="ALL"></el-tab-pane>
                <el-tab-pane label="库存不足" name="LOW"></el-tab-pane>
                <el-tab-pane label="试剂临期" name="EXPIRING"></el-tab-pane>
                <el-tab-pane label="已过期" name="EXPIRED"></el-tab-pane>
              </el-tabs>
              <el-table :data="filteredWarningList" border>
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
                    <el-table-column prop="batchNo" label="批次号" width="150"></el-table-column>
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

          <!-- 系统公告 -->
          <div v-show="activeMenu === 'announcements'">
            <AnnouncementList :can-publish="false" />
          </div>

          <!-- 反馈处理 -->
          <div v-show="activeMenu === 'feedback'">
            <el-card>
              <div slot="header">
                <i class="el-icon-chat-line-square"></i> 反馈处理
              </div>
              <el-tabs v-model="feedbackTab" @tab-click="handleFeedbackTabClick">
                <el-tab-pane label="待处理反馈" name="pending">
                  <el-table :data="pendingFeedbackList" border>
                    <el-table-column prop="feedbackType" label="类型" width="100">
                      <template slot-scope="scope">
                        <el-tag v-if="scope.row.feedbackType === 'REAGENT'" type="warning">试剂问题</el-tag>
                        <el-tag v-else-if="scope.row.feedbackType === 'SYSTEM'" type="danger">系统问题</el-tag>
                        <el-tag v-else type="info">建议</el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column prop="userName" label="提交人" width="100"></el-table-column>
                    <el-table-column prop="title" label="标题" width="140" show-overflow-tooltip></el-table-column>
                    <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip></el-table-column>
                    <el-table-column prop="createTime" label="提交时间" width="160"></el-table-column>
                    <el-table-column label="操作" width="240" fixed="right">
                      <template slot-scope="scope">
                        <el-button size="mini" type="text" @click="viewTeacherFeedbackDetail(scope.row)">查看详情</el-button>
                        <el-button size="mini" type="success" @click="handleFeedbackItem(scope.row, 'RESOLVED')">已解决</el-button>
                        <el-button size="mini" type="warning" @click="handleFeedbackItem(scope.row, 'PROCESSING')">处理中</el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                </el-tab-pane>
                <el-tab-pane label="已处理反馈" name="handled">
                  <el-table :data="handledFeedbackList" border>
                    <el-table-column prop="feedbackType" label="类型" width="100">
                      <template slot-scope="scope">
                        <el-tag v-if="scope.row.feedbackType === 'REAGENT'" type="warning">试剂问题</el-tag>
                        <el-tag v-else-if="scope.row.feedbackType === 'SYSTEM'" type="danger">系统问题</el-tag>
                        <el-tag v-else type="info">建议</el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column prop="userName" label="提交人" width="100"></el-table-column>
                    <el-table-column prop="title" label="标题" width="140" show-overflow-tooltip></el-table-column>
                    <el-table-column prop="content" label="内容" min-width="180" show-overflow-tooltip></el-table-column>
                    <el-table-column prop="status" label="状态" width="90">
                      <template slot-scope="scope">
                        <el-tag v-if="scope.row.status === 'PROCESSING'" type="primary">处理中</el-tag>
                        <el-tag v-else-if="scope.row.status === 'RESOLVED'" type="success">已解决</el-tag>
                        <el-tag v-else type="info">已关闭</el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column prop="handleRemark" label="处理备注" width="140" show-overflow-tooltip></el-table-column>
                    <el-table-column prop="createTime" label="提交时间" width="160"></el-table-column>
                    <el-table-column label="操作" width="100" fixed="right">
                      <template slot-scope="scope">
                        <el-button size="mini" type="text" @click="viewTeacherFeedbackDetail(scope.row)">查看详情</el-button>
                      </template>
                    </el-table-column>
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
    
    <!-- 反馈详情对话框 -->
    <el-dialog title="反馈详情" :visible.sync="teacherFeedbackDetailVisible" width="700px">
      <el-descriptions :column="1" border v-if="currentTeacherFeedback">
        <el-descriptions-item label="反馈类型">
          <el-tag v-if="currentTeacherFeedback.feedbackType === 'REAGENT'" type="warning">试剂问题</el-tag>
          <el-tag v-else-if="currentTeacherFeedback.feedbackType === 'SYSTEM'" type="danger">系统问题</el-tag>
          <el-tag v-else type="info">建议</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="提交人">{{ currentTeacherFeedback.userName || '未知' }}</el-descriptions-item>
        <el-descriptions-item label="标题">{{ currentTeacherFeedback.title }}</el-descriptions-item>
        <el-descriptions-item label="详细内容">
          <div style="white-space: pre-wrap; line-height: 1.6;">{{ currentTeacherFeedback.content }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ currentTeacherFeedback.createTime }}</el-descriptions-item>
        <el-descriptions-item label="当前状态">
          <el-tag v-if="currentTeacherFeedback.status === 'PENDING'" type="warning">待处理</el-tag>
          <el-tag v-else-if="currentTeacherFeedback.status === 'PROCESSING'" type="primary">处理中</el-tag>
          <el-tag v-else-if="currentTeacherFeedback.status === 'RESOLVED'" type="success">已解决</el-tag>
          <el-tag v-else type="info">已关闭</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="处理备注" v-if="currentTeacherFeedback.handleRemark">
          <div style="white-space: pre-wrap; line-height: 1.6;">{{ currentTeacherFeedback.handleRemark }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="处理时间" v-if="currentTeacherFeedback.handleTime">{{ currentTeacherFeedback.handleTime }}</el-descriptions-item>
      </el-descriptions>
      <span slot="footer">
        <el-button @click="teacherFeedbackDetailVisible = false">关闭</el-button>
        <el-button v-if="currentTeacherFeedback && currentTeacherFeedback.status === 'PENDING'" type="success" @click="handleFeedbackFromDetail('RESOLVED')">标记为已解决</el-button>
        <el-button v-if="currentTeacherFeedback && currentTeacherFeedback.status === 'PENDING'" type="warning" @click="handleFeedbackFromDetail('PROCESSING')">标记为处理中</el-button>
      </span>
    </el-dialog>

    <!-- AI预审结果对话框 -->
    <el-dialog title="AI预审建议" :visible.sync="precheckDialogVisible" width="500px">
        <div style="line-height: 1.8; font-size: 15px;">
             <div v-for="(item, index) in precheckResult" :key="index" style="margin-bottom: 8px;">{{ item }}</div>
        </div>
        <span slot="footer">
            <el-button @click="precheckDialogVisible = false">关 闭</el-button>
            <el-button type="danger" @click="handleReviewFromPrecheck('REJECTED')">拒绝申请</el-button>
            <el-button type="success" @click="handleReviewFromPrecheck('APPROVED')">通过申请</el-button>
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
import { getLocationList, getCategoryList } from '@/api/base'
import { getAnnouncements } from '@/api/announcement'
import { getPendingFeedback, getAllFeedback, handleFeedback } from '@/api/feedback'
import { exportInventory, exportStockIn, exportStockOut, getFifoSuggestion, getReplenishSuggestions } from '@/api/export'
import AnnouncementList from '@/components/AnnouncementList.vue'

export default {
  name: 'TeacherIndex',
  components: {
    AnnouncementList
  },
  computed: {
    filteredWarningList() {
      if (this.warningTab === 'ALL') return this.warningList
      return this.warningList.filter(item => item.status === this.warningTab)
    },
    isNewReagent() {
      // 如果 reagentId 是字符串，说明是手动输入的名称（新试剂）
      // 如果是数字，说明是选择的现有试剂ID
      return typeof this.stockInForm.reagentId === 'string' && this.stockInForm.reagentId !== ''
    }
  },
  data() {
    return {
      activeMenu: 'inventory',
      userInfo: this.$store.state.userInfo,
      searchName: '',
      inventoryList: [],
      // 分页相关
      inventoryPage: 1,
      inventoryPageSize: 10,
      inventoryTotal: 0,
      stockOutInventoryList: [],
      warningTab: 'ALL',
      // 预警相关
      warningCount: 0,
      warningsViewed: false,
      pendingApplicationsCount: 0,
      applicationsViewed: false,
      pendingFeedbackCount: 0,
      feedbackViewed: false,
      recordsViewed: true, // 初始设为已读
      filterStatus: '',
      sortField: 'update_time',
      reagentList: [],
      categoryList: [],
      locationList: [],
      approvedList: [],
      pendingList: [],
      precheckDialogVisible: false,
      precheckResult: [],
      currentApplication: null,
      warningList: [],
      warningList: [],
      pendingFeedbackList: [],
      handledFeedbackList: [],
      // 出入库记录分页
      stockInRecords: [],
      stockInPage: 1,
      stockInPageSize: 10,
      stockInTotal: 0,
      stockOutRecords: [],
      stockOutPage: 1,
      stockOutPageSize: 10,
      stockOutTotal: 0,
      recordTab: 'in',
      applicationTab: 'pending', // 申请审批tab
      processedList: [], // 已审批列表
      feedbackTab: 'pending',
      replenishSuggestions: [],
      // 反馈详情相关
      teacherFeedbackDetailVisible: false,
      currentTeacherFeedback: null,
      stockInForm: {
        reagentId: null,
        batchNo: '',
        quantity: 1,
        expiryDate: '',
        supplier: '',
        purchasePrice: 0,
        locationId: null,
        remark: '',
        casNo: '',
        specification: '',
        unit: '',
        dangerLevel: '一般',
        categoryId: null,
        manufacturer: '',
        supplierLeadTime: 7
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
    this.loadCategories()
    this.loadAnnouncements()
    this.loadPendingApplications()
    this.loadWarningList()
    this.loadFeedback()
  },
  methods: {
    handleMenuSelect(index) {
      this.activeMenu = index
      if (index === 'applications') {
        this.loadPendingApplications()
        this.applicationsViewed = true
        if (this.applicationTab === 'processed') {
          this.loadProcessedApplications()
        }
      } else if (index === 'warning') {
        this.loadWarningList()
        this.warningsViewed = true
      } else if (index === 'records') {
        this.loadStockRecords()
        this.recordsViewed = true
      } else if (index === 'stockOut') {
        this.loadApprovedApplications()
        this.loadInventoryForStockOut()
      } else if (index === 'announcements') {
        this.loadAnnouncements()
      } else if (index === 'feedback') {
        this.loadFeedback()
        this.feedbackViewed = true
      } else if (index === 'ai-assistant') {
        this.$router.push('/ai-assistant')
      }
    },
    loadInventory() {
      // 同时更新预警数
      this.loadWarningList()
      this.searchLoading = true
      const params = {
        name: this.searchName,
        page: this.inventoryPage,
        size: this.inventoryPageSize
      }
      if (this.filterStatus) {
        params.status = this.filterStatus
      }
      if (this.sortField) {
        params.sortField = this.sortField
        params.sortOrder = 'DESC'
      }
      getInventoryList(params).then(res => {
        if (res.data && res.data.records) {
          this.inventoryList = res.data.records
          this.inventoryTotal = res.data.total || 0
        } else {
          this.inventoryList = res.data || []
          this.inventoryTotal = this.inventoryList.length
        }
      })
    },
    handleFilterChange() {
      this.inventoryPage = 1
      this.loadInventory()
    },
    handleInventorySizeChange(val) {
      this.inventoryPageSize = val
      this.inventoryPage = 1
      this.loadInventory()
    },
    handleInventoryPageChange(val) {
      this.inventoryPage = val
      this.loadInventory()
    },
    loadReagents() {
      getReagentList().then(res => {
        this.reagentList = res.data
      })
    },
    loadLocations() {
      getLocationList().then(res => {
        this.locationList = res.data || []
      })
    },
    loadCategories() {
      getCategoryList().then(res => {
        this.categoryList = res.data || []
      })
    },
    loadAnnouncements() {
      getAnnouncements({ role: 'TEACHER' }).then(res => {
        this.announcementList = res.data || []
      })
    },
    loadPendingApplications() {
      getPendingApplications().then(res => {
        const list = res.data || []
        if (list.length > this.pendingApplicationsCount) {
          this.applicationsViewed = false
        }
        this.pendingList = list
        this.pendingApplicationsCount = list.length
      })
    },
    handleApplicationTabClick() {
      if (this.applicationTab === 'pending') {
        this.loadPendingApplications()
      } else {
        this.loadProcessedApplications()
      }
    },
    loadProcessedApplications() {
      getAllApplications().then(res => {
        this.processedList = (res.data || []).filter(item => item.status !== 'PENDING')
      })
    },
    loadApprovedApplications() {
      getAllApplications().then(res => {
        this.approvedList = res.data.filter(item => item.status === 'APPROVED')
      })
    },
    loadWarningList() {
      getWarningList().then(res => {
        const list = res.data || []
        if (list.length > this.warningCount) {
          this.warningsViewed = false
        }
        this.warningList = list
        this.warningCount = list.length
      })
      // 加载AI补货建议
      this.loadReplenishSuggestions()
    },
    loadReplenishSuggestions() {
      getReplenishSuggestions({ model: 'qwen2.5:3b' }).then(res => {
        this.replenishSuggestions = res.data || []
      }).catch(() => {
        this.replenishSuggestions = []
      })
    },
    loadFeedback() {
      if (this.feedbackTab === 'pending') {
        getPendingFeedback().then(res => {
          const list = res.data || []
          if (list.length > this.pendingFeedbackCount) {
            this.feedbackViewed = false
          }
          this.pendingFeedbackList = list
          this.pendingFeedbackCount = list.length
        })
      } else {
        // 加载已处理的反馈
        getAllFeedback().then(res => {
          this.handledFeedbackList = (res.data || []).filter(f => f.status !== 'PENDING')
        })
      }
    },
    handleFeedbackTabClick() {
      this.loadFeedback()
    },
    viewTeacherFeedbackDetail(row) {
      this.currentTeacherFeedback = row
      this.teacherFeedbackDetailVisible = true
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
        }).catch(() => {})
      }).catch(() => {})
    },
    handleFeedbackFromDetail(status) {
      this.$prompt('请输入处理备注', '处理反馈', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPlaceholder: '请输入处理备注'
      }).then(({ value }) => {
        handleFeedback(this.currentTeacherFeedback.id, {
          status: status,
          remark: value || ''
        }).then(() => {
          this.$message.success('处理成功')
          this.teacherFeedbackDetailVisible = false
          this.loadFeedback()
        }).catch(() => {})
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
    submitStockIn() {
      // 构造提交数据
      const data = { ...this.stockInForm }
      if (this.isNewReagent) {
        data.reagentName = this.stockInForm.reagentId // 输入的名称
        data.reagentId = null
      }
      stockIn(data).then(() => {
        this.$message.success('入库成功')
        this.recordsViewed = false // 产生新纪录，设为未读
        this.resetStockInForm()
        this.loadInventory()
      })
    },
    handleStockInHint() {
      let name = ''
      let unit = ''
      let reagentId = null

      if (this.isNewReagent) {
        name = this.stockInForm.reagentId
        unit = this.stockInForm.unit
        // 新试剂时不传ID
      } else {
        const reagent = this.reagentList.find(x => x.id === this.stockInForm.reagentId)
        if (reagent) {
          name = reagent.name
          unit = reagent.unit || ''
          reagentId = reagent.id
        }
      }
      // Pass reagentId to enable category-based suggestions
      
      // 添加全屏加载蒙版，阻止其他操作
      const loading = this.$loading({
        lock: true,
        text: 'AI正在智能分析...',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
      })

      stockInHint({ 
        reagentId: reagentId,
        name, 
        batchNo: this.stockInForm.batchNo, 
        quantity: this.stockInForm.quantity, 
        quantity: this.stockInForm.quantity, 
        unit, 
        model: 'qwen2.5:3b' 
      })
        .then(res => {
          loading.close() // 关闭加载
          const d = res.data || {}
          const tips = []
          if (d.casNo) tips.push('CAS号：' + d.casNo)
          if (d.specification) tips.push('建议规格：' + d.specification)
          if (d.dangerLevel) tips.push('危险等级：' + d.dangerLevel)
          if (d.defaultExpiryMonths) tips.push('建议有效期（月）：' + d.defaultExpiryMonths)
          if (d.recommendedLocation) tips.push('位置建议：' + d.recommendedLocation)
          if (d.suggestedRemark) tips.push('建议备注：' + d.suggestedRemark)
          if (d.batchUnique === false) tips.push('⚠️ 警告：批次号已存在，建议更换')
          if (d.reasoning) tips.push('\n推理：' + d.reasoning)
          
          // 使用$confirm代替$alert，提供更明确的应用/取消选项
          this.$confirm(tips.join('\n') || 'AI暂无建议', 'AI入库提示', {
            confirmButtonText: '应用建议',
            cancelButtonText: '取消',
            type: 'info'
          }).then(() => {
            // Apply suggestions
            if (d.casNo && this.isNewReagent) this.stockInForm.casNo = d.casNo
            if (d.specification && this.isNewReagent) this.stockInForm.specification = d.specification
            if (d.dangerLevel && this.isNewReagent) this.stockInForm.dangerLevel = d.dangerLevel
            
            if (d.defaultExpiryMonths) {
               // Calculate date
               const date = new Date()
               date.setMonth(date.getMonth() + d.defaultExpiryMonths)
               this.stockInForm.expiryDate = date.toISOString().split('T')[0]
            }
            if (d.suggestedRemark) {
                this.stockInForm.remark = d.suggestedRemark
            }
            // Try to match location name
            if (d.recommendedLocation) {
                const match = this.locationList.find(l => l.fullLocation.includes(d.recommendedLocation) || d.recommendedLocation.includes(l.fullLocation))
                if (match) {
                    this.stockInForm.locationId = match.id
                } else if (!d.suggestedLocationId) {
                     this.$message.info('AI建议位置 "' + d.recommendedLocation + '" 未能精确匹配到系统预设位置，请手动选择')
                }
            }
            // Prioritize historical suggestion if available
            if (d.suggestedLocationId) this.stockInForm.locationId = d.suggestedLocationId
            if (d.suggestedUnitPrice) this.stockInForm.purchasePrice = d.suggestedUnitPrice
            if (d.suggestedSupplier) this.stockInForm.supplier = d.suggestedSupplier
            if (d.suggestedRemark && !d.reasoning) { // If historical remark exists (usually prioritized in backend logic but here we check AI vs History)
                 // actually backend returns suggestedRemark from history OR AI. 
                 // If history has remark, backend sets it. If not, AI sets it.
                 this.stockInForm.remark = d.suggestedRemark
            }
          })
          .catch(action => {
            // 点击"取消"或关闭
            if (action === 'cancel') {
              this.$message.info('已取消应用建议')
            }
          })
        })
        .catch(err => {
          loading.close() // 出错也要关闭加载
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
        locationId: null,
        remark: '',
        casNo: '',
        specification: '',
        unit: '',
        dangerLevel: '一般',
        categoryId: null,
        manufacturer: '',
        supplierLeadTime: 7
      }
    },
    selectApplication(app) {
      this.stockOutForm.recipientName = app.applicantName
      this.stockOutForm.recipientId = app.applicantId
      this.stockOutForm.quantity = app.quantity
      this.stockOutForm.purpose = app.purpose
    },
    handleApplicationChange(val) {
      if (!val) {
        this.resetStockOutForm()
        this.loadInventoryForStockOut()
        return
      }
      const app = this.approvedList.find(x => x.id === val)
      if (app) {
        this.selectApplication(app)
        this.loadInventoryForStockOut(app.reagentName)
        
        // 自动获取先进先出建议
        getFifoSuggestion(app.reagentId).then(res => {
          if (res.data && res.data.inventoryId) {
            this.stockOutForm.inventoryId = res.data.inventoryId
            this.$message.info(`AI建议使用批次：${res.data.batchNo} (先进先出原则)`)
          }
        })
      }
    },
    loadInventoryForStockOut(name = '') {
      const params = {
        page: 1,
        size: 100, // 获取较多记录供选择
        status: 'NORMAL' // 只显示正常状态库存
      }
      if (name) {
        params.name = name
      }
      getInventoryList(params).then(res => {
        this.stockOutInventoryList = res.data.records || res.data || []
      })
    },
    submitStockOut() {
      if (!this.stockOutForm.inventoryId) {
        this.$message.warning('请选择库存')
        return
      }
      
      // 获取选中的库存信息
      const selectedInventory = this.stockOutInventoryList.find(x => x.id === this.stockOutForm.inventoryId)
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
            }).catch(() => {})
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
        this.recordsViewed = false // 产生新纪录，设为未读
        this.resetStockOutForm()
        this.loadInventory()
        this.loadPendingApplications()
        this.loadApprovedApplications()
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
      this.loadInventoryForStockOut()
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
          this.loadInventory() // 可能影响库存预警
        }).catch(() => {})
      }).catch(() => {})
    },
    handlePrecheck(row) {
      this.currentApplication = row
      approvePrecheck({ applicationId: row.id, model: 'qwen2.5:3b' }).then(res => {
        const d = res.data || {}
        const tips = []
        // 使用HTML标签增加可读性
        tips.push('📦 库存是否充足：' + (d.stockEnough ? '✅ 是' : '❌ 否'))
        if (d.fifoSuggestion) tips.push('💡 FIFO建议：' + d.fifoSuggestion)
        if (d.substitutes && d.substitutes.length) tips.push('🔄 替代建议：' + d.substitutes.join('；'))
        if (d.cautions && d.cautions.length) tips.push('⚠️ 注意事项：' + d.cautions.join('；'))
        if (d.summary) tips.push('📝 摘要：' + d.summary)
        
        this.precheckResult = tips
        this.precheckDialogVisible = true
      }).catch(err => {
        this.$message.error('AI预审失败：' + (err.message || ''))
      })
    },
    handleReviewFromPrecheck(status) {
      this.precheckDialogVisible = false
      if (this.currentApplication) {
        this.handleReview(this.currentApplication, status)
      }
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
      }).catch(() => {})
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
.menu-badge {
  margin-left: 8px;
}

.menu-badge /deep/ .el-badge__content {
  line-height: 18px;
}
</style>







