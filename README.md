# Finance AI Service

AI-powered financial intelligence service built with Java, Spring Boot, Spring AI, and OpenAI.

The project is being developed as a progressive, production-oriented Finance AI platform. The implementation starts with LLM-powered chat and conversation history and will evolve toward financial tools, MCP, RAG, and Agentic AI.

---

## Project Overview

**Finance AI Service** provides a conversational AI layer for a financial application.

The long-term goal is to allow a user to ask natural-language questions such as:

- What is my current account balance?
- How much did I spend on food last month?
- Show my largest transactions this year.
- Why did my expenses increase this month?
- How much should I keep as an emergency fund?
- Summarize my recent spending.

The AI service will progressively gain the ability to:

1. Understand natural-language financial questions.
2. Maintain conversation history.
3. Call financial tools.
4. Communicate with external tools/services through MCP.
5. Retrieve relevant financial knowledge using RAG.
6. Orchestrate multi-step Agentic AI workflows.
7. Integrate with production finance services.

---

## Current Architecture

```text
                    ┌─────────────────────┐
                    │       Client        │
                    └──────────┬──────────┘
                               │
                               │ REST
                               ▼
                    ┌─────────────────────┐
                    │   Chat Controller   │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │    Chat Service     │
                    └──────────┬──────────┘
                               │
                    ┌──────────┴──────────┐
                    │                     │
                    ▼                     ▼
             ┌─────────────┐       ┌─────────────┐
             │ Chat Memory │       │     LLM     │
             │ / History   │       │ Spring AI   │
             └──────┬──────┘       └──────┬──────┘
                    │                     │
                    ▼                     ▼
              PostgreSQL              OpenAI
```

The architecture will evolve into:

```text
Client
  │
  ▼
Finance AI Service
  │
  ▼
AI Orchestrator / Agent
  │
  ├──────────────► LLM
  │
  ├──────────────► Conversation Memory
  │
  ├──────────────► Financial Tools
  │
  ├──────────────► MCP
  │
  └──────────────► RAG / Vector Search
                         │
                         ▼
                Finance Domain Services
```

---

## Technology Stack

| Area | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot |
| AI Framework | Spring AI |
| LLM | OpenAI |
| API | REST |
| Persistence | PostgreSQL |
| ORM | Spring Data JPA / Hibernate |
| Conversation Memory | Spring AI Chat Memory |
| Build | Maven |
| Boilerplate Reduction | Lombok |
| Observability | Spring Boot Actuator |
| Future Messaging | Kafka |
| Future Cache | Redis |
| Future Integration | MCP |
| Future Knowledge Retrieval | RAG |
| Future Orchestration | Agentic AI |
| Future Deployment | Docker / Kubernetes / AWS |

---

## Project Coordinates

```text
Group ID     : com.ai.service
Artifact ID  : finance-ai-service
Version      : 0.0.1-SNAPSHOT
Java         : 21
```

> The package names should remain consistent throughout the source tree. If the project has legacy `com.finance.ai` packages, they should be migrated consistently before adding new features.

---

## Project Structure

```text
finance-ai-service
│
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── ai
│   │   │           └── service
│   │   │               │
│   │   │               ├── FinanceAiApplication.java
│   │   │               │
│   │   │               ├── chat
│   │   │               │   ├── controller
│   │   │               │   ├── dto
│   │   │               │   └── service
│   │   │               │
│   │   │               ├── llm
│   │   │               │   ├── config
│   │   │               │   └── service
│   │   │               │
│   │   │               ├── memory
│   │   │               │   ├── model
│   │   │               │   ├── repository
│   │   │               │   └── service
│   │   │               │
│   │   │               ├── tools
│   │   │               │   ├── account
│   │   │               │   ├── transaction
│   │   │               │   └── analytics
│   │   │               │
│   │   │               ├── mcp
│   │   │               │   ├── client
│   │   │               │   ├── config
│   │   │               │   └── service
│   │   │               │
│   │   │               ├── rag
│   │   │               │   ├── ingestion
│   │   │               │   ├── embedding
│   │   │               │   ├── retrieval
│   │   │               │   └── service
│   │   │               │
│   │   │               ├── agent
│   │   │               │   ├── config
│   │   │               │   ├── state
│   │   │               │   ├── workflow
│   │   │               │   └── service
│   │   │               │
│   │   │               ├── common
│   │   │               │   ├── constants
│   │   │               │   ├── exception
│   │   │               │   ├── response
│   │   │               │   └── util
│   │   │               │
│   │   │               └── config
│   │   │
│   │   └── resources
│   │       ├── application.yml
│   │       ├── application-local.yml
│   │       └── application-prod.yml
│   │
│   └── test
│
├── pom.xml
├── mvnw
├── mvnw.cmd
├── .gitignore
└── README.md
```

