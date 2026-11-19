import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import LivroForm from './LivroForm';
import { autorService } from '../services/api';

// Mock do service: o teste nao chama o backend de verdade
jest.mock('../services/api', () => ({
  autorService: { listar: jest.fn() },
  livroService: { buscar: jest.fn(), criar: jest.fn(), atualizar: jest.fn() },
}));

test('mostra o select com os autores cadastrados', async () => {
  autorService.listar.mockResolvedValue({
    data: [
      { id: 1, nome: 'Machado de Assis' },
      { id: 2, nome: 'Clarice Lispector' },
    ],
  });

  render(
    <MemoryRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
      <LivroForm />
    </MemoryRouter>
  );

  expect(screen.getByText('Novo livro')).toBeInTheDocument();
  expect(await screen.findByRole('option', { name: 'Machado de Assis' })).toBeInTheDocument();
  expect(screen.getByRole('option', { name: 'Clarice Lispector' })).toBeInTheDocument();
  expect(screen.getByLabelText('Autor *')).toBeInTheDocument();
});
