import React, { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { autorService, livroService } from '../services/api';

function LivroList() {
  const [livros, setLivros] = useState([]);
  const [autores, setAutores] = useState([]);
  const [autorFiltro, setAutorFiltro] = useState(''); // '' = todos os autores
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState('');
  const navigate = useNavigate();

  // Carrega os livros (todos ou so os do autor escolhido no filtro)
  const carregar = useCallback(async () => {
    try {
      setCarregando(true);
      const resposta = autorFiltro
        ? await livroService.listarPorAutor(autorFiltro)
        : await livroService.listar();
      setLivros(resposta.data);
      setErro('');
    } catch (e) {
      setErro('Não foi possível carregar os livros. O backend está rodando?');
    } finally {
      setCarregando(false);
    }
  }, [autorFiltro]);

  useEffect(() => {
    carregar();
  }, [carregar]);

  // Autores para o filtro
  useEffect(() => {
    autorService
      .listar()
      .then((resposta) => setAutores(resposta.data))
      .catch(() => setAutores([]));
  }, []);

  const excluir = async (livro) => {
    if (!window.confirm(`Excluir o livro "${livro.titulo}"?`)) return;
    try {
      await livroService.deletar(livro.id);
      carregar();
    } catch (e) {
      alert('Erro ao excluir o livro.');
    }
  };

  return (
    <div>
      <div className="cabecalho">
        <h2>Livros</h2>
        <Link to="/livros/novo" className="botao botao-primario">
          + Novo livro
        </Link>
      </div>

      <div className="filtro">
        <label htmlFor="filtroAutor">Filtrar por autor:</label>
        <select id="filtroAutor" value={autorFiltro} onChange={(e) => setAutorFiltro(e.target.value)}>
          <option value="">Todos</option>
          {autores.map((autor) => (
            <option key={autor.id} value={autor.id}>
              {autor.nome}
            </option>
          ))}
        </select>
      </div>

      {erro && <p className="mensagem-erro">{erro}</p>}

      {carregando ? (
        <p>Carregando...</p>
      ) : livros.length === 0 ? (
        <p>Nenhum livro cadastrado.</p>
      ) : (
        <table className="tabela">
          <thead>
            <tr>
              <th>Título</th>
              <th>Autor</th>
              <th>ISBN</th>
              <th>Ano</th>
              <th>Gênero</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            {livros.map((livro) => (
              <tr key={livro.id}>
                <td>{livro.titulo}</td>
                <td>{livro.autor ? livro.autor.nome : '-'}</td>
                <td>{livro.isbn || '-'}</td>
                <td>{livro.anoPublicacao || '-'}</td>
                <td>{livro.genero || '-'}</td>
                <td className="acoes">
                  <button className="botao" onClick={() => navigate(`/livros/editar/${livro.id}`)}>
                    Editar
                  </button>
                  <button className="botao botao-perigo" onClick={() => excluir(livro)}>
                    Excluir
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}

export default LivroList;