---

# Development Roadmap

The project is intentionally being developed in phases.

## Phase 1 — Basic Chat + LLM

**Goal:** Establish a working conversational API.

```text
Client
  ↓
ChatController
  ↓
ChatService
  ↓
LlmService
  ↓
ChatClient
  ↓
OpenAI
```

Expected API:

```http
POST /api/chat
```

Example:

```json
{
  "message": "Explain what an emergency fund is in simple terms."
}
```

---

## Phase 2 — LLM & Prompt Engineering

Introduce:

- System prompts
- Application-specific instructions
- Temperature/model configuration
- Prompt templates
- Response handling
- Error handling
- LLM abstraction

Example system behavior:

```text
You are a knowledgeable financial assistant.

Provide clear, accurate, and educational information
about personal finance, investing concepts, budgeting,
and markets.

Do not provide personalized investment, tax, or legal advice.
Do not fabricate financial figures or facts.
```

---

## Phase 3 — Conversation Memory

The assistant should understand context across multiple messages.

Example:

```text
User:
My monthly income is ₹1,00,000.

Assistant:
...

User:
How much should I save?

Assistant:
Based on the income you mentioned earlier...
```

Conversation history will be associated with a conversation/session identifier and persisted through the application's memory layer.

Target architecture:

```text
Chat Request
     │
     ▼
Conversation ID
     │
     ▼
Chat Memory
     │
     ├── Previous Messages
     │
     └── Current Message
     │
     ▼
LLM
```

PostgreSQL will be used for persistent application data and conversation-related storage.

---

## Phase 4 — Financial Tools

The LLM will move from simply generating text to using application capabilities.

Planned tools include:

### Account Tools

```text
getAccountBalance()
getAccountDetails()
```

### Transaction Tools

```text
getTransactions()
getTransactionById()
getLargestTransactions()
```

### Analytics Tools

```text
getMonthlySpending()
getCategorySpending()
compareSpending()
```

Example:

```text
User:
How much did I spend on food last month?

        ↓

LLM
        ↓
Transaction / Analytics Tool
        ↓
Financial Data
        ↓
LLM Analysis
        ↓
Natural Language Response
```

---

## Phase 5 — MCP

Model Context Protocol will be introduced to standardize access to external tools and services.

Conceptually:

```text
Finance AI Service
       │
       ▼
   MCP Client
       │
       ▼
   MCP Server
       │
       ├── Account Tools
       ├── Transaction Tools
       └── Analytics Tools
```

This allows the AI layer to interact with external capabilities without tightly coupling every tool implementation to the AI service.

---

## Phase 6 — RAG

Retrieval-Augmented Generation will provide domain knowledge to the assistant.

Potential knowledge sources:

- Financial education documents
- Product documentation
- Banking policies
- Finance FAQs
- Internal finance knowledge
- Regulatory/reference documents

Flow:

```text
User Question
     │
     ▼
Query Processing
     │
     ▼
Embedding
     │
     ▼
Vector Search
     │
     ▼
Relevant Context
     │
     ▼
LLM
     │
     ▼
Grounded Response
```

---

## Phase 7 — Agentic AI

The service will evolve from a simple request/response system into an agentic workflow.

Example:

```text
User:
Why did my expenses increase this month?
```

Agent:

```text
1. Understand request
2. Retrieve current-month expenses
3. Retrieve previous-month expenses
4. Compare categories
5. Identify significant changes
6. Retrieve relevant transactions
7. Analyze results
8. Generate explanation
9. Return final response
```

Target architecture:

```text
                  ┌───────────────┐
                  │     User      │
                  └───────┬───────┘
                          │
                          ▼
                ┌───────────────────┐
                │ Finance AI API    │
                └─────────┬─────────┘
                          │
                          ▼
                ┌───────────────────┐
                │ Agent Orchestrator│
                └─────────┬─────────┘
                          │
          ┌───────────────┼────────────────┐
          ▼               ▼                ▼
        LLM           MCP / Tools          RAG
          │               │                │
          └───────────────┼────────────────┘
                          ▼
                ┌───────────────────┐
                │ Finance Services  │
                └───────────────────┘
```

---

