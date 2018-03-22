package ru.prestu.authident.domain.model.entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Author author;
    @NotNull
    @Size(max = 1024)
    private String name;

    @OneToOne
    @JoinTable(name = "book_vector",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "vector_id"))
    private Vector vector;

    public Book() {
    }

    public Book(String name) {
        this.name = name;
    }

    public Book(Author author, String name, Vector vector) {
        this.author = author;
        this.name = name;
        this.vector = vector;
    }

    public Long getId() {
        return id;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Vector getVector() {
        return vector;
    }

    public void setVector(Vector vector) {
        this.vector = vector;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Book book = (Book) o;

        return id != null ? id.equals(book.id) : book.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return (author != null ? author.toString() + ", " : "") + name;
    }
}
