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
  background-color: #f8fafc;
  display: flex;
  justify-content: center;
  align-items: center;
  background-image: radial-gradient(#e5e7eb 1px, transparent 1px);
  background-size: 20px 20px;
}

.login-box {
  width: 420px;
  padding: 48px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

.title {
  text-align: center;
  margin-bottom: 32px;
  color: #1e293b;
  font-size: 24px;
  font-weight: 700;
  letter-spacing: -0.025em;
}

.login-form {
  margin-top: 24px;
}

.login-form .el-form-item {
  margin-bottom: 20px;
}

.demo-account {
  margin-top: 24px;
  padding: 20px;
  background: #f1f5f9;
  border-radius: 12px;
  font-size: 13px;
  color: #475569;
  line-height: 1.6;
}

.demo-account p {
  margin-bottom: 4px;
}
</style>







