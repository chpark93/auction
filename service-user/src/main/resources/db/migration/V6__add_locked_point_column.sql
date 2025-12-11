-- Add lockedPoint column to users table
ALTER TABLE users ADD COLUMN locked_point BIGINT NOT NULL DEFAULT 0 AFTER point;

