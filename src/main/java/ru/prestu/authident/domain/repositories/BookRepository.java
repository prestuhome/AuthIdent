package ru.prestu.authident.domain.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.prestu.authident.domain.model.entities.Book;

public interface BookRepository extends JpaRepository<Book, Long> {

}
