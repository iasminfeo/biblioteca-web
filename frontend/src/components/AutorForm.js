import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { autorService } from '../services/api';

const AUTOR_VAZIO = { nome: '', nacionalidade: '', dataNascimento: '' };

function AutorForm() {
  const { id } = useParams(); // existe so na rota /autores/editar/:id
  const editando = Boolean(id);
  const navigate = useNavigate();
  const [autor, setAutor] = useState(AUTOR_VAZIO);
  const [erro, setErro] = useState('');

  useEffect(() => {
    if (!editando) return;
    autorService
      .buscar(id)
      .then((resposta) => {
        const { nome, nacionalidade, dataNascimento } = resposta.data;
        setAutor({
          nome: nome || '',
          nacionalidade: nacionalidade || '',
          dataNascimento: dataNascimento || '',
        });
      })
      .catch(() => setErro('Autor não encontrado.'));
  }, [id, editando]);

  const alterar = (e) => {
    setAutor({ ...autor, [e.target.name]: e.target.value });
  };

  const salvar = async (e) => {
    e.preventDefault();
    // Campos opcionais vazios vao como null (a data precisa estar no formato AAAA-MM-DD)
    const dados = {
      nome: autor.nome,
      nacionalidade: autor.nacionalidade || null,
      dataNascimento: autor.dataNascimento || null,
    };
    try {
      if (editando) {
        await autorService.atualizar(id, dados);
      } else {
        await autorService.criar(dados);
      }
      navigate('/autores');
    } catch (e) {
      setErro('Erro ao salvar o autor. Verifique os campos.');
    }
  };

  return (
    <div>
      <h2>{editando ? 'Editar autor' : 'Novo autor'}</h2>
      {erro && <p className="mensagem-erro">{erro}</p>}

      <form className="formulario" onSubmit={salvar}>
        <label htmlFor="nome">Nome *</label>
        <input id="nome" name="nome" value={autor.nome} onChange={alterar} maxLength={100} required />

        <label htmlFor="nacionalidade">Nacionalidade</label>
        <input
          id="nacionalidade"
          name="nacionalidade"
          value={autor.nacionalidade}
          onChange={alterar}
          maxLength={50}
        />

        <label htmlFor="dataNascimento">Data de nascimento</label>
        <input
          id="dataNascimento"
          name="dataNascimento"
          type="date"
          value={autor.dataNascimento}
          onChange={alterar}
        />

        <div className="acoes">
          <button type="submit" className="botao botao-primario">
            Salvar
          </button>
          <button type="button" className="botao" onClick={() => navigate('/autores')}>
            Cancelar
          </button>
        </div>
      </form>
    </div>
  );
}

export default AutorForm;
