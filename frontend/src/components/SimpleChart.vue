<template>
  <div class="simple-chart">
    <div class="chart-title">{{ title }}</div>
    <div class="chart-content">
      <div v-for="(item, index) in data" :key="index" class="chart-item">
        <div class="item-label">{{ item.name }}</div>
        <div class="item-bar">
          <div 
            class="item-fill" 
            :style="{ 
              width: getPercentage(item.value) + '%',
              backgroundColor: item.color 
            }"
          ></div>
        </div>
        <div class="item-value">{{ item.value }}</div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SimpleChart',
  props: {
    title: {
      type: String,
      default: '图表'
    },
    data: {
      type: Array,
      default: () => []
    }
  },
  methods: {
    getPercentage(value) {
      if (!this.data.length) return 0
      const max = Math.max(...this.data.map(item => item.value))
      return max > 0 ? (value / max) * 100 : 0
    }
  }
}
</script>

<style scoped>
.simple-chart {
  padding: 20px;
}

.chart-title {
  font-size: 16px;
  font-weight: bold;
  text-align: center;
  margin-bottom: 20px;
  color: #333;
}

.chart-content {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.chart-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.item-label {
  min-width: 80px;
  font-size: 14px;
  color: #666;
}

.item-bar {
  flex: 1;
  height: 20px;
  background-color: #f0f0f0;
  border-radius: 10px;
  overflow: hidden;
}

.item-fill {
  height: 100%;
  border-radius: 10px;
  transition: width 0.3s ease;
}

.item-value {
  min-width: 40px;
  text-align: right;
  font-size: 14px;
  font-weight: bold;
  color: #333;
}
</style>


