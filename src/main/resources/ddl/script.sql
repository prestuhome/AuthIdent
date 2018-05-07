create table authors
(
  id         bigserial                                     not null
    constraint authors_pkey
    primary key,
  first_name varchar(1024) default '' :: character varying not null,
  last_name  varchar(1024) default '' :: character varying not null,
  patronymic varchar(1024) default '' :: character varying not null
);

create table books
(
  id        bigserial                                     not null
    constraint books_pkey
    primary key,
  name      varchar(1024) default '' :: character varying not null,
  author_id bigint
    constraint books_authors_id_fk
    references authors
    on delete set null,
  book_info double precision []
);
