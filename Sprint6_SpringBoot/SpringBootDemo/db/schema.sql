DROP TABLE IF EXISTS book;

CREATE TABLE book (
                      book_id     INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                      title       VARCHAR(100)  NOT NULL,
                      author      VARCHAR(100)  NOT NULL,
                      description VARCHAR(255),
                      price       NUMERIC(10,2) NOT NULL
);