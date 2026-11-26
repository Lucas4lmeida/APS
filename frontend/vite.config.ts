import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// O front chama a API em /api/...; em desenvolvimento o Vite repassa para o Spring Boot
// (porta 8080), sem precisar de CORS. Depois, no Docker, o mesmo papel fica com o servidor web.
const proxyApi = {
  '/api': {
    target: 'http://127.0.0.1:8080',
    changeOrigin: true,
    rewrite: (caminho: string) => caminho.replace(/^\/api/, ''),
  },
}

// Endereço IPv4 explícito: com "localhost", o Node no Windows escuta só em [::1] e navegadores que
// resolvem localhost para 127.0.0.1 recebem "conexão recusada". http://localhost:5173 continua
// funcionando, e o servidor segue acessível só neste computador.
const host = '127.0.0.1'

export default defineConfig({
  plugins: [react()],
  server: { host, port: 5173, strictPort: true, proxy: proxyApi },
  preview: { host, port: 4173, strictPort: true, proxy: proxyApi },
})
