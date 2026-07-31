DROP DATABASE IF EXISTS judge;
CREATE DATABASE judge;
USE judge;

CREATE TABLE users(
	user_id INT AUTO_INCREMENT PRIMARY KEY,
	username VARCHAR(30) NOT NULL UNIQUE,
	password_hash VARCHAR(255) NOT NULL,
	score INT DEFAULT 0,
	role ENUM('USER', 'ADMIN') DEFAULT 'USER'
);

CREATE TABLE problems (
    problem_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    points INT NOT NULL
);

CREATE TABLE submissions (
    submission_id INT AUTO_INCREMENT PRIMARY KEY,
    code LONGTEXT NOT NULL,
    language VARCHAR(30) NOT NULL,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    verdict VARCHAR(30),
    user_id INT NOT NULL,
    problem_id INT NOT NULL,

    FOREIGN KEY (user_id)
        REFERENCES users(user_id),

    FOREIGN KEY (problem_id)
        REFERENCES problems(problem_id)
);

INSERT INTO users(username, password_hash, role) VALUES
('admin', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'ADMIN');

INSERT INTO problems(title, points) VALUES
('Sum Two Numbers', 1),
('Sort Array', 2),
('Reverse Array', 1);