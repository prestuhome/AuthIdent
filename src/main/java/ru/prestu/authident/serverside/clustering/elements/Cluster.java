package ru.prestu.authident.serverside.clustering.elements;

import ru.prestu.authident.serverside.clustering.statistic.Statistical;

import java.util.List;

public class Cluster<C, E extends Statistical> {

    private Number centroid;
    private List<E> elements;

}
