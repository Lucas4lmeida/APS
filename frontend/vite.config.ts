import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// O front chama a API em /api/...; em desenvolvimento o Vite repassa para o Spring Boot
// (porta 8080), sem precisar de CORS. Depois, no Docker, o mesmo papel fica com o servidor web.
const proxyApi = {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true,
    rewrite: (caminho: string) => caminho.replace(/^\/api/, ''),
  },
}

export default defineConfig({
  plugins: [react()],
  server: { port: 5173, proxy: proxyApi },
  preview: { port: 4173, proxy: proxyApi },
})
