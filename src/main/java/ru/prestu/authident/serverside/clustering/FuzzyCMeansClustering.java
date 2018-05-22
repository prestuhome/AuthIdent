package ru.prestu.authident.serverside.clustering;

import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.distance.DistanceMeasure;
import ru.prestu.authident.domain.model.entities.Book;
import ru.prestu.authident.serverside.clustering.elements.Cluster;
import ru.prestu.authident.serverside.clustering.elements.Element;

import java.util.ArrayList;
import java.util.List;

public class FuzzyCMeansClustering {

    private Element elementWithUnknownAuthor = null;

    private DistanceMeasure distance;
    private boolean useNorm;
    private int[] characteristicOrdinals;

    public FuzzyCMeansClustering(DistanceMeasure distance, boolean useNorm, int[] characteristicOrdinals) {
        this.distance = distance;
        this.useNorm = useNorm;
        this.characteristicOrdinals = characteristicOrdinals;
    }

    public List<Cluster> cluster(List<Book> books) {
        List<Element> elements = getElementsFromBooks(books);
        List<Cluster> clusters = getExactClusters(elements);
        if (elementWithUnknownAuthor != null) elements.remove(elementWithUnknownAuthor);

        System.out.println(distance.getClass().getSimpleName() + ":");
        double scatterCriterion = calculateScatterCriterion(clusters);
        System.out.println(scatterCriterion);
        double newScatterCriterion;
        for (Element element : elements) {
            Cluster oldCluster = clusters.stream().filter(c -> c.getElements().contains(element)).findFirst().get();
            for (int j = 0; j < clusters.size(); j++) {
                Cluster cluster = clusters.get(j);
                if (cluster.equals(oldCluster)) continue;
                oldCluster.removeElement(element);
                cluster.addElement(element);
                newScatterCriterion = calculateScatterCriterion(clusters);
                if (newScatterCriterion < scatterCriterion) {
                    scatterCriterion = newScatterCriterion;
                    System.out.println(scatterCriterion);
                    oldCluster = cluster;
                } else {
                    oldCluster.addElement(element);
                    cluster.removeElement(element);
                }
            }
        }

        if (elementWithUnknownAuthor != null) {
            Cluster oldCluster = clusters.get(0);
            oldCluster.addElement(elementWithUnknownAuthor);
            scatterCriterion = calculateScatterCriterion(clusters);
            for (int j = 1; j < clusters.size(); j++) {
                Cluster cluster = clusters.get(j);
                if (cluster.equals(oldCluster)) continue;
                oldCluster.removeElement(elementWithUnknownAuthor);
                cluster.addElement(elementWithUnknownAuthor);
                newScatterCriterion = calculateScatterCriterion(clusters);
                if (newScatterCriterion < scatterCriterion) {
                    scatterCriterion = newScatterCriterion;
                    System.out.println(scatterCriterion);
                    oldCluster = cluster;
                } else {
                    oldCluster.addElement(elementWithUnknownAuthor);
                    cluster.removeElement(elementWithUnknownAuthor);
                }
            }
        }

        return clusters;
    }

    private double calculateScatterCriterion(List<Cluster> clusters) {
        double scatterCriterion = 0;
        for (Cluster cluster : clusters) {
            for (Element element : cluster.getElements()) {
                double[] point = new double[characteristicOrdinals.length];
                double[] center = new double[characteristicOrdinals.length];
                if (useNorm) {
                    for (int i = 0; i < characteristicOrdinals.length; i++) {
                        point[i] = element.getNormPoint()[characteristicOrdinals[i]];
                        center[i] = cluster.getNormCenter()[characteristicOrdinals[i]];
                    }
                } else {
                    for (int i = 0; i < characteristicOrdinals.length; i++) {
                        point[i] = element.getPoint()[characteristicOrdinals[i]];
                        center[i] = cluster.getCenter()[characteristicOrdinals[i]];
                    }
                }
                scatterCriterion += distance.measure(new DenseInstance(point), new DenseInstance(center));
            }
        }
        return scatterCriterion;
    }

    private List<Element> getElementsFromBooks(List<Book> books) {
        List<Element> elements = new ArrayList<>();
        double[] dividers = new double[20];
        for (Book book : books) {
            Element element = new Element();
            element.setBook(book);
            double[] point = book.getPoint();
            element.setPoint(point);
            for (int i = 0; i < dividers.length; i++) {
                dividers[i] += point[i] * point[i];
            }
            elements.add(element);
        }
        for (int i = 0; i < dividers.length; i++) {
            dividers[i] = Math.sqrt(dividers[i]);
        }
        for (Element element : elements) {
            double[] normPoint = new double[20];
            for (int i = 0; i < dividers.length; i++) {
                normPoint[i] = element.getPoint()[i] / dividers[i];
            }
            element.setNormPoint(normPoint);
        }
        return elements;
    }


    private List<Cluster> getExactClusters(List<Element> elements) {
        List<Cluster> clusters = new ArrayList<>();
        for (Element element : elements) {
            if (element.getBook().getAuthor() == null) {
                elementWithUnknownAuthor = element;
                continue;
            }
            if (clusters.stream().anyMatch(c -> c.getAuthor().equals(element.getBook().getAuthor()))) {
                clusters.stream().filter(c -> c.getAuthor().equals(element.getBook().getAuthor())).findFirst().ifPresent(c -> c.addElement(element));
            } else {
                Cluster cluster = new Cluster();
                cluster.setAuthor(element.getBook().getAuthor());
                cluster.addElement(element);
                clusters.add(cluster);
            }
        }
        return clusters;
    }
}
