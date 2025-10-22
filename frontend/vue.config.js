module.exports = {
  devServer: {
    port: 8082,
    proxy: {
      '/api': {
        // 后端存在 context-path: /backend
        target: 'http://localhost:8080/backend',
        changeOrigin: true
      }
    }
  },
  lintOnSave: false
}







