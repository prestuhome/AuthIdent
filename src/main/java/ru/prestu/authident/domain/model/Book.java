package ru.prestu.authident.domain.model;

import org.apache.commons.math3.ml.clustering.Clusterable;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "books")
public class Book implements Clusterable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Author author;
    @NotNull
    @Size(max = 1024)
    private String name;
    @ElementCollection
    private List<Double> bookInfo;

    public Book() {
    }

    public Book(String name) {
        this.name = name;
    }

    public Book(Author author, String name) {
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
    public double[] getPoint() {
        double[] points = new double[bookInfo.size()];
        for(int i = 0; i < bookInfo.size(); i++) points[i] = bookInfo.get(i);
        return points;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return Objects.equals(id, book.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return (author != null ? author.toString() + ", " : "") + "\"" + name + "\"";
    }
}
