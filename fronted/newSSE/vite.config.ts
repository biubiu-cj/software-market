import { fileURLToPath, URL } from 'node:url'
import vue from '@vitejs/plugin-vue'
import vueJsxPlugin from '@vitejs/plugin-vue-jsx'
import UnoCSS from 'unocss/vite'
import { defineConfig } from 'vite'
import dnsPrefetchPlugin from './plugins/vite-plugin-dns-prefetch'
// https://vitejs.dev/config/
const base = '/'
export default defineConfig({
  base,
  plugins: [
    vue(),
    vueJsxPlugin(),
    UnoCSS(),
    // analyzer({
    //   analyzerMode: 'server', // 默认值，启动本地服务器
    //   openAnalyzer: true, // 自动打开浏览器
    //   analyzerPort: 8888, // 分析服务器端口
    // }),
    dnsPrefetchPlugin({
      maxLinks: 5,
      exclude: ['localhost', '127.0.0.1', 'vuejs.org'],
    }),
  ],
  server: {
    // 本地 pnpm dev 时把接口转发到后端，使 .env 里的相对路径 /api 在开发环境也能用
    proxy: {
      '/api': { target: 'http://localhost:8080', changeOrigin: true },
      '/avatar': { target: 'http://localhost:8080', changeOrigin: true },
      '/websocket': { target: 'ws://localhost:8080', ws: true },
    },
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
})