# Productionization Roadmap

After the AI capabilities are complete, the service will be hardened for production.

Planned areas:

### Persistence

- PostgreSQL
- Database migrations
- Transaction management
- Indexing
- Query optimization

### Caching

- Redis
- Conversation/session caching
- Frequently requested financial data

### Messaging

- Apache Kafka
- Event-driven processing
- Async workflows
- Audit events

### Security

- Authentication
- Authorization
- JWT/OAuth2
- User-level data isolation
- API validation
- Secret management

### Observability

- Actuator
- Metrics
- Structured logging
- Distributed tracing
- Prometheus
- Grafana
- Centralized logging

### Deployment

```text
Docker
   ↓
Kubernetes
   ↓
AWS
```

Potential AWS services:

```text
EKS
RDS
ElastiCache
S3
CloudWatch
Secrets Manager
MSK
```

---

# Configuration

The application uses environment variables for secrets.

Example:

```text
OPENAI_API_KEY
OPENAI_MODEL
DB_USERNAME
DB_PASSWORD
```

Do not commit secrets to source control.

Example PowerShell configuration:

```powershell
$env:OPENAI_API_KEY="your-api-key"
$env:OPENAI_MODEL="gpt-4o-mini"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="your-password"
```

---

# Running Locally

Build the project:

```bash
mvn clean test
```

Run the application:

```bash
mvn spring-boot:run
```

Run using the local profile:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

---

# API Testing

Example chat request:

```http
POST http://localhost:9090/api/chat
Content-Type: application/json
```

```json
{
  "message": "Explain what an emergency fund is in simple terms."
}
```

The response should contain the generated assistant response.

---

# Health Check

With Actuator enabled:

```http
GET http://localhost:9090/actuator/health
```

---

# Configuration Profiles

## Local

```text
application-local.yml
```

Used for local development.

## Production

```text
application-prod.yml
```

Used for production-specific configuration.

Secrets and environment-specific values should be supplied through environment variables or a secure secret-management system.

---

# Financial AI Safety Principles

The assistant is designed to provide educational financial information.

It should:

- Clearly distinguish education from personalized financial advice.
- Avoid fabricating account balances or transactions.
- Never invent financial figures.
- Avoid unsupported investment recommendations.
- Avoid pretending to have access to data it cannot retrieve.
- Ask for clarification when required.
- Use application tools when actual financial data is required.
- Protect user-specific financial information.
- Keep financial data isolated by user and conversation.

---

# Engineering Principles

The project follows these principles:

- Clean separation of concerns
- Dependency inversion
- Small focused services
- Explicit DTOs
- Configuration through environment variables
- Testable business logic
- Production-oriented exception handling
- Observability from the beginning
- Secure handling of financial data
- Incremental architecture evolution

The AI layer should not directly own or duplicate financial domain logic. Financial facts should come from trusted application services/tools, while the AI layer focuses on understanding, orchestration, reasoning, and response generation.

---

# Current Implementation Status

```text
[✓] Project foundation
[✓] Spring Boot application
[✓] LLM configuration
[✓] OpenAI integration foundation
[✓] Chat API foundation
[✓] Conversation/memory architecture foundation

[ ] Complete persistent conversation flow
[ ] Prompt engineering layer
[ ] Financial account tools
[ ] Transaction tools
[ ] Analytics tools
[ ] MCP integration
[ ] RAG ingestion
[ ] Vector retrieval
[ ] Agentic workflow
[ ] Security
[ ] Kafka/event-driven processing
[ ] Redis caching
[ ] Observability stack
[ ] Docker
[ ] Kubernetes
[ ] AWS deployment
```

---

# Long-Term Vision

The final system is intended to become a production-style AI financial assistant:

```text
                         Finance AI Platform
                                │
             ┌──────────────────┼──────────────────┐
             │                  │                  │
             ▼                  ▼                  ▼
          LLM Layer        Memory Layer        Knowledge
             │                  │                  │
             │                  ▼                  ▼
             │             PostgreSQL            RAG
             │
             ▼
       Agent Orchestrator
             │
       ┌─────┼─────┐
       ▼     ▼     ▼
    Tools   MCP  Analytics
       │     │     │
       └─────┼─────┘
             ▼
      Finance Services
```

The objective is not merely to build a chatbot, but to learn and demonstrate how a modern AI service evolves from a simple LLM API into a secure, observable, tool-using, retrieval-enabled, agentic financial platform.

---

## License

This project is intended as a learning and engineering project.
