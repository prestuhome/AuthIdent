package ru.prestu.authident.domain.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.prestu.authident.domain.model.entities.Customer;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findByLastNameStartsWithIgnoreCase(String lastName);
}
