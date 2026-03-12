<template>
  <div>
    <el-card>
      <div slot="header">
        <span>二维码管理</span>
        <el-button-group style="float: right;">
          <el-button type="primary" size="small" @click="showGenerateDialog">
            <i class="el-icon-plus"></i> 生成二维码
          </el-button>
          <el-button type="success" size="small" @click="batchGenerate">
            <i class="el-icon-files"></i> 批量生成
          </el-button>
          <el-button type="info" size="small" @click="printAllQrcodes" v-if="list.filter(item => item.qrcodeUrl).length > 0">
            <i class="el-icon-printer"></i> 批量打印
          </el-button>
        </el-button-group>
      </div>

      <!-- 搜索栏 -->
      <el-form :inline="true" style="margin-bottom: 20px;">
        <el-form-item label="试剂名称">
          <el-input v-model="searchName" placeholder="搜索试剂名称" clearable style="width: 200px;"></el-input>
        </el-form-item>
        <el-form-item label="批次号">
          <el-input v-model="searchBatchNo" placeholder="搜索批次号" clearable style="width: 150px;"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadList">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="list" border v-loading="loading">
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="id" label="ID" width="60"></el-table-column>
        <el-table-column prop="reagentName" label="试剂名称" width="150"></el-table-column>
        <el-table-column prop="batchNo" label="批次号" width="120"></el-table-column>
        <el-table-column prop="specification" label="规格" width="100"></el-table-column>
        <el-table-column prop="location" label="存放位置" width="180" show-overflow-tooltip></el-table-column>
        <el-table-column label="二维码" width="140" align="center">
          <template slot-scope="scope">
            <div v-if="scope.row.qrcodeUrl" style="text-align: center;">
              <img :src="scope.row.qrcodeUrl" style="width: 100px; height: 100px; cursor: pointer; border: 1px solid #ddd; padding: 4px;" @click="previewQrcode(scope.row)" />
            </div>
            <el-button v-else size="mini" type="primary" @click="generateSingleQrcode(scope.row)">
              <i class="el-icon-plus"></i> 生成
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="生成时间" width="160"></el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" @click="previewQrcode(scope.row)" v-if="scope.row.qrcodeUrl">预览</el-button>
            <el-button size="mini" type="success" @click="downloadQrcode(scope.row)" v-if="scope.row.qrcodeUrl">下载</el-button>
            <el-button size="mini" type="warning" @click="printQrcode(scope.row)" v-if="scope.row.qrcodeUrl">打印</el-button>
            <el-button size="mini" type="danger" @click="deleteQrcode(scope.row)" v-if="scope.row.qrcodeUrl">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-if="total > 0"
        style="margin-top: 20px; text-align: center; padding: 10px 0;"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="currentPage"
        :page-sizes="[10, 20, 50]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        background>
      </el-pagination>
    </el-card>

    <!-- 生成二维码对话框 -->
    <el-dialog title="生成二维码" :visible.sync="generateDialogVisible" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="选择试剂">
          <el-select v-model="form.inventoryId" placeholder="请选择库存试剂" style="width: 100%;" filterable @change="updateQrcodeContent">
            <el-option 
              v-for="item in inventoryList" 
              :key="item.id" 
              :label="`${item.reagentName} - ${item.batchNo} (${item.location})`" 
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="二维码尺寸">
          <el-radio-group v-model="form.size">
            <el-radio :label="128">小 (128×128)</el-radio>
            <el-radio :label="256">中 (256×256)</el-radio>
            <el-radio :label="512">大 (512×512)</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="二维码内容">
          <el-input type="textarea" v-model="form.content" :rows="5" placeholder="自动生成JSON格式数据"></el-input>
        </el-form-item>
        <el-form-item label="预览">
          <div v-if="previewQrcodeData" style="text-align: center; padding: 20px; background: #f5f7fa; border-radius: 4px;">
            <img :src="previewQrcodeData" style="width: 200px; height: 200px;" />
            <div style="margin-top: 10px; color: #909399; font-size: 12px;">实时预览</div>
          </div>
          <div v-else style="text-align: center; padding: 20px; color: #999;">
            请选择试剂查看预览
          </div>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="generateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmGenerate" :disabled="!form.inventoryId">生成并保存</el-button>
      </span>
    </el-dialog>

    <!-- 批量生成对话框 -->
    <el-dialog title="批量生成二维码" :visible.sync="batchDialogVisible" width="600px">
      <el-alert title="将为所有未生成二维码的库存试剂批量生成二维码" type="info" style="margin-bottom: 20px;" :closable="false"></el-alert>
      <el-table :data="toBatchList" border max-height="400">
        <el-table-column prop="reagentName" label="试剂名称"></el-table-column>
        <el-table-column prop="batchNo" label="批次号" width="120"></el-table-column>
        <el-table-column prop="location" label="位置" width="150"></el-table-column>
      </el-table>
      <div style="margin-top: 20px; text-align: center;">
        共 {{ toBatchList.length }} 条记录需要生成二维码
      </div>
      <span slot="footer">
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmBatchGenerate" :loading="generating">开始生成</el-button>
      </span>
    </el-dialog>

    <!-- 预览二维码对话框 -->
    <el-dialog title="二维码预览与打印" :visible.sync="previewDialogVisible" width="600px">
      <div v-if="currentQrcode" style="text-align: center;">
        <!-- 二维码显示 -->
        <div style="background: #fff; padding: 20px; display: inline-block; border: 2px dashed #ddd;">
          <img :src="currentQrcode.qrcodeUrl" style="width: 300px; height: 300px; display: block;" />
          <div style="margin-top: 10px; font-weight: bold; font-size: 16px;">{{ currentQrcode.reagentName }}</div>
          <div style="color: #666; font-size: 14px;">批次号: {{ currentQrcode.batchNo }}</div>
        </div>
        
        <!-- 详细信息 -->
        <el-descriptions :column="1" border style="margin-top: 20px;">
          <el-descriptions-item label="试剂ID">{{ currentQrcode.id }}</el-descriptions-item>
          <el-descriptions-item label="试剂名称">{{ currentQrcode.reagentName }}</el-descriptions-item>
          <el-descriptions-item label="批次号">{{ currentQrcode.batchNo }}</el-descriptions-item>
          <el-descriptions-item label="规格">{{ currentQrcode.specification }}</el-descriptions-item>
          <el-descriptions-item label="存放位置">{{ currentQrcode.location }}</el-descriptions-item>
          <el-descriptions-item label="生成时间">{{ currentQrcode.createTime }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <span slot="footer">
        <el-button @click="previewDialogVisible = false">关闭</el-button>
        <el-button type="success" @click="downloadQrcode(currentQrcode)">
          <i class="el-icon-download"></i> 下载图片
        </el-button>
        <el-button type="warning" @click="printQrcode(currentQrcode)">
          <i class="el-icon-printer"></i> 打印标签
        </el-button>
      </span>
    </el-dialog>

  </div>
</template>

<script>
import QRCode from 'qrcode'

export default {
  name: 'QrcodeManagement',
  data() {
    return {
      loading: false,
      list: [],
      searchName: '',
      searchBatchNo: '',
      currentPage: 1,
      pageSize: 10,
      total: 0,
      generateDialogVisible: false,
      batchDialogVisible: false,
      previewDialogVisible: false,
      scanDialogVisible: false,
      generating: false,
      form: {
        inventoryId: null,
        content: '',
        size: 256
      },
      inventoryList: [],
      toBatchList: [],
      currentQrcode: null,
      scanResult: null,
      previewQrcodeData: null
    }
  },
  mounted() {
    this.loadList()
    this.loadInventoryList()
  },
  methods: {
    async loadList() {
      this.loading = true
      try {
        // 调用后端API获取库存列表
        const inventoryApi = await import('@/api/inventory')
        const params = {
          name: this.searchName,
          batchNo: this.searchBatchNo,
          page: this.currentPage,
          size: this.pageSize
        }
        
        const res = await inventoryApi.getInventoryList(params)
        let mockData = []
        
        // 处理返回的数据
        if (res.data && res.data.records) {
          // 分页数据
          mockData = res.data.records.map(item => ({
            id: item.id,
            reagentName: item.reagentName,
            batchNo: item.batchNo || 'N/A',
            specification: item.specification,
            location: item.locationName || item.location,
            casNo: item.casNo || '',
            expiryDate: item.expiryDate,
            qrcodeUrl: null,
            createTime: null
          }))
          this.total = res.data.total
        } else if (Array.isArray(res.data)) {
          // 列表数据
          mockData = res.data.map(item => ({
            id: item.id,
            reagentName: item.reagentName,
            batchNo: item.batchNo || 'N/A',
            specification: item.specification,
            location: item.locationName || item.location,
            casNo: item.casNo || '',
            expiryDate: item.expiryDate,
            qrcodeUrl: null,
            createTime: null
          }))
          this.total = mockData.length
        }
        
        // 从LocalStorage恢复已生成的二维码
        const savedQrcodes = this.loadQrcodesFromStorage()
        mockData.forEach(item => {
          const saved = savedQrcodes[item.id]
          if (saved) {
            item.qrcodeUrl = saved.qrcodeUrl
            item.createTime = saved.createTime
          }
        })
        
        this.list = mockData
        this.loading = false
      } catch (error) {
        console.error('加载列表失败:', error)
        this.$message.error('加载数据失败：' + (error.message || '未知错误'))
        this.loading = false
      }
    },
    loadInventoryList() {
      // 调用后端API获取库存列表供选择
      import('@/api/inventory').then(inventoryApi => {
        inventoryApi.getInventoryList({}).then(res => {
          let data = []
          if (res.data && res.data.records) {
            data = res.data.records
          } else if (Array.isArray(res.data)) {
            data = res.data
          }
          
          this.inventoryList = data.map(item => ({
            id: item.id,
            reagentName: item.reagentName,
            batchNo: item.batchNo || 'N/A',
            location: item.locationName || item.location,
            specification: item.specification,
            casNo: item.casNo || '',
            expiryDate: item.expiryDate
          }))
        }).catch(error => {
          console.error('加载库存列表失败:', error)
        })
      })
    },
    createQrcodeData(item) {
      // 创建易读的二维码文本内容
      const lines = [
        `【试剂库存信息】`,
        ``,
        `试剂名称：${item.reagentName || '-'}`,
        `批次号：${item.batchNo || '-'}`,
        `规格型号：${item.specification || '-'}`,
        `CAS编号：${item.casNo || '-'}`,
        `存放位置：${item.location || '-'}`,
        `有效期：${item.expiryDate || '-'}`,
        ``,
        `ID: ${item.id}`,
        `生成时间：${new Date().toLocaleString('zh-CN')}`
      ]
      return lines.join('\n')
    },
    async generateQrcodeImage(data, size = 256) {
      try {
        // 直接使用文本内容生成二维码
        const qrcodeDataUrl = await QRCode.toDataURL(data, {
          width: size,
          margin: 1,
          color: {
            dark: '#000000',
            light: '#FFFFFF'
          },
          errorCorrectionLevel: 'M'
        })
        
        return qrcodeDataUrl
      } catch (error) {
        console.error('生成二维码失败:', error)
        throw error
      }
    },
    showGenerateDialog() {
      this.form = {
        inventoryId: null,
        content: '',
        size: 256
      }
      this.previewQrcodeData = null
      this.generateDialogVisible = true
    },
    async updateQrcodeContent() {
      if (!this.form.inventoryId) {
        this.previewQrcodeData = null
        return
      }
      
      // 找到选中的试剂
      const selectedItem = this.inventoryList.find(item => item.id === this.form.inventoryId)
      if (!selectedItem) return
      
      // 创建二维码数据
      const qrData = this.createQrcodeData(selectedItem)
      this.form.content = qrData  // 直接使用文本内容
      
      // 生成预览
      try {
        this.previewQrcodeData = await this.generateQrcodeImage(qrData, this.form.size)
      } catch (error) {
        this.$message.error('生成预览失败')
      }
    },
    async confirmGenerate() {
      if (!this.form.inventoryId) {
        this.$message.warning('请选择试剂')
        return
      }
      
      try {
        const selectedItem = this.inventoryList.find(item => item.id === this.form.inventoryId)
        const qrData = this.createQrcodeData(selectedItem)
        const qrcodeUrl = await this.generateQrcodeImage(qrData, this.form.size)
        const createTime = new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-')
        
        // 保存到LocalStorage
        this.saveQrcodeToStorage(this.form.inventoryId, qrcodeUrl, createTime)
        
        // 在列表中更新
        const listItem = this.list.find(item => item.id === this.form.inventoryId)
        if (listItem) {
          listItem.qrcodeUrl = qrcodeUrl
          listItem.createTime = createTime
        }
        
        this.$message.success('二维码生成成功')
        this.generateDialogVisible = false
        this.loadList()
      } catch (error) {
        this.$message.error('生成二维码失败')
      }
    },
    async batchGenerate() {
      // 获取所有未生成二维码的试剂（不限于当前页）
      try {
        this.loading = true
        const inventoryApi = await import('@/api/inventory')
        
        // 获取所有库存数据（不分页）
        const res = await inventoryApi.getInventoryList({ size: 10000 })
        let allData = []
        
        if (res.data && res.data.records) {
          allData = res.data.records
        } else if (Array.isArray(res.data)) {
          allData = res.data
        }
        
        // 过滤出未生成二维码的试剂
        this.toBatchList = allData.map(item => ({
          id: item.id,
          reagentName: item.reagentName,
          batchNo: item.batchNo || 'N/A',
          location: item.locationName || item.location,
          specification: item.specification,
          casNo: item.casNo || '',
          expiryDate: item.expiryDate
        })).filter(item => {
          // 检查当前列表中是否已生成
          const existItem = this.list.find(i => i.id === item.id)
          return !existItem || !existItem.qrcodeUrl
        })
        
        this.loading = false
        
        if (this.toBatchList.length === 0) {
          this.$message.info('所有试剂都已生成二维码')
          return
        }
        
        this.batchDialogVisible = true
      } catch (error) {
        console.error('获取待生成列表失败:', error)
        this.$message.error('获取数据失败')
        this.loading = false
      }
    },
    async confirmBatchGenerate() {
      this.generating = true
      try {
        let successCount = 0
        this.$message.info(`开始批量生成 ${this.toBatchList.length} 个二维码，请稍候...`)
        
        for (const item of this.toBatchList) {
          const qrData = this.createQrcodeData(item)
          const qrcodeUrl = await this.generateQrcodeImage(qrData, 256)
          
          // 保存到LocalStorage
          const createTime = new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-')
          this.saveQrcodeToStorage(item.id, qrcodeUrl, createTime)
          
          // 更新列表中的数据（如果在当前页）
          const listItem = this.list.find(i => i.id === item.id)
          if (listItem) {
            listItem.qrcodeUrl = qrcodeUrl
            listItem.createTime = createTime
          }
          
          successCount++
          
          // TODO: 实际应该调用后端API保存二维码
          // await saveQrcode({ inventoryId: item.id, qrcodeUrl })
          
          // 模拟批量生成的延迟
          await new Promise(resolve => setTimeout(resolve, 50))
        }
        
        this.$message.success(`成功生成 ${successCount} 个二维码！`)
        this.generating = false
        this.batchDialogVisible = false
        
        // 刷新列表
        this.loadList()
      } catch (error) {
        this.$message.error('批量生成失败: ' + (error.message || '未知错误'))
        this.generating = false
      }
    },
    async generateSingleQrcode(row) {
      try {
        const qrData = this.createQrcodeData(row)
        const qrcodeUrl = await this.generateQrcodeImage(qrData, 256)
        const createTime = new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-')
        
        // 保存到LocalStorage
        this.saveQrcodeToStorage(row.id, qrcodeUrl, createTime)
        
        // 更新列表
        row.qrcodeUrl = qrcodeUrl
        row.createTime = createTime
        
        this.$message.success('二维码生成成功')
      } catch (error) {
        this.$message.error('生成二维码失败')
      }
    },
    previewQrcode(row) {
      this.currentQrcode = row
      this.previewDialogVisible = true
    },
    downloadQrcode(row) {
      if (!row.qrcodeUrl) {
        this.$message.warning('该试剂还未生成二维码')
        return
      }
      
      // 创建下载链接
      const link = document.createElement('a')
      link.href = row.qrcodeUrl
      link.download = `${row.reagentName}_${row.batchNo}_二维码.png`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      
      this.$message.success('二维码下载成功')
    },
    printQrcode(row) {
      if (!row.qrcodeUrl) {
        this.$message.warning('该试剂还未生成二维码')
        return
      }
      
      // 创建打印窗口
      const printWindow = window.open('', '_blank')
      const htmlContent = '<!DOCTYPE html>' +
        '<html><head><title>打印二维码标签</title>' +
        '<style>' +
        'body { margin: 0; padding: 20px; text-align: center; }' +
        '.label { border: 2px dashed #333; padding: 20px; display: inline-block; page-break-after: always; }' +
        'img { width: 300px; height: 300px; display: block; margin: 0 auto; }' +
        '.info { margin-top: 15px; font-family: Arial, sans-serif; }' +
        '.name { font-size: 18px; font-weight: bold; margin: 10px 0; }' +
        '.detail { font-size: 14px; color: #666; margin: 5px 0; }' +
        '</style></head><body>' +
        '<div class="label">' +
        '<img src="' + row.qrcodeUrl + '" />' +
        '<div class="info">' +
        '<div class="name">' + row.reagentName + '</div>' +
        '<div class="detail">批次号: ' + row.batchNo + '</div>' +
        '<div class="detail">规格: ' + row.specification + '</div>' +
        '<div class="detail">位置: ' + row.location + '</div>' +
        '</div></div>' +
        '<script>window.onload = function() { window.print(); window.onafterprint = function() { window.close(); } }</' + 'script>' +
        '</body></html>'
      
      printWindow.document.write(htmlContent)
      printWindow.document.close()
    },
    printAllQrcodes() {
      const qrcodeItems = this.list.filter(item => item.qrcodeUrl)
      if (qrcodeItems.length === 0) {
        this.$message.warning('没有可打印的二维码')
        return
      }
      
      // 创建打印窗口
      const printWindow = window.open('', '_blank')
      let htmlContent = '<!DOCTYPE html><html><head><title>批量打印二维码标签</title>' +
        '<style>' +
        'body { margin: 0; padding: 20px; }' +
        '.label { border: 2px dashed #333; padding: 20px; display: inline-block; page-break-after: always; margin: 10px; text-align: center; }' +
        'img { width: 250px; height: 250px; display: block; margin: 0 auto; }' +
        '.info { margin-top: 15px; font-family: Arial, sans-serif; }' +
        '.name { font-size: 16px; font-weight: bold; margin: 8px 0; }' +
        '.detail { font-size: 12px; color: #666; margin: 4px 0; }' +
        '</style></head><body>'
      
      qrcodeItems.forEach(item => {
        htmlContent += '<div class="label">' +
          '<img src="' + item.qrcodeUrl + '" />' +
          '<div class="info">' +
          '<div class="name">' + item.reagentName + '</div>' +
          '<div class="detail">批次: ' + item.batchNo + '</div>' +
          '<div class="detail">规格: ' + item.specification + '</div>' +
          '<div class="detail">位置: ' + item.location + '</div>' +
          '</div></div>'
      })
      
      htmlContent += '<script>window.onload = function() { window.print(); window.onafterprint = function() { window.close(); } }</' + 'script>' +
        '</body></html>'
      
      printWindow.document.write(htmlContent)
      printWindow.document.close()
      
      this.$message.success('准备打印 ' + qrcodeItems.length + ' 个二维码标签')
    },
    deleteQrcode(row) {
      this.$confirm('确定要删除该二维码吗？删除后可重新生成', '提示', {
        type: 'warning'
      }).then(() => {
        // 调用后端API删除
        row.qrcodeUrl = null
        row.createTime = null
        this.$message.success('删除成功')
      }).catch(() => {})
    },
    resetSearch() {
      this.searchName = ''
      this.searchBatchNo = ''
      this.currentPage = 1
      this.loadList()
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.currentPage = 1
      this.loadList()
    },
    handleCurrentChange(val) {
      this.currentPage = val
      this.loadList()
    },
    // LocalStorage操作方法
    saveQrcodeToStorage(id, qrcodeUrl, createTime) {
      try {
        const storageKey = 'reagent_qrcodes'
        const saved = JSON.parse(localStorage.getItem(storageKey) || '{}')
        saved[id] = { qrcodeUrl, createTime }
        localStorage.setItem(storageKey, JSON.stringify(saved))
      } catch (error) {
        console.error('保存二维码到LocalStorage失败:', error)
      }
    },
    loadQrcodesFromStorage() {
      try {
        const storageKey = 'reagent_qrcodes'
        return JSON.parse(localStorage.getItem(storageKey) || '{}')
      } catch (error) {
        console.error('从LocalStorage加载二维码失败:', error)
        return {}
      }
    },
    clearQrcodeStorage() {
      try {
        const storageKey = 'reagent_qrcodes'
        localStorage.removeItem(storageKey)
        this.$message.success('已清空本地二维码缓存')
      } catch (error) {
        console.error('清空LocalStorage失败:', error)
      }
    }
  }
}
</script>

<style scoped>
.el-table img {
  display: block;
  margin: 0 auto;
}

/* 二维码预览样式优化 */
.el-dialog__body {
  padding: 20px;
}

/* 打印样式 */
@media print {
  body * {
    visibility: hidden;
  }
  .label, .label * {
    visibility: visible;
  }
  .label {
    position: absolute;
    left: 0;
    top: 0;
  }
}
</style>

