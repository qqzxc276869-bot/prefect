<template>
  <div>
    <el-card>
      <div slot="header">
        <span>库位可视化</span>
        <el-radio-group v-model="viewMode" size="small" style="float: right;">
          <el-radio-button label="map">地图视图</el-radio-button>
          <el-radio-button label="list">列表视图</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 地图视图 -->
      <div v-if="viewMode === 'map'" style="width: 100%;">
        <div style="margin-bottom: 20px;">
          <el-select v-model="selectedRoom" placeholder="选择房间" style="width: 200px;" @change="loadMap" clearable>
            <el-option label="全部房间" value=""></el-option>
            <el-option v-for="room in roomOptions" :key="room.value" :label="room.label" :value="room.value"></el-option>
          </el-select>
          <el-button style="margin-left: 10px;" size="small" @click="loadStatistics" :loading="loading">刷新</el-button>
        </div>
        <div id="locationMapCanvas" style="width: 100%; min-height: 600px; height: calc(100vh - 300px); background: #f5f7fa; border: 1px solid #ddd; border-radius: 4px; position: relative; overflow: auto;">
          <!-- 模拟地图 -->
          <div v-for="(cabinet, index) in cabinets" :key="index" 
               :style="{position: 'absolute', left: cabinet.x + 'px', top: cabinet.y + 'px', width: '120px', height: '160px', border: '2px solid #409EFF', background: '#fff', borderRadius: '4px', padding: '10px', cursor: 'pointer', boxShadow: '0 2px 8px rgba(0,0,0,0.1)'}"
               @click="viewCabinet(cabinet)">
            <div style="font-weight: bold; text-align: center; margin-bottom: 8px; color: #409EFF;">{{ cabinet.name }}</div>
            <div style="font-size: 11px; color: #666; margin-bottom: 3px; word-break: break-all;">{{ cabinet.fullLocation }}</div>
            <div style="font-size: 12px; color: #666; margin-bottom: 5px;">试剂种类: {{ cabinet.count }}种</div>
            <div style="font-size: 12px; color: #666; margin-bottom: 8px;">占用率: {{ cabinet.rate }}%</div>
            <el-progress :percentage="cabinet.rate" :color="getProgressColor(cabinet.rate)" :stroke-width="8" style="margin-top: 10px;"></el-progress>
          </div>
        </div>
      </div>

      <!-- 列表视图 -->
      <div v-if="viewMode === 'list'" style="width: 100%;">
        <div style="margin-bottom: 10px;">
          <el-button size="small" @click="loadStatistics" :loading="loading">刷新</el-button>
        </div>
        <el-table :data="locationList" border stripe style="width: 100%;" v-loading="loading">
          <el-table-column prop="roomName" label="房间" width="120" align="center"></el-table-column>
          <el-table-column prop="fullLocation" label="完整位置" min-width="250"></el-table-column>
          <el-table-column prop="reagentCount" label="试剂种类" width="100" align="center"></el-table-column>
          <el-table-column prop="totalQuantity" label="总数量" width="100" align="center"></el-table-column>
          <el-table-column label="占用率" width="200" align="center">
            <template slot-scope="scope">
              <el-progress :percentage="scope.row.occupancyRate" :color="getProgressColor(scope.row.occupancyRate)"></el-progress>
            </template>
          </el-table-column>
          <el-table-column prop="attributes" label="描述" min-width="150" show-overflow-tooltip></el-table-column>
          <el-table-column label="操作" width="120" align="center" fixed="right">
            <template slot-scope="scope">
              <el-button size="mini" type="text" @click="viewLocationDetail(scope.row)">查看详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script>
import { getLocationStatistics, getLocationList } from '@/api/base'

export default {
  name: 'LocationMap',
  data() {
    return {
      viewMode: 'map',
      selectedRoom: '',
      cabinets: [],
      locationList: [],
      allLocations: [],
      roomOptions: [],
      loading: false
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        // 加载位置列表
        const locationRes = await getLocationList()
        this.allLocations = locationRes.data || []
        
        // 提取房间选项
        const rooms = [...new Set(this.allLocations.map(loc => loc.roomName).filter(Boolean))]
        this.roomOptions = rooms.map(room => ({
          label: room,
          value: room
        }))
        
        if (this.roomOptions.length > 0 && !this.selectedRoom) {
          this.selectedRoom = this.roomOptions[0].value
        }
        
        // 加载位置统计
        await this.loadStatistics()
      } catch (error) {
        this.$message.error('加载数据失败：' + (error.message || '未知错误'))
      } finally {
        this.loading = false
      }
    },
    
    async loadStatistics() {
      try {
        const res = await getLocationStatistics()
        const statistics = res.data || []
        
        // 更新列表视图数据
        this.locationList = statistics.map(stat => ({
          locationId: stat.locationId,
          fullLocation: stat.fullLocation || `${stat.roomName || ''}-${stat.cabinetNo || ''}-${stat.shelfNo || ''}`,
          roomName: stat.roomName,
          cabinetNo: stat.cabinetNo,
          shelfNo: stat.shelfNo,
          reagentCount: stat.reagentCount || 0,
          totalQuantity: stat.totalQuantity || 0,
          occupancyRate: stat.occupancyRate || 0,
          attributes: stat.description || ''
        }))
        
        // 更新地图视图数据
        this.loadMap()
      } catch (error) {
        this.$message.error('加载统计信息失败：' + (error.message || '未知错误'))
      }
    },
    
    loadMap() {
      // 根据选中的房间筛选位置
      const filteredLocations = this.locationList.filter(loc => {
        if (!this.selectedRoom) return true
        return loc.roomName === this.selectedRoom
      })
      
      // 为每个位置分配坐标（简单的网格布局）
      this.cabinets = filteredLocations.map((loc, index) => {
        const col = index % 4
        const row = Math.floor(index / 4)
        return {
          id: loc.locationId,
          name: loc.cabinetNo || loc.fullLocation,
          fullLocation: loc.fullLocation,
          x: 50 + col * 180,
          y: 50 + row * 200,
          count: loc.reagentCount,
          rate: loc.occupancyRate,
          totalQuantity: loc.totalQuantity
        }
      })
    },
    
    viewCabinet(cabinet) {
      const location = this.locationList.find(loc => loc.locationId === cabinet.id)
      if (location) {
        this.$message({
          message: `位置：${location.fullLocation}\n试剂种类：${location.reagentCount}种\n总数量：${location.totalQuantity}\n占用率：${location.occupancyRate}%`,
          type: 'info',
          duration: 3000
        })
      }
    },
    
    viewLocationDetail(row) {
      this.$message({
        message: `位置：${row.fullLocation}\n房间：${row.roomName || '未知'}\n柜号：${row.cabinetNo || '未知'}\n层架：${row.shelfNo || '未知'}\n试剂种类：${row.reagentCount}种\n总数量：${row.totalQuantity}\n占用率：${row.occupancyRate}%`,
        type: 'info',
        duration: 3000
      })
    },
    
    getProgressColor(rate) {
      if (rate < 60) return '#67C23A'
      if (rate < 85) return '#E6A23C'
      return '#F56C6C'
    }
  },
  watch: {
    selectedRoom() {
      this.loadMap()
    }
  }
}
</script>




