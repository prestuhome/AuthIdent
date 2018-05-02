CREATE TABLE authors
(
  id         BIGSERIAL NOT NULL PRIMARY KEY,
  first_name VARCHAR(1024) DEFAULT '',
  last_name  VARCHAR(1024) DEFAULT '',
  patronymic VARCHAR(1024) DEFAULT ''
);

create table books
(
  id         BIGSERIAL NOT NULL PRIMARY KEY,
  name       VARCHAR(1024) DEFAULT '',
  author_id  BIGINT CONSTRAINT books_authors_id_fk REFERENCES authors ON DELETE SET NULL,
  book_info  DOUBLE PRECISION []
);