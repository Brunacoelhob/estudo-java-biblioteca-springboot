const apiURL = 'http://localhost:8080/livros';

const tituloInput = document.getElementById('titulo');
const autorInput = document.getElementById('autor');
const anoInput = document.getElementById('ano');
const editoraInput = document.getElementById('editora');
const imagemUrlInput = document.getElementById('imagemUrl');
const tabelaCorpo = document.querySelector('#tabela-livros tbody');

function carregarLivros() {
    fetch(apiURL)
        .then(res => res.json())
        .then(livros => {
            tabelaCorpo.innerHTML = '';
            livros.forEach(livro => {
                const linha = document.createElement('tr');
                linha.innerHTML = `
                    <td>${livro.id}</td>
                    <td>
                        <img 
                            src="${livro.imagemUrl && livro.imagemUrl.trim() !== '' ? livro.imagemUrl : 'https://via.placeholder.com/50'}" 
                            class="capa-livro" 
                            alt="Capa do livro"
                        >
                    </td>
                    <td>${livro.titulo}</td>
                    <td>${livro.autor}</td>
                    <td>${livro.anoPublicacao ?? '-'}</td>
                    <td>${livro.editora ?? '-'}</td>
                    <td>${livro.disponivel ? 'Sim' : 'Não'}</td>
                    <td class="acoes-cell">
                        <div class="btn-group-linha">
                            <button class="btn btn-editar" onclick="editarInline(this, ${livro.id})">Editar</button>
                            <button class="btn btn-excluir" onclick="deletarLivro(${livro.id})">Excluir</button>
                        </div>
                    </td>
                `;
                tabelaCorpo.appendChild(linha);
            });
        })
        .catch(erro => console.error('Erro ao carregar livros:', erro));
}


function adicionarLivro() {
    const livro = {
        titulo: tituloInput.value,
        autor: autorInput.value,
        anoPublicacao: parseInt(anoInput.value),
        editora: editoraInput.value,
        imagemUrl: imagemUrlInput.value,
        disponivel: true
    };

    fetch(apiURL, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(livro)
    })
        .then(res => {
            if (res.ok) {
                limparFormulario();
                carregarLivros();
            } else {
                alert('Erro ao adicionar livro');
            }
        });
}

function editarInline(botaoEditar, id) {
    const linha = botaoEditar.closest('tr');
    const dados = linha.querySelectorAll('td');

    const imagemSrc = dados[1].querySelector('img').src;
    const titulo = dados[2].textContent;
    const autor = dados[3].textContent;
    const ano = dados[4].textContent;
    const editora = dados[5].textContent;
    const disponivel = dados[6].textContent.trim() === 'Sim';

    linha.innerHTML = `
        <td>${id}</td>
        <td><input type="text" value="${imagemSrc}" class="input"></td>
        <td><input type="text" value="${titulo}" class="input"></td>
        <td><input type="text" value="${autor}" class="input"></td>
        <td><input type="number" value="${ano}" class="input"></td>
        <td><input type="text" value="${editora}" class="input"></td>
        <td>
            <select class="input">
                <option value="true" ${disponivel ? 'selected' : ''}>Sim</option>
                <option value="false" ${!disponivel ? 'selected' : ''}>Não</option>
            </select>
        </td>
        <td class="acoes-cell">
            <div class="btn-group-linha">
                <button class="btn btn-salvar" onclick="salvarEdicao(${id}, this)">Salvar</button>
                <button class="btn btn-excluir" onclick="carregarLivros()">Cancelar</button>
            </div>
        </td>
    `;
}

function salvarEdicao(id, botaoSalvar) {
    const linha = botaoSalvar.closest('tr');
    const inputs = linha.querySelectorAll('input');
    const select = linha.querySelector('select');

    const livroAtualizado = {
        imagemUrl: inputs[0].value,
        titulo: inputs[1].value,
        autor: inputs[2].value,
        anoPublicacao: parseInt(inputs[3].value),
        editora: inputs[4].value,
        disponivel: select.value === 'true'
    };

    const apiUrl = `http://localhost:8080/livros/${id}`;

    fetch(apiUrl, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(livroAtualizado)
    })
    .then(res => {
        if (res.ok) {
            carregarLivros();
        } else {
            alert('Erro ao salvar alterações');
        }
    })
    .catch(erro => {
        console.error('Erro na requisição:', erro);
        alert('Erro na comunicação com o servidor');
    });
}

function deletarLivro(id) {
    if (confirm('Tem certeza que deseja excluir este livro?')) {
        fetch(`${apiURL}/${id}`, {
            method: 'DELETE'
        })
            .then(res => {
                if (res.ok) {
                    carregarLivros();
                } else {
                    alert('Erro ao excluir livro');
                }
            });
    }
}

function limparFormulario() {
    tituloInput.value = '';
    autorInput.value = '';
    anoInput.value = '';
    editoraInput.value = '';
    imagemUrlInput.value = '';
}

document.addEventListener('DOMContentLoaded', carregarLivros);
