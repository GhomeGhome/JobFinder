-- Show schema for INTERVIEWS table in H2 database
-- Run this in H2 Console or via JDBC

-- Option 1: Show columns and types
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    IS_NULLABLE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'INTERVIEWS'
ORDER BY ORDINAL_POSITION;

-- Option 2: Show full DDL (create statement)
SCRIPT TABLE INTERVIEWS;

-- Option 3: Simple describe
SHOW COLUMNS FROM INTERVIEWS;
