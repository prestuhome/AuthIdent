package ru.prestu.authident.domain.model.entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

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
    @ElementCollection
    @Column(name = "book_info")
    private List<Double> bookInfo = new ArrayList<>(20);

    public Book() {
    }

    public Book(String name) {
        this.name = name;
    }

    public Book(Author author, String name, Vector vector) {
        this.author = author;
        this.name = name;
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

    public List<Double> getBookInfo() {
        return bookInfo;
    }

    public void setBookInfo(List<Double> bookInfo) {
        this.bookInfo = bookInfo;
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
        return (author != null ? author.toString() + ", " : "") + "\"" + name + "\"";
    }
}
