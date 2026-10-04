// URL da API: pode ser trocada definindo window.API_URL antes deste script (ex.: em config.js).
const apiURL = (window.API_URL || 'http://localhost:8080') + '/livros';

const tituloInput = document.getElementById('titulo');
const autorInput = document.getElementById('autor');
const anoInput = document.getElementById('ano');
const editoraInput = document.getElementById('editora');
const imagemUrlInput = document.getElementById('imagemUrl');
const tabelaCorpo = document.querySelector('#tabela-livros tbody');

// Capa padrão embutida (sem depender de serviço externo).
const CAPA_PADRAO =
    'data:image/svg+xml;utf8,' +
    encodeURIComponent(
        '<svg xmlns="http://www.w3.org/2000/svg" width="50" height="70"><rect width="50" height="70" fill="#d9d9d9"/>' +
        '<text x="25" y="40" font-size="10" text-anchor="middle" fill="#666">sem capa</text></svg>'
    );

// SEGURANÇA: todo texto vindo da API é inserido com textContent/atributos do DOM,
// nunca com innerHTML. Assim um título como "<img onerror=...>" aparece como texto e não executa.
function el(tag, props = {}, ...filhos) {
    const e = document.createElement(tag);
    Object.entries(props).forEach(([k, v]) => {
        if (k === 'class') e.className = v;
        else if (k.startsWith('on')) e.addEventListener(k.slice(2), v);
        else e[k] = v;
    });
    filhos.forEach(f => e.append(f));
    return e;
}

// Só http(s) vira src de imagem; qualquer outra coisa usa a capa padrão.
function urlSegura(url) {
    return typeof url === 'string' && /^https?:\/\//i.test(url) ? url : CAPA_PADRAO;
}

async function requisitar(url, opcoes) {
    const res = await fetch(url, opcoes);
    if (!res.ok) {
        let detalhe = 'Erro inesperado';
        try {
            const problema = await res.json();
            const campos = problema.campos ? Object.values(problema.campos).join('; ') : '';
            detalhe = campos || problema.detail || detalhe;
        } catch (_) { /* resposta sem corpo JSON */ }
        throw new Error(detalhe);
    }
    return res.status === 204 ? null : res.json();
}

function linhaDoLivro(livro) {
    const capa = el('img', { src: urlSegura(livro.imagemUrl), className: 'capa-livro', alt: 'Capa do livro' });
    const acoes = el('div', { class: 'btn-group-linha' },
        el('button', { class: 'btn btn-editar', textContent: 'Editar', onclick: () => editarInline(livro) }),
        el('button', { class: 'btn btn-excluir', textContent: 'Excluir', onclick: () => deletarLivro(livro.id) })
    );
    return el('tr', {},
        el('td', { textContent: livro.id }),
        el('td', {}, capa),
        el('td', { textContent: livro.titulo }),
        el('td', { textContent: livro.autor }),
        el('td', { textContent: livro.anoPublicacao ?? '-' }),
        el('td', { textContent: livro.editora ?? '-' }),
        el('td', { textContent: livro.disponivel ? 'Sim' : 'Não' }),
        el('td', { class: 'acoes-cell' }, acoes)
    );
}

async function carregarLivros() {
    try {
        const livros = await requisitar(apiURL);
        tabelaCorpo.replaceChildren(...livros.map(linhaDoLivro));
    } catch (erro) {
        console.error('Erro ao carregar livros:', erro);
        tabelaCorpo.replaceChildren(
            el('tr', {}, el('td', { colSpan: 8, textContent: 'Não foi possível carregar os livros. A API está no ar?' }))
        );
    }
}

async function adicionarLivro() {
    const livro = {
        titulo: tituloInput.value,
        autor: autorInput.value,
        anoPublicacao: anoInput.value ? parseInt(anoInput.value, 10) : null,
        editora: editoraInput.value,
        imagemUrl: imagemUrlInput.value,
        disponivel: true
    };
    try {
        await requisitar(apiURL, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(livro)
        });
        limparFormulario();
        carregarLivros();
    } catch (erro) {
        alert('Erro ao adicionar livro: ' + erro.message);
    }
}

function editarInline(livro) {
    const campo = (tipo, valor) => el('input', { type: tipo, value: valor ?? '', className: 'input' });
    const imagem = campo('text', livro.imagemUrl);
    const titulo = campo('text', livro.titulo);
    const autor = campo('text', livro.autor);
    const ano = campo('number', livro.anoPublicacao);
    const editora = campo('text', livro.editora);
    const disponivel = el('select', { className: 'input' },
        el('option', { value: 'true', textContent: 'Sim', selected: livro.disponivel }),
        el('option', { value: 'false', textContent: 'Não', selected: !livro.disponivel })
    );

    const salvar = async () => {
        try {
            await requisitar(`${apiURL}/${livro.id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    imagemUrl: imagem.value,
                    titulo: titulo.value,
                    autor: autor.value,
                    anoPublicacao: ano.value ? parseInt(ano.value, 10) : null,
                    editora: editora.value,
                    disponivel: disponivel.value === 'true'
                })
            });
            carregarLivros();
        } catch (erro) {
            alert('Erro ao salvar alterações: ' + erro.message);
        }
    };

    const linha = el('tr', {},
        el('td', { textContent: livro.id }),
        el('td', {}, imagem), el('td', {}, titulo), el('td', {}, autor),
        el('td', {}, ano), el('td', {}, editora), el('td', {}, disponivel),
        el('td', { class: 'acoes-cell' }, el('div', { class: 'btn-group-linha' },
            el('button', { class: 'btn btn-salvar', textContent: 'Salvar', onclick: salvar }),
            el('button', { class: 'btn btn-excluir', textContent: 'Cancelar', onclick: carregarLivros })
        ))
    );
    // Troca a linha atual pela linha de edição.
    [...tabelaCorpo.rows].find(r => r.cells[0].textContent === String(livro.id))?.replaceWith(linha);
}

async function deletarLivro(id) {
    if (!confirm('Tem certeza que deseja excluir este livro?')) return;
    try {
        await requisitar(`${apiURL}/${id}`, { method: 'DELETE' });
        carregarLivros();
    } catch (erro) {
        alert('Erro ao excluir livro: ' + erro.message);
    }
}

function limparFormulario() {
    [tituloInput, autorInput, anoInput, editoraInput, imagemUrlInput].forEach(i => (i.value = ''));
}

document.addEventListener('DOMContentLoaded', carregarLivros);
