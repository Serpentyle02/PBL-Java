# 🌌 O Roubo Quântico — Jogo de Ficção Interativa em Java

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Architecture](https://img.shields.io/badge/Architecture-MVC-blue?style=for-the-badge)
![Status](https://img.shields.io/badge/Status-Concluído-brightgreen?style=for-the-badge)

Projeto desenvolvido como solução do Problema de Aprendizagem Baseada em Problemas (**PBL 1**) da disciplina **EXA863 - MI - Programação** do curso de Engenharia de Computação da **Universidade Estadual de Feira de Santana (UEFS)**.

---

## 📋 Sobre o Jogo

**O Roubo Quântico** é um jogo textual de ficção interativa e mistério sci-fi que se passa em um ambiente acadêmico-científico. O jogador assume o papel de um estudante investigador que precisa ajudar sua colega, Elisa, a recuperar o código de uma patente de computação quântica roubada na véspera da apresentação do TCC.

O roteiro possui **24 cenas e 4 finais distintos**. Cada escolha afeta os atributos do protagonista (**Lógica, Carisma, Estresse, Coragem, Pistas e Reputação**) e seus relacionamentos com NPCs, e alguns caminhos só ficam disponíveis se o jogador cumprir requisitos de atributos.

### Recursos do jogo
* **Atributos e relacionamentos:** cada opção pode alterar atributos e a afinidade com NPCs (ex.: Beto, Luiza, Prof. Silva). O status do protagonista é exibido no topo de cada cena.
* **Escolhas condicionadas:** opções com requisitos (`Pistas`, `CoragemAlta`, `EstresseBaixo`, `ReputacaoAlta`, `ReputacaoBaixa`, `FlagranteFalha`) aparecem marcadas como *(bloqueada)* quando o jogador não os cumpre.
* **3 slots de salvamento:** salvar, carregar, sobrescrever (com confirmação) e excluir partidas. A partida registra nome, data/hora, atributos, reputação e relacionamentos.
* **Autosave:** ao chegar na cena do marco narrativo, salva automaticamente no slot da partida atual ou no primeiro slot vazio. Nunca sobrescreve o save de outro slot.
* **Galeria e finais:** conteúdos desbloqueados e finais alcançados são registrados e persistem entre partidas.
* **Preferências:** velocidade do texto (lenta ou instantânea), autosave e dicas de jogo ligados/desligados.
* **Interface limpa:** o terminal é apagado a cada troca de cena ou menu, exibindo somente a tela atual, sem histórico. Detalhes internos como o número da cena não são mostrados ao jogador.

---

## 🏛️ Arquitetura e Padrões de Projeto

O projeto foi construído em **Java puro (sem frameworks ou bibliotecas externas de terceiros)**, respeitando o padrão **MVC (Model-View-Controller)** e organizado em pacotes modulares:

```text
                    ┌────────────────────────────┐
                    │ controllers.MainController │
                    └─────────────┬──────────────┘
                                  │ (Inicializa)
        ┌─────────────────────────┴───────────────────────────┐
        ▼                                                     ▼
┌────────────────────┐          ┌──────────────────────────────────────────────┐
│ views.ConsoleView  │ ◄──────► │ controllers.JogoController                   │
│ (Camada View)      │ Interage │ controllers.MenuController                   │
└────────────────────┘          │ controllers.SalvamentoController             │
                                │ (Camada Controller)                          │
                                └──────────────────────┬───────────────────────┘
                                                       │ Manipula
                                                       ▼
                                  ┌──────────────────────────────────────────┐
                                  │ models.*                                 │
                                  │ (Protagonista, CenaJson, Opcao, etc)     │
                                  └──────────────────────────────────────────┘
```

A camada `models` não depende de `views` nem de `controllers`, e a `views` apenas imprime e lê entradas: toda regra de jogo (requisitos, consequências, salvamento) fica em models e controllers.

### Principais Decisões de Engenharia
* **Organização em Pacotes:** separação clara entre as camadas `models`, `views` e `controllers`.
* **Parser de JSON Nativo:** leitura do `Roteiro.json` e dos arquivos de save com Expressões Regulares (`java.util.regex`) e varredura de chaves respeitando strings e escapes, sem Gson ou Jackson. Os arquivos são lidos e gravados em **UTF-8**.
* **Encapsulamento com Trava Numérica:** nenhum atributo do personagem assume valores negativos (`Math.max(0, valor)`).
* **Polimorfismo e Extensibilidade:** classe abstrata `CenaBase` estendida por `CenaJson`, permitindo incluir novas fontes de dados ou tipos de cena.
* **Mapeamento de Relacionamentos:** `HashMap<String, Integer>` para gestão de afinidades com NPCs.
* **Tratamento de erros de persistência:** falhas de leitura/gravação e slots inválidos lançam `SalvamentoException`, tratada pelos controllers.
* **Entrada encerrada:** se a entrada padrão terminar (ex.: Ctrl+Z / Ctrl+D), o jogo encerra de forma limpa.

---

## 📦 Estrutura de Pastas do Repositório

```text
.
├── src/
│   ├── controllers/
│   │   ├── MainController.java         # Ponto de entrada do programa
│   │   ├── MenuController.java         # Menu principal e navegação inicial
│   │   ├── JogoController.java         # Loop narrativo, escolhas, saves, preferências e galeria
│   │   └── SalvamentoController.java   # Leitura/gravação de slots e dados globais
│   ├── models/
│   │   ├── Personagem.java             # Classe abstrata base para entidades
│   │   ├── Protagonista.java           # Jogador: atributos, reputação e relacionamentos
│   │   ├── NPC.java                    # Personagens secundários
│   │   ├── CenaBase.java               # Estrutura base abstrata de cena
│   │   ├── CenaJson.java               # Parser e construtor de cenas via JSON (Regex)
│   │   ├── Opcao.java                  # Requisitos e consequências de cada escolha
│   │   ├── ProgressoJogo.java          # Estado de um slot e dados globais (preferências, galeria, finais)
│   │   └── SalvamentoException.java    # Exceção de persistência
│   ├── views/
│   │   └── ConsoleView.java            # Entrada/saída e limpeza do terminal
│   └── jsonFiles/
│       ├── Roteiro.json                # Banco de dados da narrativa
│       ├── save_slot_1..3.json         # Saves (gerados pelo jogo)
│       └── dados_globais.json          # Preferências, galeria e finais (gerado pelo jogo)
└── README.md                           # Documentação do repositório
```

---

## 🎮 Como Jogar

* **Menu principal:** Nova Partida, Carregar/Excluir Saves, Galeria de Conquistas e Finais, Preferências, Instruções e Créditos, Sair.
* **Durante a história:** digite o número da escolha desejada e pressione ENTER.
  * `7` — Preferências
  * `8` — Salvar (escolha um dos 3 slots)
  * `9` — Voltar ao Menu Principal

---

## 🛠️ Como Compilar e Executar

### Pré-requisitos
* **Java Development Kit (JDK) 17** ou superior instalado e configurado nas variáveis de ambiente.

### Passo a Passo pelo Terminal

1. **Clonar o repositório:**
   ```bash
   git clone https://github.com/Serpentyle02/PBL-Java.git
   cd PBL-Java
   ```

2. **Compilar todos os pacotes a partir da pasta raiz:**
   ```bash
   javac -encoding UTF-8 -d bin src/models/*.java src/views/*.java src/controllers/*.java
   ```

3. **Executar a aplicação a partir da pasta raiz:**

   * Linux / macOS:
     ```bash
     java -cp bin controllers.MainController
     ```
   * Windows:
     ```cmd
     chcp 65001
     java -cp bin controllers.MainController
     ```

> **Nota:** execute sempre a partir da **pasta raiz** do projeto. O jogo procura o roteiro em `src/jsonFiles/Roteiro.json` e grava os saves em `src/jsonFiles/`, usando caminhos relativos ao diretório de execução. No Windows, `chcp 65001` evita acentos quebrados no terminal.

---

## ⚠️ Limitações Conhecidas

* O **Final 4 (Expulsão Precoce / Game Over)**, na Cena 23, hoje é inalcançável: sua condição exige estresse alto e coragem baixa na Cena 15, mas os valores do roteiro nunca chegam a essa combinação. É um ajuste de balanceamento do `Roteiro.json`.

---

## 👨‍💻 Autores

* **Renan Queiroz dos Santos** - *Engenharia de Computação - UEFS*
* **Felipe Pereira** - *Engenharia de Computação - UEFS*