package ru.prestu.authident.domain.model.entities;

import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Objects;

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
    @NotNull
    @Size(max = 1024)
    private String fileName;
    @Column(name = "book_info", columnDefinition="double precision[]")
    @Type(type = "ru.prestu.authident.domain.model.types.DoubleArrayType")
    private List<Double> bookInfo;

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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public List<Double> getBookInfo() {
        return bookInfo;
    }

    public void setBookInfo(List<Double> bookInfo) {
        this.bookInfo = bookInfo;
    }

    @Transient
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
