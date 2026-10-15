<!-- eslint-disable vue/html-self-closing -->
<script setup lang="ts">
import type { Ref } from 'vue'
import type { AllInfo } from '@/api/info/getInfo'
import { inject, onMounted, ref, useTemplateRef } from 'vue'

import { useRouter } from 'vue-router'
import { getInfoById } from '@/api/info/getInfo'
import { deleteAccount, updateEmailPush, updateUserInfo, uploadAvatar } from '@/api/info/updateInfo'

import { updatePassword } from '@/api/LoginAndRegister/forgetPwd'
import { sendCode } from '@/api/LoginAndRegister/utils'
import { showMsg } from '@/components/MessageBox'
import { usePWA } from '@/composables/usePWA'
import { useUserStore } from '@/store/userStore'
import {
  levelClassHandler,
  levelExpHandler,
  levelNameHandler,
} from '@/utils/level'

const router = useRouter()

// 来自 DefaultLayout 提供的是否为宽屏（>768px）
const isPC = inject('isPC', ref(false)) as Ref<boolean>

const { userInfo, setToken, setRefreshToken } = useUserStore()
const { pwaExperienceEnabled } = usePWA()
const allInfo = ref<AllInfo>({
  avatarURL: '',
  ban: '',
  email: '',
  intro: '',
  name: '',
  phone: '',
  punishnum: 0,
  score: 0,
  userID: 0,
  emailpush: false,
})
const VCode = useTemplateRef('VCode')
const password1 = useTemplateRef('password1')
const password2 = useTemplateRef('password2')

async function codeHandler() {
  try {
    if (!allInfo.value)
      return
    const data = await sendCode(allInfo.value.email, 1)
    if (!data)
      return

    showMsg('验证码发过去啦')
  }
  catch (e) {
    showMsg('发送验证码失败')
    console.error(e)
  }
}

async function updatePasswordFunc() {
  if (!password1.value || !password2.value || !VCode.value) {
    return
  }
  if (password1.value.value === '' || password2.value.value === '') {
    showMsg('密码不能为空')
    return
  }
  if (password1.value.value !== password2.value.value) {
    showMsg('两次密码不一致')
    return
  }
  try {
    const res = await updatePassword(
      userInfo.email,
      password1.value.value,
      password2.value.value,
      VCode.value.value,
    )
    showMsg(res.msg)
  }
  catch (e) {
    showMsg('更新密码失败')
    console.error(e)
  }
}

async function uploadAvatarFunc(e: Event) {
  try {
    if (!allInfo.value)
      return
    const el = e.target as HTMLInputElement
    const file = el.files?.[0]
    if (!file)
      return
    const res = await uploadAvatar(file)
    allInfo.value.avatarURL = res.data.avatarURL
    showMsg('头像更新成功')
  }
  catch (e) {
    console.error(e)
    showMsg('失败了:-(')
  }
}

/**
 * @description 更新用户信息。试图阻止空格名字，但是后端没有阻止
 */
async function updateUserInfoFunc() {
  if (!allInfo.value)
    return
  if (/^\s+|\s+$/.test(allInfo.value.name)) {
    showMsg('用户名不能以空格作为开头或者结尾')
    return
  }
  const res = await updateUserInfo(
    allInfo.value.avatarURL,
    allInfo.value.intro,
    allInfo.value.name,
    allInfo.value.userID,
  )
  showMsg(res.msg)
}

/**
 * @description 删除所有信息
 */
function logout() {
  if (confirm('确定要退出登录吗')) {
    localStorage.removeItem('userInfo')
    localStorage.removeItem('token')
    localStorage.removeItem('email')
    localStorage.removeItem('password')
    localStorage.removeItem('rememberMe')
    setToken('')
    setRefreshToken('')
    router.push('/')
    window.location.reload()
  }
}
const isPushDisabled = ref()
onMounted(async () => {
  allInfo.value = await getInfoById(userInfo.userID)
  isPushDisabled.value = allInfo.value.emailpush
})

async function togglePush() {
  try {
    if (!allInfo.value)
      return
    await updateEmailPush(allInfo.value.userID)
    isPushDisabled.value = !isPushDisabled.value
    showMsg(`邮件推送已${isPushDisabled.value ? '启用' : '禁用'}`)
  }
  catch (e) {
    console.error(e)
    showMsg('失败了')
  }
}

function togglePWAExperience() {
  pwaExperienceEnabled.value = !pwaExperienceEnabled.value
  showMsg(`电脑端样式新体验已${pwaExperienceEnabled.value ? '启用' : '关闭'}`)
}

const deleteCode = ref('')

