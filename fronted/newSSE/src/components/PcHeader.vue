<script lang="ts" setup>
import { useTemplateRef } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/userStore'

defineProps<{
  unreadNoticeNum: number
  unreadChatNum: number
}>()

const router = useRouter()
const sinfo = useTemplateRef('sinfo')
const { userInfo } = useUserStore()
function search() {
  const el = sinfo.value as HTMLInputElement
  if (!el || !el.value)
    return
  if (el.value === '年度报告') {
    router.push('/annual2025')
    el.value = ''
    return
  }
  const currentRoute = router.currentRoute.value
  if (currentRoute.path === '/partition/课程交流'
    // 如果先搜索一次然后返回，再次搜索时path会被URI编码
    || currentRoute.path === `/partition/${encodeURIComponent('课程交流')}`
    || currentRoute?.query?.partition === '课程交流'
    || currentRoute?.query?.partition === encodeURIComponent('课程交流')) {
    router.push(`/search?sinfo=${encodeURIComponent(el.value)}&partition=课程交流`)
  }
  else {
    router.push(`/search?sinfo=${encodeURIComponent(el.value)}`)
  }
  el.value = ''
}
</script>

<template>
  <div class="site-header">
    <div
      class="links"
    >
      <router-link to="/post">
        <div
          class="icon"
          style="
								background-image: url(https://img.icons8.com/?size=100&id=89802&format=png&color=000000);
								background-size: 90% 90%;
							"
        />
        发帖
      </router-link>
      <router-link to="/partitions">
        <div
          class="icon"
          style="
								background-image: url(/images/icons8-sidebar-followed.png);
								background-size: 80% 80%;
								background-position: 0px 4px;
							"
        />
        分区
      </router-link>
      <router-link
        to="/course"
      >
        <div
          class="icon"
          style="
								background-image: url(https://img.icons8.com/?size=100&id=85872&format=png&color=000000);
								background-size: 95% 95%;
								background-position: 0px 3px;
							"
        />
        课程专区
      </router-link>
      <router-link to="/feedback">
        <div
          class="icon"
          style="
								background-image: url(https://img.icons8.com/?size=100&id=85500&format=png&color=000000);
								background-position: 0px 3px;
							"
        />
        反馈
      </router-link>
      <router-link to="/shop">
        <div
          class="icon" style="
              background-image: url(https://img.icons8.com/ios-filled/100/shopping-bag.png);
              background-position: 0px 3px;
            "
        />
        商城
      </router-link>
      <router-link to="/heat">
        <div
          class="icon" style="
              background-image: url(https://img.icons8.com/?size=100&id=59862&format=png&color=000000);
              background-position: 0px 3px;
            "
        />
        热榜
      </router-link>
      <router-link to="/rating">
        <div
          class="icon" style="
              background-image: url(https://img.icons8.com/?size=100&id=60985&format=png&color=000000);
              background-position: 0px 3px;
            "
        />
        打分
      </router-link>
    </div>
    <form class="search" @submit.prevent="search">
      <input
        id="sinfo"
        ref="sinfo"
        name="sinfo"
        placeholder="搜索..."
      >
      <button type="submit">
        <div
          class="icon"
          style="
								background-image: url(https://img.icons8.com/?size=100&id=131&format=png&color=000000);
								background-size: 75% 75%;
								background-position: 0px 0px;
							"
        />
      </button>
    </form>
    <div
      class="links account"
    >
      <RouterLink
        to="/save"
      >
        <div
          class="icon"
          style="
								background-image: url(https://img.icons8.com/?size=100&id=85185&format=png&color=000000);
							"
        />
        收藏
      </RouterLink>
      <RouterLink
        to="/history"
      >
        <div
          class="icon"
          style="
								background-image: url(https://img.icons8.com/?size=100&id=83976&format=png&color=000000);
								background-size: 90% 90%;
							"
        />
        历史
      </RouterLink>
      <router-link to="/notice">
        <div
          class="icon"
          style="
								background-image: url(https://img.icons8.com/?size=100&id=32058&format=png&color=000000);
							"
        />
        通知
        <div
          v-if="unreadNoticeNum"
          class="notice-num"
        >
          {{ unreadNoticeNum }}
        </div>
      </router-link>
      <router-link to="/chat">
        私信
        <div
          v-if="unreadChatNum"
          class="notice-num"
        >
          {{ unreadChatNum }}
        </div>
      </router-link>
      <router-link to="/options">
        {{ userInfo.name }}
      </router-link>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.site-header {
  display: flex;
  background: var(--header-bg);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  height: 3em;
  color: var(--color-text);
  align-items: center;
  padding: 0 1em;
  border-bottom: 1px solid var(--header-border);
  box-shadow: var(--shadow-xs);
  transition:
    box-shadow 0.3s ease,
    height 0.3s ease;
  margin-bottom: 0;
  overflow: hidden;

  .links {
    a {
      position: relative;
      display: inline-block;
      padding: 0.35em 0.55em;
      border-radius: var(--radius-sm);
      margin-right: 0.15em;
      text-decoration: none;
      transition:
        background-color 0.2s ease,
        color 0.2s ease;
    }

    a:hover,
    .router-link-active {
      background: var(--accent-muted);
      color: var(--accent) !important;

      .icon {
        filter: none;
        opacity: 1;
      }
    }

    a:link,
    a:visited {
      color: var(--color-text);
    }

    .notice-num {
      position: absolute;
      top: -4px;
      right: -3px;
      border-radius: 9px;
      background: #ff5050;
      color: #eee;
      height: 1rem;
      text-align: center;
      line-height: 1;
      font-size: 13px;
      padding: 2px 4px;
    }
  }

  .search {
    flex: 1;
    margin: 0 1em;
    text-align: center;

    input {
      width: 100%;
      max-width: 48em;
      height: 2.25em;
      padding: 0 2.25em 0 0.85em;
      border: 1px solid var(--color-border);
      border-radius: var(--radius-full);
      line-height: 1.2;
      vertical-align: middle;
      background: var(--surface-2);
      color: var(--color-text);
      transition:
        border-color 0.2s ease,
        box-shadow 0.2s ease;
    }

    input:focus {
      outline: none;
      border-color: var(--accent);
      box-shadow: 0 0 0 3px var(--accent-muted);
    }

    button {
      background: unset;
      height: 1em;
      width: 1em;
      padding: 0;
      margin: 0 0 0 -1.5em;
      vertical-align: middle;
    }
  }
}
.dark-mode .site-header {
  background: var(--header-bg);
  color: var(--color-text);

  .links {
    a:hover,
    .router-link-active {
      background: var(--accent-muted);
      color: var(--color-link) !important;
    }

    a:link,
    a:visited {
      color: var(--color-text);
    }

    .notice-num {
      background: #ff5050;
    }
  }

  .search {
    height: auto;

    input {
      background: var(--surface-2);
      border: 1px solid var(--color-border);
      color: var(--color-text);
    }

    input::placeholder {
      color: var(--shop-text-muted);
    }
    button {
      overflow: hidden;
    }
  }
}

.icon {
  width: 20px;
  height: 20px;
  background-size: cover;
  background-repeat: no-repeat;
  background-size: contain;
  background-position: 0px 3px;
  display: inline-block;
}

body.dark-mode .icon {
  filter: invert(1);
}
</style>
