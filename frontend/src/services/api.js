import axios from 'axios';

// URL base da API REST (backend Spring Boot).
// Pode ser trocada na hora do build com a variavel REACT_APP_API_URL.
const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_URL,
  headers: { 'Content-Type': 'application/json' },
});

export const autorService = {
  listar: () => api.get('/autores'),
  buscar: (id) => api.get(`/autores/${id}`),
  criar: (autor) => api.post('/autores', autor),
  atualizar: (id, autor) => api.put(`/autores/${id}`, autor),
  deletar: (id) => api.delete(`/autores/${id}`),
};

export default api;
