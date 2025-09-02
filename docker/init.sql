-- Database initialization script for PostgreSQL
-- This script runs when the PostgreSQL container starts for the first time

-- Create database if it doesn't exist (though it's created by POSTGRES_DB env var)
-- Additional database setup can be added here

-- Set timezone
SET timezone = 'UTC';

-- Create any additional databases or users if needed
-- Example:
-- CREATE DATABASE pyme_facturacion_test;
-- GRANT ALL PRIVILEGES ON DATABASE pyme_facturacion_test TO pyme_user;
