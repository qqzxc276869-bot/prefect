import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}')
  },
  mutations: {
    SET_USER_INFO(state, userInfo) {
      state.userInfo = userInfo
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    },
    CLEAR_USER_INFO(state) {
      state.userInfo = {}
      localStorage.removeItem('userInfo')
      localStorage.removeItem('token')
    }
  },
  actions: {
    setUserInfo({ commit }, userInfo) {
      commit('SET_USER_INFO', userInfo)
    },
    logout({ commit }) {
      commit('CLEAR_USER_INFO')
    }
  }
})






