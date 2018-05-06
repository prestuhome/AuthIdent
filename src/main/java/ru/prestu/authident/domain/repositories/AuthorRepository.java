package ru.prestu.authident.domain.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.prestu.authident.domain.model.Author;

public interface AuthorRepository extends JpaRepository<Author, Long> {

}