async function sendDeleteCode() {
  if (!allInfo.value?.email) {
    showMsg('邮箱信息不存在')
    return
  }
  try {
    await sendCode(allInfo.value.email, 1)
    showMsg('验证码已发送到邮箱')
  }
  catch (e) {
    showMsg('发送验证码失败')
    console.error(e)
  }
}

async function deleteAccountFunc() {
  if (!confirm('确定要注销账户吗？此操作不可撤销，所有数据将被永久删除！')) {
    return
  }
  if (!deleteCode.value) {
    showMsg('请先输入验证码')
    return
  }
  try {
    const res = await deleteAccount(
      allInfo.value.email,
      allInfo.value.phone,
      deleteCode.value,
    )
    showMsg(res.msg || '账号已注销')
    logout()
  }
  catch (e) {
    showMsg('注销失败')
    console.error(e)
  }
}
</script>

<template>
  <div class="root page-section">
    <h2 class="page-heading">
      设置
    </h2>

    <!-- 个人信息 -->
    <section class="panel">
      <h3 class="panel-title">
        个人信息
      </h3>
      <div class="avatar-row">
        <img :src="allInfo?.avatarURL" alt="用户头像" class="avatar-img">
        <label for="fileInput" class="fileInput avatar-upload-btn">修改头像</label>
        <input id="fileInput" type="file" accept="image/*" style="display: none" @change="uploadAvatarFunc">
      </div>

      <div class="user-details">
        <p><strong>ID：</strong>{{ allInfo?.userID }}</p>
        <p><strong>邮箱：</strong>{{ allInfo?.email }}</p>
        <div class="exp-row">
          <strong>经验：</strong>
          <span class="level" :class="levelClassHandler(allInfo?.score || 0)">{{ levelNameHandler(allInfo?.score || 0) }}
          </span>
          <div class="progress-container">
            <div
              class="progress-bar" :style="{
                width:
                  `${((allInfo?.score || 0)
                    / parseInt(
                      levelExpHandler(
                        allInfo?.score || 0,
                      ),
                    ))
                    * 100
                  }%`,
              }"
            />
          </div>
          <span class="exp-text">
            {{ allInfo?.score }} / {{ levelExpHandler(allInfo?.score || 0) }}
          </span>
          <span class="level level-next">{{
            levelNameHandler(
              parseInt(levelExpHandler(allInfo?.score || 0)),
            )
          }}
          </span>
        </div>
      </div>

      <div class="form-group">
        <span>用户名</span>
        <input v-model="allInfo.name" type="text">
      </div>
      <div class="form-group">
        <span>简介</span>
        <input v-model="allInfo.intro" type="text">
      </div>
      <button type="button" class="btn-primary btn-auto" @click="updateUserInfoFunc">
        修改信息
      </button>
    </section>

    <!-- 重置密码 -->
    <section class="panel">
      <h3 class="panel-title">
        重置密码
      </h3>
      <div class="form-group">
        <span>验证码</span>
        <div class="inline-field">
          <input ref="VCode" type="text" placeholder="请输入验证码">
          <button type="button" class="btn-outline" @click="codeHandler">
            发送验证码
          </button>
        </div>
      </div>
      <div class="form-group">
        <span>新密码</span>
        <input ref="password1" type="password" placeholder="请输入新密码">
      </div>
      <div class="form-group">
        <span>确认密码</span>
        <input ref="password2" type="password" placeholder="请再次输入新密码">
      </div>
      <button type="button" class="btn-primary btn-auto" @click="updatePasswordFunc">
        重置密码
      </button>
    </section>

    <!-- 偏好 -->
    <section class="panel">
      <h3 class="panel-title">
        偏好
      </h3>
      <div class="toggle-container">
        <span>启用邮件推送</span>
        <div
          class="toggle-switch"
          :class="{ active: isPushDisabled }"
          role="switch"
          :aria-checked="!!isPushDisabled"
          tabindex="0"
          @click="togglePush"
          @keydown.enter.prevent="togglePush"
        >
          <span class="slider" />
        </div>
      </div>
      <div v-if="isPC" class="toggle-container">
        <span>启用电脑端样式新体验</span>
        <div
          class="toggle-switch"
          :class="{ active: pwaExperienceEnabled }"
          role="switch"
          :aria-checked="!!pwaExperienceEnabled"
          tabindex="0"
          @click="togglePWAExperience"
          @keydown.enter.prevent="togglePWAExperience"
        >
          <span class="slider" />
        </div>
      </div>
    </section>

    <!-- 账号操作 -->
    <section class="panel">
      <h3 class="panel-title">
        账号
      </h3>
      <div class="account-actions">
        <button type="button" class="btn-outline" @click="router.push('/oauth2-apps')">
          授权管理
        </button>
        <button type="button" class="btn-outline" @click="logout">
          退出登录
        </button>
      </div>
    </section>

    <!-- 危险区 -->
    <section class="panel danger-zone">
      <h3 class="panel-title danger-title">
        注销账户
      </h3>
      <p class="danger-hint">
        此操作不可撤销，账户数据将被永久删除
      </p>
      <div class="danger-row">
        <input v-model="deleteCode" type="text" placeholder="输入邮箱验证码">
        <button type="button" class="btn-outline" @click="sendDeleteCode">
          发送验证码
        </button>
        <button type="button" class="solid btn-danger" @click="deleteAccountFunc">
          注销账户
        </button>
      </div>
    </section>
  </div>
