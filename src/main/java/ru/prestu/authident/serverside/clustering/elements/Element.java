package ru.prestu.authident.serverside.clustering.elements;

import ru.prestu.authident.domain.model.entities.Book;

public class Element {

    private Book book;
    private double[] point;
    private double[] normPoint;

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public double[] getPoint() {
        return point;
    }

    public void setPoint(double[] point) {
        this.point = point;
    }

    public double[] getNormPoint() {
        return normPoint;
    }

    public void setNormPoint(double[] normPoint) {
        this.normPoint = normPoint;
    }
}
