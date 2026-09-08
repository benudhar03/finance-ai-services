-- Required for pgvector-backed VectorStore
CREATE EXTENSION IF NOT EXISTS vector;

-- Required by Spring AI's PgVectorStore schema initialization
CREATE EXTENSION IF NOT EXISTS hstore;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
