import React from 'react';
import { BrowserRouter, NavLink, Navigate, Route, Routes } from 'react-router-dom';
import AutorList from './components/AutorList';
import AutorForm from './components/AutorForm';
import LivroList from './components/LivroList';
import LivroForm from './components/LivroForm';
import './App.css';

function App() {
  return (
    <BrowserRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
      <div className="app">
        <header className="menu">
          <h1>Biblioteca Web</h1>
          <nav>
            <NavLink to="/autores">Autores</NavLink>
            <NavLink to="/livros">Livros</NavLink>
          </nav>
        </header>

        <main className="conteudo">
          <Routes>
            <Route path="/" element={<Navigate to="/autores" replace />} />
            <Route path="/autores" element={<AutorList />} />
            <Route path="/autores/novo" element={<AutorForm />} />
            <Route path="/autores/editar/:id" element={<AutorForm />} />
            <Route path="/livros" element={<LivroList />} />
            <Route path="/livros/novo" element={<LivroForm />} />
            <Route path="/livros/editar/:id" element={<LivroForm />} />
            <Route path="*" element={<p>Página não encontrada.</p>} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  );
}

export default App;
