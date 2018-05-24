package ru.prestu.authident.serverside.clustering.elements;

import net.sf.javaml.core.DenseInstance;
import ru.prestu.authident.domain.model.entities.Author;
import ru.prestu.authident.domain.model.enums.Distances;

import java.util.ArrayList;
import java.util.List;

public class Cluster {

    private List<Element> elements = new ArrayList<>();
    private double[] center = new double[20];
    private double maxDist;
    private double[] normCenter = new double[20];
    private double maxNormDist;
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

    public double getMaxDist() {
        return maxDist;
    }

    public double[] getNormCenter() {
        return normCenter;
    }

    public double getMaxNormDist() {
        return maxNormDist;
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
        calculateMaxDist();
    }

    public void removeElement(Element element) {
        elements.remove(element);
        calculateCenters();
        calculateMaxDist();
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

    private void calculateMaxDist() {
        maxDist = 0;
        maxNormDist = 0;
        for (Element element : elements) {
            maxDist = Math.max(maxDist, Distances.EUCLIDEAN_DISTANCE.getDistance().measure(new DenseInstance(center), new DenseInstance(element.getPoint())));
            maxNormDist = Math.max(maxNormDist, Distances.EUCLIDEAN_DISTANCE.getDistance().measure(new DenseInstance(normCenter), new DenseInstance(element.getNormPoint())));
        }
    }
}
