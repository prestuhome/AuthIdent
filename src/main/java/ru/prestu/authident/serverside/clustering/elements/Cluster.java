package ru.prestu.authident.serverside.clustering.elements;

import ru.prestu.authident.domain.model.entities.Author;

import java.util.ArrayList;
import java.util.List;

public class Cluster {

    private List<Element> elements = new ArrayList<>();
    private double[] center = new double[20];
    private double[] normCenter = new double[20];
    private Author author;

    public List<Element> getElements() {
        return elements;
    }

    public void setElements(List<Element> elements) {
        this.elements = elements;
        calculateCenters();
    }

    public double[] getCenter() {
        return center;
    }

    public double[] getNormCenter() {
        return normCenter;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public void addElement(Element element) {
        elements.add(element);
        calculateCenters();
    }

    public void removeElement(Element element) {
        elements.remove(element);
        calculateCenters();
    }

    private void calculateCenters() {
        center = new double[20];
        normCenter = new double[20];
        for (int i = 0; i < center.length; i++) {
            center[i] = 0;
            normCenter[i] = 0;
        }
        for (Element element : elements) {
            for (int i = 0; i < center.length; i++) {
                center[i] += element.getPoint()[i];
                normCenter[i] += element.getNormPoint()[i];
            }
        }
        for (int i = 0; i < center.length; i++) {
            center[i] /= elements.size();
            normCenter[i] /= elements.size();
        }
    }
}
