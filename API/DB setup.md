



# Database setup

This is a simple MySQL database setup for the chat application. It creates a database called `TorCord` and a table called `messages` with the following columns:

`CREATE DATABASE IF NOT EXISTS TorCord;`

`USE TorCord;`

`CREATE TABLE IF NOT EXISTS messages ( msgID INT AUTO_INCREMENT,
    userID VARCHAR(255),
    content TEXT,
    datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (msgID)
);`

`INSERT INTO messages (userID, content) VALUES ('test_user', 'This is a test message');`