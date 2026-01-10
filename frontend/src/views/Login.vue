<template>
  <div class="login-container">
    <div class="login-box">
      <h1 class="title">实验室化学试剂与耗材库存管理系统</h1>
      <el-form :model="loginForm" :rules="rules" ref="loginForm" class="login-form">
        <el-form-item prop="username">
          <el-input 
            v-model="loginForm.username" 
            placeholder="请输入用户名"
            prefix-icon="el-icon-user"
          ></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input 
            v-model="loginForm.password" 
            type="password"
            placeholder="请输入密码"
            prefix-icon="el-icon-lock"
            @keyup.enter.native="handleLogin"
          ></el-input>
        </el-form-item>
        <el-form-item>
          <el-button 
            type="primary" 
            style="width: 100%;" 
            :loading="loading"
            @click="handleLogin"
          >登 录</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import { login } from '@/api/auth'

export default {
  name: 'Login',
  data() {
    return {
      loginForm: {
        username: '',
        password: ''
      },
      rules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' }
        ]
      },
      loading: false
    }
  },
  methods: {
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true
          login(this.loginForm).then(res => {
            this.loading = false
            // 保存token和用户信息
            localStorage.setItem('token', res.data.token)
            const userInfo = {
              userId: res.data.userId,
              username: res.data.username,
              realName: res.data.realName,
              role: res.data.role,
              department: res.data.department
            }
            this.$store.dispatch('setUserInfo', userInfo)
            
            this.$message.success('登录成功')
            
            // 根据角色跳转到不同页面
            if (res.data.role === 'STUDENT') {
              this.$router.push('/student')
            } else if (res.data.role === 'TEACHER') {
              this.$router.push('/teacher')
            } else if (res.data.role === 'ADMIN') {
              this.$router.push('/admin')
            }
          }).catch(() => {
            this.loading = false
          })
        }
      })
    }
  }
}
</script>

<style scoped>
.login-container {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-box {
  width: 450px;
  padding: 40px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
  font-size: 24px;
}

.login-form {
  margin-top: 20px;
}

.demo-account {
  margin-top: 20px;
  padding: 15px;
  background: #f5f5f5;
  border-radius: 5px;
  font-size: 12px;
  color: #666;
  line-height: 1.8;
}

.demo-account p {
  margin: 0;
}
</style>







