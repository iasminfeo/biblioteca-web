import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import LivroList from './LivroList';
import { autorService, livroService } from '../services/api';

// Mock do service: o teste nao chama o backend de verdade
jest.mock('../services/api', () => ({
  autorService: { listar: jest.fn() },
  livroService: { listar: jest.fn(), listarPorAutor: jest.fn(), deletar: jest.fn() },
}));

test('mostra os livros com o nome do autor', async () => {
  autorService.listar.mockResolvedValue({ data: [{ id: 1, nome: 'Machado de Assis' }] });
  livroService.listar.mockResolvedValue({
    data: [
      {
        id: 10,
        titulo: 'Dom Casmurro',
        isbn: '978-8535910667',
        anoPublicacao: 1899,
        genero: 'Romance',
        autor: { id: 1, nome: 'Machado de Assis' },
      },
    ],
  });

  render(
    <MemoryRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
      <LivroList />
    </MemoryRouter>
  );

  expect(await screen.findByText('Dom Casmurro')).toBeInTheDocument();
  // o nome do autor aparece na tabela (e tambem no filtro)
  expect(screen.getByRole('cell', { name: 'Machado de Assis' })).toBeInTheDocument();
  expect(screen.getByText('Romance')).toBeInTheDocument();
});
