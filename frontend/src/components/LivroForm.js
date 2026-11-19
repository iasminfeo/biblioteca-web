import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { autorService, livroService } from '../services/api';

const LIVRO_VAZIO = { titulo: '', isbn: '', anoPublicacao: '', genero: '', autorId: '' };

function LivroForm() {
  const { id } = useParams(); // existe so na rota /livros/editar/:id
  const editando = Boolean(id);
  const navigate = useNavigate();
  const [livro, setLivro] = useState(LIVRO_VAZIO);
  const [autores, setAutores] = useState([]);
  const [erro, setErro] = useState('');

  // Carrega os autores para o <select>
  useEffect(() => {
    autorService
      .listar()
      .then((resposta) => setAutores(resposta.data))
      .catch(() => setErro('Não foi possível carregar os autores.'));
  }, []);

  // Na edicao, carrega o livro e preenche o formulario
  useEffect(() => {
    if (!editando) return;
    livroService
      .buscar(id)
      .then((resposta) => {
        const l = resposta.data;
        setLivro({
          titulo: l.titulo || '',
          isbn: l.isbn || '',
          anoPublicacao: l.anoPublicacao ?? '',
          genero: l.genero || '',
          autorId: l.autor ? String(l.autor.id) : '',
        });
      })
      .catch(() => setErro('Livro não encontrado.'));
  }, [id, editando]);

  const alterar = (e) => {
    setLivro({ ...livro, [e.target.name]: e.target.value });
  };

  const salvar = async (e) => {
    e.preventDefault();
    // O autor vai so com o id: "autor": {"id": 1}
    const dados = {
      titulo: livro.titulo,
      isbn: livro.isbn || null,
      anoPublicacao: livro.anoPublicacao === '' ? null : Number(livro.anoPublicacao),
      genero: livro.genero || null,
      autor: { id: Number(livro.autorId) },
    };
    try {
      if (editando) {
        await livroService.atualizar(id, dados);
      } else {
        await livroService.criar(dados);
      }
      navigate('/livros');
    } catch (e) {
      setErro(e.response?.data?.erro || 'Erro ao salvar o livro. Verifique os campos.');
    }
  };

  return (
    <div>
      <h2>{editando ? 'Editar livro' : 'Novo livro'}</h2>
      {erro && <p className="mensagem-erro">{erro}</p>}

      <form className="formulario" onSubmit={salvar}>
        <label htmlFor="titulo">Título *</label>
        <input id="titulo" name="titulo" value={livro.titulo} onChange={alterar} maxLength={200} required />

        <label htmlFor="autorId">Autor *</label>
        <select id="autorId" name="autorId" value={livro.autorId} onChange={alterar} required>
          <option value="">Selecione um autor</option>
          {autores.map((autor) => (
            <option key={autor.id} value={autor.id}>
              {autor.nome}
            </option>
          ))}
        </select>

        <label htmlFor="isbn">ISBN</label>
        <input id="isbn" name="isbn" value={livro.isbn} onChange={alterar} maxLength={20} />

        <label htmlFor="anoPublicacao">Ano de publicação</label>
        <input
          id="anoPublicacao"
          name="anoPublicacao"
          type="number"
          value={livro.anoPublicacao}
          onChange={alterar}
        />

        <label htmlFor="genero">Gênero</label>
        <input id="genero" name="genero" value={livro.genero} onChange={alterar} maxLength={50} />

        <div className="acoes">
          <button type="submit" className="botao botao-primario">
            Salvar
          </button>
          <button type="button" className="botao" onClick={() => navigate('/livros')}>
            Cancelar
          </button>
        </div>
      </form>
    </div>
  );
}

export default LivroForm;
