#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE USER springboot WITH PASSWORD '12345';
    CREATE DATABASE springboot;
    GRANT ALL PRIVILEGES ON DATABASE springboot TO springboot;
    \c springboot
    GRANT ALL ON schema public TO springboot
EOSQL