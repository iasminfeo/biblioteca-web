import React, { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { autorService } from '../services/api';

// Converte "AAAA-MM-DD" para "DD/MM/AAAA" sem passar por Date (evita erro de fuso horario)
function formatarData(data) {
  if (!data) return '-';
  const [ano, mes, dia] = data.split('-');
  return `${dia}/${mes}/${ano}`;
}

function AutorList() {
  const [autores, setAutores] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState('');
  const navigate = useNavigate();

  const carregar = useCallback(async () => {
    try {
      setCarregando(true);
      const resposta = await autorService.listar();
      setAutores(resposta.data);
      setErro('');
    } catch (e) {
      setErro('Não foi possível carregar os autores. O backend está rodando?');
    } finally {
      setCarregando(false);
    }
  }, []);

  useEffect(() => {
    carregar();
  }, [carregar]);

  const excluir = async (autor) => {
    if (!window.confirm(`Excluir o autor "${autor.nome}"?`)) return;
    try {
      await autorService.deletar(autor.id);
      carregar();
    } catch (e) {
      const mensagem = e.response?.data?.erro || 'Erro ao excluir o autor.';
      alert(mensagem);
    }
  };

  return (
    <div>
      <div className="cabecalho">
        <h2>Autores</h2>
        <Link to="/autores/novo" className="botao botao-primario">
          + Novo autor
        </Link>
      </div>

      {erro && <p className="mensagem-erro">{erro}</p>}

      {carregando ? (
        <p>Carregando...</p>
      ) : autores.length === 0 ? (
        <p>Nenhum autor cadastrado.</p>
      ) : (
        <table className="tabela">
          <thead>
            <tr>
              <th>Nome</th>
              <th>Nacionalidade</th>
              <th>Data de nascimento</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            {autores.map((autor) => (
              <tr key={autor.id}>
                <td>{autor.nome}</td>
                <td>{autor.nacionalidade || '-'}</td>
                <td>{formatarData(autor.dataNascimento)}</td>
                <td className="acoes">
                  <button className="botao" onClick={() => navigate(`/autores/editar/${autor.id}`)}>
                    Editar
                  </button>
                  <button className="botao botao-perigo" onClick={() => excluir(autor)}>
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

export default AutorList;