</template>

<style scoped>
.root {
  color: var(--color-text);
  width: 100%;
  max-width: 46rem;
  margin-inline: auto;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.panel {
  background: var(--surface-1);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  padding: 1.25rem 1.35rem;
}

.panel-title {
  margin: 0 0 1rem;
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-text);
}

.avatar-row {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
}

.avatar-img {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  object-fit: cover;
  background: var(--surface-2);
  border: 1px solid var(--color-border);
}

.avatar-upload-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: auto !important;
  min-height: 38px;
  padding: 0 1rem;
  margin: 0;
  border: 1px solid var(--color-border) !important;
  border-bottom: 1px solid var(--color-border) !important;
  border-radius: var(--radius-md);
  background: var(--surface-1);
  color: var(--color-text);
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
  transition:
    border-color 0.2s ease,
    color 0.2s ease;
}

.avatar-upload-btn:hover {
  border-color: var(--accent) !important;
  color: var(--accent);
  background: var(--surface-1);
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  margin-bottom: 1.1rem;
  font-size: 0.9rem;
}

.user-details p {
  margin: 0;
  color: var(--color-text-secondary);
}

.user-details strong {
  color: var(--color-text);
  font-weight: 600;
}

.exp-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-top: 0.25rem;
  font-size: 0.9rem;
  color: var(--color-text-secondary);
}

.exp-text {
  font-size: 0.82rem;
  color: var(--color-text-secondary);
  white-space: nowrap;
}

.progress-container {
  flex: 1;
  min-width: 120px;
  max-width: 260px;
  background-color: var(--surface-2);
  border-radius: var(--radius-full);
  overflow: hidden;
  height: 8px;
}

.progress-bar {
  height: 100%;
  border-radius: var(--radius-full);
  background: var(--accent);
  transition: width 0.4s ease-in-out;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group > span {
  display: block;
  margin-bottom: 0.45rem;
  font-size: 0.85rem;
  font-weight: 500;
  color: var(--color-text-secondary);
}

.form-group input {
  width: 100%;
  min-height: 2.6rem;
  padding: 0 0.85rem;
  font-size: 0.95rem;
  font-family: var(--font-sans);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--surface-2);
  color: var(--color-text);
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease,
    background-color 0.2s ease;
}

.form-group input:focus {
  outline: none;
  border-color: var(--accent);
  background: var(--surface-1);
  box-shadow: 0 0 0 3px var(--accent-muted);
}

.inline-field {
  display: flex;
  gap: 0.6rem;
  align-items: center;
}

.inline-field input {
  flex: 1;
  min-width: 0;
}

/* 未定义级别的等级样色（保留原有等级配色逻辑） */
.level {
  font-size: 0.85rem;
  padding: 2px 10px;
  border-radius: var(--radius-full);
  white-space: nowrap;
}

.level-undefined {
  text-shadow:
    0.2em 0.2em var(--color-level-undefined-box-shadow),
    -0.2em -0.2em var(--color-level-undefined-box-shadow);
}

.level-0 {
  background-color: #36c7d9;
  color: #083344;
}

.level-1 {
  background-color: #66d934;
  color: #14532d;
}

.level-2 {
  background-color: #d74a4a;
  color: #fff;
}

.level-3 {
  background-color: rgb(240, 133, 39);
  color: #fff;
}

