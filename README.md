# 🌌 O Roubo Quântico — Jogo de Ficção Interativa em Java

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Architecture](https://img.shields.io/badge/Architecture-MVC-blue?style=for-the-badge)
![Status](https://img.shields.io/badge/Status-Concluído-brightgreen?style=for-the-badge)

Projeto desenvolvido como solução do Problema de Aprendizagem Baseada em Problemas (**PBL 1**) da disciplina **EXA863 - MI - Programação** do curso de Engenharia de Computação da **Universidade Estadual de Feira de Santana (UEFS)**.

---

## 📋 Sobre o Jogo

**O Roubo Quântico** é um jogo textual de ficção interativa e mistério sci-fi que se passa em um ambiente acadêmico-científico. O jogador assume o papel de um estudante investigador que precisa ajudar sua colega, Elisa, a recuperar o código de uma patente de computação quântica roubada na véspera da apresentação do TCC.

Ao longo de **23 cenas intercaladas com 4 finais distintos**, cada escolha afeta diretamente os atributos do protagonista, seus relacionamentos com NPCs e sua reputação acadêmica, determinando o desfecho da investigação.

---

## 🏛️ Arquitetura e Padrões de Projeto

O projeto foi construído em **Java puro (sem frameworks ou bibliotecas externas de terceiros)**, respeitando estritamente o padrão de arquitetura **MVC (Model-View-Controller)** e organizado em pacotes modulares:

```text
                            ┌────────────────────────┐
                            │ controllers.MainController │
                            └───────────┬────────────┘
                                        │ (Inicializa)
            ┌───────────────────────────┴───────────────────────────┐
            ▼                                                       ▼
 ┌────────────────────┐               ┌───────────────────────────────────────────┐
 │ views.ConsoleView  │ ◄───────────► │ controllers.JogoController                │
 │ (Camada View)      │   Interage    │ controllers.MenuController (Controllers)  │
 └────────────────────┘               └─────────────────────┬─────────────────────┘
                                                            │
                                                            │ Manipula
                                                            ▼
                                           ┌──────────────────────────────────────┐
                                           │ models.*                             │
                                           │ (Protagonista, CenaJson, Opcao, etc) │
                                           └──────────────────────────────────────┘
```

### Principais Decisões de Engenharia:
* **Organização em Pacotes:** Separação clara entre as camadas `models`, `views` e `controllers`.
* **Parser de JSON Nativo:** Processamento e decodificação do arquivo `roteiro.json` utilizando Expressões Regulares (`java.util.regex`) sem dependência de bibliotecas externas (como Gson ou Jackson).
* **Encapsulamento com Trava Numérica:** Garantia matemática de que nenhum atributo do personagem (Lógica, Carisma, Estresse, Coragem, Pistas e Reputação) assuma valores negativos, utilizando a função `Math.max(0, valor)`.
* **Polimorfismo e Extensibilidade:** Implementação de classe abstrata `CenaBase` estendida por `CenaJson`, permitindo fácil inclusão de novas fontes de dados ou tipos de cena.
* **Mapeamento de Relacionamentos:** Uso de `HashMap<String, Integer>` para gestão escalável de afinidades com NPCs.
* **Filtro Dinâmico de Escolhas:** Validação de condições de acesso a opções antes da renderização no console.

---

## 📦 Estrutura de Pastas do Repositório

```text
.
├── src/
│   ├── controllers/
│   │   ├── MainController.java     # Ponto de entrada do programa
│   │   ├── MenuController.java     # Gerenciamento de menus e navegação inicial
│   │   └── JogoController.java     # Controle do loop narrativo e estado do jogo
│   ├── models/
│   │   ├── Personagem.java         # Classe abstrata base para entidades
│   │   ├── Protagonista.java       # Representação do jogador e seus atributos
│   │   ├── NPC.java                # Representação de personagens secundários
│   │   ├── CenaBase.java           # Estrutura base abstrata de cena
│   │   ├── CenaJson.java           # Parser e construtor de cenas via JSON (Regex)
│   │   ├── Opcao.java              # Lógica de requisitos e consequências de escolhas
│   │   └── ProgressoJogo.java      # Container de estado da sessão
│   └── views/
│       └── ConsoleView.java        # Entrada/saída de dados e limpeza de terminal
├── roteiro.json                    # Banco de dados da narrativa
└── README.md                       # Documentação do repositório
```

---

## 🛠️ Como Compilar e Executar

### Pré-requisitos
* **Java Development Kit (JDK) 17** ou superior instalado e configurado nas variáveis de ambiente.

### Passo a Passo pelo Terminal

1. **Clonar o repositório:**
   ```bash
   git clone [https://github.com/seu-usuario/o-roubo-quantico.git](https://github.com/seu-usuario/o-roubo-quantico.git)
   cd o-roubo-quantico
   ```

2. **Compilar todos os pacotes a partir da pasta raiz:**
   ```bash
   javac -d bin src/models/*.java src/views/*.java src/controllers/*.java
   ```

3. **Executar a aplicação especificando a classe principal do pacote:**
   
   * Linux / macOS:
     ```bash
     java -cp bin:. controllers.MainController
     ```
   * Windows:
     ```cmd
     java -cp "bin;." controllers.MainController
     ```

> **Nota:** Certifique-se de que o arquivo `roteiro.json` esteja na pasta raiz onde o comando de execução é chamado.

---

## 👨‍💻 Autores

* **Renan Queiroz dos Santos** - *Engenharia de Computação - UEFS*
* **Felipe Pereira** - *Engenharia de Computação - UEFS*