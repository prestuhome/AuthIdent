package ru.prestu.authident.serverside.clustering.implementation;

import ru.prestu.authident.serverside.clustering.ClusteringAnalysis;
import ru.prestu.authident.serverside.clustering.elements.Cluster;
import ru.prestu.authident.serverside.clustering.statistic.Statistical;

import java.util.Collection;
import java.util.List;

public class DumbClusteringAnalysis implements ClusteringAnalysis {

    @Override
    public List<Cluster> clustering(Collection<? extends Statistical> elements) {
        return null;
    }
}
