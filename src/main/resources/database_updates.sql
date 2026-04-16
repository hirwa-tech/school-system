
ALTER TABLE quiz_attempts ADD COLUMN IF NOT EXISTS total_marks DOUBLE PRECISION;

-- 1. Add DS role to users table if not exists
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;
ALTER TABLE users ADD CONSTRAINT users_role_check CHECK (role IN ('TEACHER', 'STUDENT', 'DS'));

-- 2. Add temporary column to users table if not exists
ALTER TABLE users ADD COLUMN IF NOT EXISTS temporary BOOLEAN NOT NULL DEFAULT FALSE;

-- 3. Create news table if not exists
CREATE TABLE IF NOT EXISTS news (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    document_name VARCHAR(255),
    document_path VARCHAR(255),
    document_size BIGINT,
    image_name VARCHAR(255),
    image_path VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Add image columns to news table if they don't exist (for existing databases)
ALTER TABLE news ADD COLUMN IF NOT EXISTS image_name VARCHAR(255);
ALTER TABLE news ADD COLUMN IF NOT EXISTS image_path VARCHAR(255);

-- 3a. Create news_likes table for news likes
CREATE TABLE IF NOT EXISTS news_likes (
    news_id BIGINT NOT NULL,
    username VARCHAR(50) NOT NULL,
    PRIMARY KEY (news_id, username),
    FOREIGN KEY (news_id) REFERENCES news(id) ON DELETE CASCADE
);

-- 3b. Create news_comments table for news comments
CREATE TABLE IF NOT EXISTS news_comments (
    news_id BIGINT NOT NULL,
    comment TEXT,
    FOREIGN KEY (news_id) REFERENCES news(id) ON DELETE CASCADE
);

-- 4. Create gallery table if not exists
CREATE TABLE IF NOT EXISTS gallery (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(20) NOT NULL CHECK (file_type IN ('IMAGE', 'VIDEO')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- 5. Add content column to notes table (if not exists)
ALTER TABLE notes ADD COLUMN IF NOT EXISTS content TEXT;

-- 6. Add file columns to notes table if they don't exist
ALTER TABLE notes ADD COLUMN IF NOT EXISTS file_name VARCHAR(255);
ALTER TABLE notes ADD COLUMN IF NOT EXISTS file_type VARCHAR(100);
ALTER TABLE notes ADD COLUMN IF NOT EXISTS file_size BIGINT;
ALTER TABLE notes ADD COLUMN IF NOT EXISTS file_data BYTEA;

-- 7. Add description column to homeworks table (if not exists)
ALTER TABLE homeworks ADD COLUMN IF NOT EXISTS description TEXT;

-- 8. Add file columns to homeworks table if they don't exist
ALTER TABLE homeworks ADD COLUMN IF NOT EXISTS file_name VARCHAR(255);
ALTER TABLE homeworks ADD COLUMN IF NOT EXISTS file_type VARCHAR(100);
ALTER TABLE homeworks ADD COLUMN IF NOT EXISTS file_size BIGINT;
ALTER TABLE homeworks ADD COLUMN IF NOT EXISTS file_data BYTEA;

-- 9. Add marks column to questions table
ALTER TABLE questions ADD COLUMN IF NOT EXISTS marks INTEGER NOT NULL DEFAULT 1;

-- 10. Update any existing questions to have default marks of 1
UPDATE questions SET marks = 1 WHERE marks IS NULL;

-- 11. Update questions type from MATCH_THE_FOLLOWING to MULTIPLE_CHOICE (if any exist)
UPDATE questions SET type = 'MULTIPLE_CHOICE' WHERE type = 'MATCH_THE_FOLLOWING';

-- Note: PostgreSQL doesn't support altering CHECK constraints directly
-- You'll need to drop and recreate the constraint if needed:
-- ALTER TABLE questions DROP CONSTRAINT IF EXISTS questions_type_check;
-- ALTER TABLE questions ADD CONSTRAINT questions_type_check CHECK (type IN ('MULTIPLE_CHOICE', 'FILL_IN_THE_GAP', 'OPEN_QUESTION'));

-- Verify the changes
SELECT id, title, content, file_name FROM notes LIMIT 10;
SELECT id, title, description, file_name, due_date FROM homeworks LIMIT 10;
SELECT id, question_text, type, marks FROM questions LIMIT 10;
SELECT id, title, content FROM news LIMIT 10;
SELECT id, title, file_name, file_type FROM gallery LIMIT 10;

-- Database Update Script for LMS Changes
-- Run this script to update your existing database

-- 0. Add total_marks column to quiz_attempts table for storing actual marks instead of percentage
ALTER TABLE quiz_attempts ADD COLUMN IF NOT EXISTS total_marks DOUBLE PRECISION;

-- 1. Add DS role to users table if not exists
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;
ALTER TABLE users ADD CONSTRAINT users_role_check CHECK (role IN ('TEACHER', 'STUDENT', 'DS'));

-- 2. Add temporary column to users table if not exists
ALTER TABLE users ADD COLUMN IF NOT EXISTS temporary BOOLEAN NOT NULL DEFAULT FALSE;

-- 3. Create news table if not exists
CREATE TABLE IF NOT EXISTS news (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    document_name VARCHAR(255),
    document_path VARCHAR(255),
    document_size BIGINT,
    image_name VARCHAR(255),
    image_path VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- 3a. Create news_likes table for news likes
CREATE TABLE IF NOT EXISTS news_likes (
    news_id BIGINT NOT NULL,
    username VARCHAR(50) NOT NULL,
    PRIMARY KEY (news_id, username),
    FOREIGN KEY (news_id) REFERENCES news(id) ON DELETE CASCADE
);

-- 3b. Create news_comments table for news comments
CREATE TABLE IF NOT EXISTS news_comments (
    news_id BIGINT NOT NULL,
    comment TEXT,
    FOREIGN KEY (news_id) REFERENCES news(id) ON DELETE CASCADE
);

-- 4. Create gallery table if not exists
CREATE TABLE IF NOT EXISTS gallery (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(20) NOT NULL CHECK (file_type IN ('IMAGE', 'VIDEO')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- 5. Add content column to notes table (if not exists)
ALTER TABLE notes ADD COLUMN IF NOT EXISTS content TEXT;

-- 6. Add file columns to notes table if they don't exist
ALTER TABLE notes ADD COLUMN IF NOT EXISTS file_name VARCHAR(255);
ALTER TABLE notes ADD COLUMN IF NOT EXISTS file_type VARCHAR(100);
ALTER TABLE notes ADD COLUMN IF NOT EXISTS file_size BIGINT;
ALTER TABLE notes ADD COLUMN IF NOT EXISTS file_data BYTEA;

-- 7. Add description column to homeworks table (if not exists)
ALTER TABLE homeworks ADD COLUMN IF NOT EXISTS description TEXT;

-- 8. Add file columns to homeworks table if they don't exist
ALTER TABLE homeworks ADD COLUMN IF NOT EXISTS file_name VARCHAR(255);
ALTER TABLE homeworks ADD COLUMN IF NOT EXISTS file_type VARCHAR(100);
ALTER TABLE homeworks ADD COLUMN IF NOT EXISTS file_size BIGINT;
ALTER TABLE homeworks ADD COLUMN IF NOT EXISTS file_data BYTEA;

-- 9. Add marks column to questions table
ALTER TABLE questions ADD COLUMN IF NOT EXISTS marks INTEGER NOT NULL DEFAULT 1;

-- 10. Update any existing questions to have default marks of 1
UPDATE questions SET marks = 1 WHERE marks IS NULL;

-- 11. Update questions type from MATCH_THE_FOLLOWING to MULTIPLE_CHOICE (if any exist)
UPDATE questions SET type = 'MULTIPLE_CHOICE' WHERE type = 'MATCH_THE_FOLLOWING';

-- Verify the changes
SELECT id, title, content, file_name FROM notes LIMIT 10;
SELECT id, title, description, file_name, due_date FROM homeworks LIMIT 10;
SELECT id, question_text, type, marks FROM questions LIMIT 10;
SELECT id, title, content FROM news LIMIT 10;
SELECT id, title, file_name, file_type FROM gallery LIMIT 10;

