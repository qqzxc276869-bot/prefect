<template>
  <div>
    <el-card>
      <div slot="header">
        <span>课题组管理</span>
        <el-button type="primary" size="small" style="float: right;" @click="showAddDialog">添加课题组</el-button>
      </div>
      
      <el-table :data="groupList" border>
        <el-table-column prop="groupName" label="课题组名称" width="150"></el-table-column>
        <el-table-column prop="groupCode" label="课题组编码" width="120"></el-table-column>
        <el-table-column prop="piName" label="负责人(PI)" width="100"></el-table-column>
        <el-table-column prop="department" label="所属部门" width="150"></el-table-column>
        <el-table-column prop="totalBudget" label="总预算(元)" width="120">
          <template slot-scope="scope">
            {{ scope.row.totalBudget | currency }}
          </template>
        </el-table-column>
        <el-table-column prop="usedBudget" label="已使用(元)" width="120">
          <template slot-scope="scope">
            {{ scope.row.usedBudget | currency }}
          </template>
        </el-table-column>
        <el-table-column prop="availableBudget" label="可用预算(元)" width="120">
          <template slot-scope="scope">
            <span :style="{color: scope.row.availableBudget < scope.row.totalBudget * 0.1 ? 'red' : ''}">
              {{ scope.row.availableBudget | currency }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="budgetYear" label="预算年度" width="100"></el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.status === 'ACTIVE'" type="success">活跃</el-tag>
            <el-tag v-else-if="scope.row.status === 'SUSPENDED'" type="warning">暂停</el-tag>
            <el-tag v-else type="info">关闭</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250">
          <template slot-scope="scope">
            <el-button size="mini" @click="viewMembers(scope.row)">成员</el-button>
            <el-button size="mini" type="primary" @click="editItem(scope.row)">编辑</el-button>
            <el-button size="mini" type="warning" @click="manageBudget(scope.row)">预算</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 添加/编辑课题组对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="600px">
      <el-form :model="currentItem" label-width="120px">
        <el-form-item label="课题组名称" required>
          <el-input v-model="currentItem.groupName"></el-input>
        </el-form-item>
        <el-form-item label="课题组编码" required>
          <el-input v-model="currentItem.groupCode"></el-input>
        </el-form-item>
        <el-form-item label="负责人(PI)" required>
          <el-select v-model="currentItem.piId" style="width: 100%;" filterable>
            <el-option 
              v-for="user in teacherList" 
              :key="user.id" 
              :label="user.realName" 
              :value="user.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="所属部门">
          <el-input v-model="currentItem.department"></el-input>
        </el-form-item>
        <el-form-item label="总预算(元)" required>
          <el-input-number v-model="currentItem.totalBudget" :min="0" :precision="2" style="width: 100%;"></el-input-number>
        </el-form-item>
        <el-form-item label="预算年度">
          <el-date-picker 
            v-model="currentItem.budgetYear" 
            type="year" 
            placeholder="选择年份"
            value-format="yyyy"
            style="width: 100%;">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="currentItem.status" style="width: 100%;">
            <el-option label="活跃" value="ACTIVE"></el-option>
            <el-option label="暂停" value="SUSPENDED"></el-option>
            <el-option label="关闭" value="CLOSED"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input type="textarea" :rows="3" v-model="currentItem.description"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveItem">保存</el-button>
      </span>
    </el-dialog>
    
    <!-- 成员管理对话框 -->
    <el-dialog title="课题组成员" :visible.sync="memberDialogVisible" width="700px">
      <div style="margin-bottom: 15px;">
        <el-button type="primary" size="small" @click="showAddMemberDialog">添加成员</el-button>
      </div>
      <el-table :data="memberList" border>
        <el-table-column prop="userName" label="姓名" width="120"></el-table-column>
        <el-table-column prop="role" label="角色" width="120">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.role === 'PI'" type="danger">负责人</el-tag>
            <el-tag v-else-if="scope.row.role === 'RESEARCHER'" type="warning">研究员</el-tag>
            <el-tag v-else type="info">学生</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="joinDate" label="加入日期" width="120"></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.status === 'ACTIVE'" type="success">在组</el-tag>
            <el-tag v-else type="info">已离组</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template slot-scope="scope">
            <el-button size="mini" type="danger" @click="removeMember(scope.row)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
    
    <!-- 添加成员对话框 -->
    <el-dialog title="添加成员" :visible.sync="addMemberDialogVisible" width="500px">
      <el-form :model="newMember" label-width="100px">
        <el-form-item label="用户" required>
          <el-select v-model="newMember.userId" style="width: 100%;" filterable>
            <el-option 
              v-for="user in allUsers" 
              :key="user.id" 
              :label="`${user.realName}(${user.role})`" 
              :value="user.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="组内角色">
          <el-select v-model="newMember.role" style="width: 100%;">
            <el-option label="负责人(PI)" value="PI"></el-option>
            <el-option label="研究员" value="RESEARCHER"></el-option>
            <el-option label="学生" value="STUDENT"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="加入日期">
          <el-date-picker v-model="newMember.joinDate" type="date" value-format="yyyy-MM-dd" style="width: 100%;"></el-date-picker>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="addMemberDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="addMember">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getResearchGroupList, saveResearchGroup, getGroupMembers, addGroupMember, removeGroupMember } from '@/api/researchGroup'
import { getUserList } from '@/api/user'

export default {
  name: 'ResearchGroupManagement',
  filters: {
    currency(value) {
      if (!value) return '0.00'
      return parseFloat(value).toFixed(2)
    }
  },
  data() {
    return {
      groupList: [],
      teacherList: [],
      allUsers: [],
      dialogVisible: false,
      dialogTitle: '',
      currentItem: {
        groupName: '',
        groupCode: '',
        piId: null,
        department: '',
        totalBudget: 0,
        budgetYear: new Date().getFullYear().toString(),
        status: 'ACTIVE',
        description: ''
      },
      memberDialogVisible: false,
      currentGroup: null,
      memberList: [],
      addMemberDialogVisible: false,
      newMember: {
        userId: null,
        role: 'STUDENT',
        joinDate: new Date().toISOString().split('T')[0]
      }
    }
  },
  mounted() {
    this.loadList()
    this.loadUsers()
  },
  methods: {
    loadList() {
      getResearchGroupList().then(response => {
        if (response.code === 200) {
          this.groupList = response.data
        }
      })
    },
    loadUsers() {
      getUserList().then(response => {
        if (response.code === 200) {
          this.allUsers = response.data
          this.teacherList = response.data.filter(u => u.role === 'TEACHER' || u.role === 'ADMIN')
        }
      })
    },
    showAddDialog() {
      this.dialogTitle = '添加课题组'
      this.currentItem = {
        groupName: '',
        groupCode: '',
        piId: null,
        department: '',
        totalBudget: 0,
        budgetYear: new Date().getFullYear().toString(),
        status: 'ACTIVE',
        description: ''
      }
      this.dialogVisible = true
    },
    editItem(row) {
      this.dialogTitle = '编辑课题组'
      this.currentItem = { ...row }
      this.dialogVisible = true
    },
    saveItem() {
      if (!this.currentItem.groupName || !this.currentItem.groupCode || !this.currentItem.piId) {
        this.$message.warning('请填写必填项')
        return
      }
      saveResearchGroup(this.currentItem).then(response => {
        if (response.code === 200) {
          this.$message.success('保存成功')
          this.dialogVisible = false
          this.loadList()
        } else {
          this.$message.error(response.message || '保存失败')
        }
      })
    },
    viewMembers(row) {
      this.currentGroup = row
      this.memberDialogVisible = true
      getGroupMembers(row.id).then(response => {
        if (response.code === 200) {
          this.memberList = response.data
        }
      })
    },
    showAddMemberDialog() {
      this.newMember = {
        userId: null,
        role: 'STUDENT',
        joinDate: new Date().toISOString().split('T')[0]
      }
      this.addMemberDialogVisible = true
    },
    addMember() {
      if (!this.newMember.userId) {
        this.$message.warning('请选择用户')
        return
      }
      const params = {
        groupId: this.currentGroup.id,
        userId: this.newMember.userId,
        role: this.newMember.role,
        joinDate: this.newMember.joinDate
      }
      addGroupMember(params).then(response => {
        if (response.code === 200) {
          this.$message.success('添加成功')
          this.addMemberDialogVisible = false
          this.viewMembers(this.currentGroup)
        } else {
          this.$message.error(response.message || '添加失败')
        }
      })
    },
    removeMember(row) {
      this.$confirm('确认移除该成员吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        removeGroupMember(row.id).then(response => {
          if (response.code === 200) {
            this.$message.success('移除成功')
            this.viewMembers(this.currentGroup)
          }
        })
      })
    },
    manageBudget(row) {
      this.$message.info('预算管理功能开发中...')
      // 可以跳转到专门的预算管理页面
    }
  }
}
</script>

<style scoped>
</style>








