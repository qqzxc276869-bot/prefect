<template>
  <div>
    <el-card>
      <div slot="header">
        <span>库位可视化</span>
        <el-radio-group v-model="viewMode" size="small" style="float: right;">
          <el-radio-button label="map">地图视图</el-radio-button>
          <el-radio-button label="list">列表视图</el-radio-button>
          <el-radio-button label="heat">热力图</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 地图视图 -->
      <div v-if="viewMode === 'map'">
        <div style="margin-bottom: 20px;">
          <el-select v-model="selectedRoom" placeholder="选择房间" style="width: 200px;" @change="loadMap">
            <el-option label="化学实验室A" value="A"></el-option>
            <el-option label="化学实验室B" value="B"></el-option>
            <el-option label="生物实验室" value="C"></el-option>
          </el-select>
        </div>
        <div id="locationMapCanvas" style="width: 100%; height: 600px; background: #f5f7fa; border: 1px solid #ddd; border-radius: 4px; position: relative;">
          <!-- 模拟地图 -->
          <div v-for="(cabinet, index) in cabinets" :key="index" 
               :style="{position: 'absolute', left: cabinet.x + 'px', top: cabinet.y + 'px', width: '100px', height: '150px', border: '2px solid #409EFF', background: '#fff', borderRadius: '4px', padding: '10px', cursor: 'pointer'}"
               @click="viewCabinet(cabinet)">
            <div style="font-weight: bold; text-align: center; margin-bottom: 5px;">{{ cabinet.name }}</div>
            <div style="font-size: 12px; color: #666;">试剂数: {{ cabinet.count }}</div>
            <div style="font-size: 12px; color: #666;">占用率: {{ cabinet.rate }}%</div>
            <el-progress :percentage="cabinet.rate" :color="getProgressColor(cabinet.rate)" style="margin-top: 10px;"></el-progress>
          </div>
        </div>
      </div>

      <!-- 列表视图 -->
      <div v-if="viewMode === 'list'">
        <el-table :data="locationList" border>
          <el-table-column prop="fullLocation" label="位置" width="200"></el-table-column>
          <el-table-column prop="reagentCount" label="试剂数量" width="100"></el-table-column>
          <el-table-column prop="capacity" label="容量" width="80"></el-table-column>
          <el-table-column label="占用率" width="200">
            <template slot-scope="scope">
              <el-progress :percentage="scope.row.occupancyRate" :color="getProgressColor(scope.row.occupancyRate)"></el-progress>
            </template>
          </el-table-column>
          <el-table-column prop="attributes" label="属性" show-overflow-tooltip></el-table-column>
          <el-table-column label="操作" width="120">
            <template slot-scope="scope">
              <el-button size="mini" type="text" @click="viewLocationDetail(scope.row)">查看详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 热力图 -->
      <div v-if="viewMode === 'heat'">
        <div id="heatmapChart" style="height: 500px;"></div>
      </div>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'LocationMap',
  data() {
    return {
      viewMode: 'map',
      selectedRoom: 'A',
      cabinets: [],
      locationList: []
    }
  },
  mounted() {
    this.loadMap()
    this.loadList()
  },
  methods: {
    loadMap() {
      this.cabinets = [
        { name: 'A01', x: 50, y: 50, count: 15, rate: 75 },
        { name: 'A02', x: 200, y: 50, count: 12, rate: 60 },
        { name: 'A03', x: 350, y: 50, count: 18, rate: 90 },
        { name: 'A04', x: 50, y: 250, count: 8, rate: 40 },
        { name: 'A05', x: 200, y: 250, count: 14, rate: 70 }
      ]
    },
    loadList() {
      this.locationList = [
        { fullLocation: '化学实验室A-A01-L1', reagentCount: 15, capacity: 20, occupancyRate: 75, attributes: '通风，防爆' },
        { fullLocation: '化学实验室A-A01-L2', reagentCount: 12, capacity: 20, occupancyRate: 60, attributes: '通风' },
        { fullLocation: '化学实验室B-B01-L1', reagentCount: 18, capacity: 20, occupancyRate: 90, attributes: '通风，防爆，避光' }
      ]
    },
    viewCabinet(cabinet) {
      this.$message.info(`查看${cabinet.name}柜详情`)
    },
    viewLocationDetail(row) {
      this.$message.info(`查看${row.fullLocation}详情`)
    },
    getProgressColor(rate) {
      if (rate < 60) return '#67C23A'
      if (rate < 85) return '#E6A23C'
      return '#F56C6C'
    }
  }
}
</script>