.level-4 {
  background: linear-gradient(45deg, #ff7d7d, #ff5a99, #e376e5, #9a7ef8);
  background-size: 300% 300%;
  animation: change-color 5s ease infinite;
  color: #fff;
}

.level-5 {
  background: linear-gradient(45deg, #ff7d7d, #ff5a99, #e376e5, #9a7ef8);
  background-size: 300% 300%;
  color: transparent;
  background-clip: text;
  animation: change-color 5s ease infinite;
}

.level-6 {
  position: relative;
  background: linear-gradient(45deg, #ff7d7d, #ff5a99, #e376e5, #9a7ef8);
  background-size: 300% 300%;
  color: transparent;
  background-clip: text;
  animation: change-color 5s ease infinite;
}

.level-6::before {
  content: 'Lv6 专家';
  position: absolute;
  transform: rotateX(180deg);
  transform-origin: bottom;
  line-height: 32px;
  background: linear-gradient(0deg, #9a7ef8 0, transparent 80%);
  background-clip: text;
  -webkit-background-clip: text;
  color: transparent;
  opacity: 0.5;
}

.level-7 {
  position: relative;
  line-height: 32px;
  background: linear-gradient(45deg, #ff7d7d, #ff5a99, #e376e5, #9a7ef8);
  color: transparent;
  background-clip: text;
  -webkit-background-clip: text;
  animation: change-color 5s ease infinite;
  background-size: 300% 300%;
}

.level-7::before {
  content: 'Lv7 大神';
  transform: rotateX(180deg);
  position: absolute;
  transform-origin: bottom;
  background: linear-gradient(
    45deg,
    #ff7d7d3e,
    #ff5a993e,
    #e376e53e,
    #9a7ef83e,
    transparent,
    #9a7ef73e,
    #e376e53e,
    #ff5a993e,
    #ff7d7d3e
  );
  background-size: 300% 300%;
  color: transparent;
  background-clip: text;
  animation: change-color 5s ease infinite;
  opacity: 0.5;
}

.level-8 {
  position: relative;
  line-height: 32px;
  text-shadow:
    2px 2px 4px rgba(0, 0, 0, 0.3),
    -2px -2px 4px rgba(255, 255, 255, 0.3);
}

.level-8::before {
  content: '祖师爷';
  transform: rotateX(160deg) skew(10deg);
  position: absolute;
  transform-origin: bottom;
  background: linear-gradient(
    45deg,
    #ff7d7d,
    #ff5a99,
    #e376e5,
    #9a7ef8,
    transparent,
    #9a7ef8,
    #e376e5,
    #ff5a99,
    #ff7d7d
  );
  background-size: 300% 300%;
  color: transparent;
  background-clip: text;
  -webkit-background-clip: text;
  animation: change-color 5s ease infinite;
  opacity: 0.25;
}

@keyframes change-color {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

.level-next {
  background-color: var(--surface-2);
  color: var(--color-text-secondary);
}

/* 按钮 */
.btn-auto {
  width: auto;
  margin-top: 0.25rem;
  padding: 0 1.5rem;
}

.btn-outline {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 40px;
  padding: 0 1.1rem;
  margin: 0;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--surface-1);
  color: var(--color-text);
  font-size: 0.875rem;
  font-weight: 500;
  transition:
    border-color 0.2s ease,
    color 0.2s ease,
    background-color 0.2s ease;
  white-space: nowrap;
}

.btn-outline:hover {
  border-color: var(--accent);
  color: var(--accent);
  background: var(--surface-1);
}

.btn-danger.solid {
  border: 1px solid var(--color-error);
  background: var(--color-error);
  color: #fff;
  min-height: 40px;
  padding: 0 1.25rem;
  border-radius: var(--radius-md);
  font-size: 0.875rem;
  font-weight: 600;
}

.btn-danger.solid:hover {
  background: color-mix(in srgb, var(--color-error) 85%, #000);
  color: #fff;
}

/* 偏好开关 */
.toggle-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
  padding: 0.5rem 0;
  font-size: 0.9rem;
}

.toggle-switch {
  position: relative;
  width: 46px;
  height: 26px;
  flex-shrink: 0;
  background-color: var(--color-border);
  border-radius: var(--radius-full);
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
  overflow: hidden;
  transition: background-color 0.2s ease;
}

.slider {
  position: absolute;
  width: 20px;
  height: 20px;
  background-color: #fff;
  border-radius: 50%;
  top: 3px;
  left: 3px;
  transition: transform 0.2s ease;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.25);
}

.toggle-switch.active {
  background-color: var(--accent);
}

.toggle-switch.active .slider {
  transform: translateX(20px);
}

/* 账号操作 */
.account-actions {
  display: flex;
  gap: 0.6rem;
  flex-wrap: wrap;
}

/* 危险区 */
.danger-zone {
  border-color: color-mix(in srgb, var(--color-error) 30%, var(--color-border));
}

.danger-title {
  color: var(--color-error);
}

.danger-hint {
  margin: 0 0 0.9rem;
  font-size: 0.82rem;
  color: var(--color-text-secondary);
}

.danger-row {
  display: flex;
  gap: 0.6rem;
  flex-wrap: wrap;
  align-items: center;
}

.danger-row input {
  flex: 1;
  min-width: 160px;
  min-height: 2.6rem;
  padding: 0 0.85rem;
  font-size: 0.9rem;
  font-family: var(--font-sans);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--surface-2);
  color: var(--color-text);
}

.danger-row input:focus {
  outline: none;
  border-color: var(--color-error);
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--color-error) 15%, transparent);
}

@media screen and (min-width: 768px) {
  .root {
    width: 100%;
  }
}
</style>
