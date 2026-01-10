module.exports = {
  pages: {
    index: {
      // 入口文件
      entry: 'src/main.js',
      // 使用我们自定义的模板，而不是默认 public/index.html
      template: 'public/index-template.html',
      // 输出的 html 文件名仍然叫 index.html
      filename: 'index.html'
    }
  },
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







