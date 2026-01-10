<template>
  <div class="announcement-list">
    <el-card>
      <div slot="header" class="card-header">
        <span><i class="el-icon-bell"></i> 系统公告</span>
        <div>
          <el-button 
            v-if="canPublish" 
            size="mini" 
            type="primary" 
            icon="el-icon-plus" 
            @click="$emit('publish')"
          >发布公告</el-button>
          <el-button 
            size="mini" 
            type="text" 
            icon="el-icon-refresh" 
            @click="loadAnnouncements"
          >刷新</el-button>
        </div>
      </div>
      
      <el-empty v-if="!announcementList.length && !loading" description="暂无公告"></el-empty>
      
      <el-timeline v-else>
        <el-timeline-item
          v-for="item in announcementList"
          :key="item.id"
          :timestamp="item.publishTime"
          :type="getPriorityTag(item.priority)"
          placement="top"
        >
          <el-card shadow="hover">
            <div class="announcement-item">
              <div class="announcement-header">
                <h4 class="announcement-title">{{ item.title }}</h4>
                <div class="announcement-meta">
                  <el-tag :type="getPriorityTag(item.priority)" size="mini">
                    {{ getPriorityLabel(item.priority) }}
                  </el-tag>
                  <el-tag size="mini" style="margin-left: 8px;">
                    {{ getAudienceLabel(item.audience) }}
                  </el-tag>
                  <span v-if="canPublish" style="margin-left: 8px;">
                    <el-button 
                      size="mini" 
                      type="danger" 
                      icon="el-icon-delete" 
                      @click="handleDelete(item)"
                    >删除</el-button>
                  </span>
                </div>
              </div>
              <div class="announcement-content">{{ item.content }}</div>
              <div class="announcement-footer">
                <small style="color: #909399;">
                  发布人：{{ item.creatorName || '系统' }} | 
                  发布时间：{{ item.publishTime }}
                </small>
              </div>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
      
      <div v-if="loading" style="text-align: center; padding: 20px;">
        <i class="el-icon-loading"></i> 加载中...
      </div>
    </el-card>
  </div>
</template>

<script>
import { getAnnouncements, deleteAnnouncement } from '@/api/announcement'

export default {
  name: 'AnnouncementList',
  props: {
    canPublish: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      announcementList: [],
      loading: false
    }
  },
  mounted() {
    this.loadAnnouncements()
  },
  methods: {
    loadAnnouncements() {
      this.loading = true
      getAnnouncements().then(res => {
        this.announcementList = res.data || []
      }).catch(err => {
        this.$message.error('加载公告失败：' + (err.message || '未知错误'))
      }).finally(() => {
        this.loading = false
      })
    },
    
    handleDelete(item) {
      this.$confirm(`确定要删除公告「${item.title}」吗？`, '提示', {
        type: 'warning'
      }).then(() => {
        deleteAnnouncement(item.id).then(() => {
          this.$message.success('删除成功')
          this.loadAnnouncements()
        }).catch(err => {
          this.$message.error('删除失败：' + (err.message || '未知错误'))
        })
      }).catch(() => {})
    },
    
    getAudienceLabel(value) {
      const map = {
        'ALL': '全部人员',
        'TEACHER': '老师',
        'STUDENT': '学生',
        'ADMIN': '管理员'
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
    }
  }
}
</script>

<style scoped>
.announcement-list {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  color: #4f46e5;
}

.announcement-item {
  padding: 10px 0;
}

.announcement-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.announcement-title {
  margin: 0;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.announcement-meta {
  display: flex;
  align-items: center;
}

.announcement-content {
  margin: 15px 0;
  line-height: 1.6;
  color: #606266;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.announcement-footer {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #EBEEF5;
}
</style>



