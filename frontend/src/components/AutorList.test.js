import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import AutorList from './AutorList';
import { autorService } from '../services/api';

// Mock do service: o teste nao chama o backend de verdade
jest.mock('../services/api', () => ({
  autorService: { listar: jest.fn(), deletar: jest.fn() },
}));

test('mostra os autores retornados pela API', async () => {
  autorService.listar.mockResolvedValue({
    data: [{ id: 1, nome: 'Machado de Assis', nacionalidade: 'Brasileira', dataNascimento: '1839-06-21' }],
  });

  render(
    <MemoryRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
      <AutorList />
    </MemoryRouter>
  );

  expect(await screen.findByText('Machado de Assis')).toBeInTheDocument();
  expect(screen.getByText('21/06/1839')).toBeInTheDocument();
  expect(screen.getByText('Editar')).toBeInTheDocument();
  expect(screen.getByText('Excluir')).toBeInTheDocument();
});
