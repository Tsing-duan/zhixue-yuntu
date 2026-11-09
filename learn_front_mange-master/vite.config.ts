import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'
export default defineConfig(({ mode }) => {
  const env = { ...loadEnv(mode, process.cwd(), ''), ...process.env }
  const api = env.VITE_API_BASE_URL || env.VUE_APP_BASE_URL || 'http://localhost:8080'
  const proxy = Object.fromEntries(['/img', '/video', '/file'].map(path => [path, { target: api, changeOrigin: true }]))
  return {
    plugins: [vue()],
    envPrefix: [], // Only the explicit public define entries below reach the browser.
    resolve: { alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) }, extensions: ['.mjs','.js','.ts','.tsx','.json','.vue'] },
    define: { 'import.meta.env.VITE_DEMO_CONTENT': JSON.stringify(env.VITE_DEMO_CONTENT === 'true'), 'import.meta.env.VITE_API_BASE_URL': JSON.stringify(api), 'import.meta.env.VITE_ADMIN_PORT': JSON.stringify(env.VITE_ADMIN_PORT || env.VUE_APP_ADMIN_PORT || '3000') },
    server: { host: '127.0.0.1', port: Number(env.FRONTEND_PORT || 3000), strictPort: true, proxy },
    preview: { host: '127.0.0.1', port: Number(env.FRONTEND_PORT || 3000), strictPort: true, proxy },
    build: { outDir: 'dist', assetsDir: 'assets', target: 'es2022' },
  }
})
