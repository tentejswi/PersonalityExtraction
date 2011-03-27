CREATE DATABASE IF NOT EXISTS pe;

USE pe;

CREATE TABLE user_queue(handle VARCHAR(255), done TINYINT(1) DEFAULT 0, updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, PRIMARY KEY(handle));