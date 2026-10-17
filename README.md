# Task Manager API

API REST para gerenciamento de tarefas pessoais, com autenticação stateless via JWT. Cada usuário enxerga e altera apenas as próprias tarefas.

![Java](https://img.shields.io/badge/Java-21-orange) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-green) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)

## Stack

- **Java 21** e **Spring Boot 3.3**
- **Spring Security** com filtro JWT próprio (biblioteca `jjwt`)
- **Spring Data JPA** + **PostgreSQL**, com filtros dinâmicos via `Specification`
- **Flyway** para versionamento do banco
- **Bean Validation** e tratamento global de erros com respostas padronizadas
- **springdoc-openapi** (Swagger UI)
- **JUnit 5**, **MockMvc** e **H2** nos testes
- **Docker** (build multi-stage) e **GitHub Actions**

## Funcionalidades

- Cadastro e login retornando token JWT
- CRUD de tarefas com título, descrição, prioridade e prazo
- Fluxo de status com regras de transição (`TODO`, `IN_PROGRESS`, `DONE`); uma tarefa concluída não volta para `TODO`
- Listagem paginada com filtros por status, prioridade e busca no título
- Indicador de tarefa atrasada e resumo de contagem por status
- Isolamento entre usuários: acessar tarefa de outra pessoa retorna `404`, sem revelar que o id existe

## Como rodar

### Com Docker (mais simples)

```bash
docker compose up --build
```

A API sobe em `http://localhost:8080` e o Swagger em `http://localhost:8080/swagger-ui.html`.

### Localmente

Requisitos: JDK 21 e Maven 3.9+.

```bash
docker compose up -d postgres
mvn spring-boot:run
```

### Testes

```bash
mvn verify
```

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/auth/register` | Cadastra usuário e retorna o token |
| POST | `/api/auth/login` | Autentica e retorna o token |
| GET | `/api/tasks` | Lista tarefas (filtros: `status`, `priority`, `search`, paginação) |
| GET | `/api/tasks/summary` | Contagem por status |
| GET | `/api/tasks/{id}` | Detalhe de uma tarefa |
| POST | `/api/tasks` | Cria tarefa |
| PUT | `/api/tasks/{id}` | Atualiza dados da tarefa |
| PATCH | `/api/tasks/{id}/status` | Altera o status |
| DELETE | `/api/tasks/{id}` | Remove a tarefa |

O arquivo [`requests.http`](requests.http) tem exemplos prontos para a extensão REST Client do VS Code.

### Exemplo de erro padronizado

```json
{
  "timestamp": "2026-10-17T18:30:00-03:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Dados inválidos",
  "path": "/api/tasks",
  "fields": { "title": "Título é obrigatório" }
}
```

## Variáveis de ambiente

| Variável | Padrão | Descrição |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/taskmanager` | URL do banco |
| `DB_USERNAME` / `DB_PASSWORD` | `taskmanager` | Credenciais |
| `JWT_SECRET` | valor de desenvolvimento | Segredo HMAC (mínimo 32 caracteres) |
| `JWT_EXPIRATION_MINUTES` | `60` | Validade do token |

## Estrutura

```
src/main/java/com/thiagoxavier/taskmanager
├── auth/        # cadastro e login
├── config/      # segurança e OpenAPI
├── exception/   # erros de domínio e handler global
├── security/    # geração e validação de JWT
├── task/        # domínio de tarefas (entidade, regras, API)
└── user/        # usuários
```
