package ru.prestu.authident.serverside.clustering;

import ru.prestu.authident.serverside.clustering.elements.Cluster;
import ru.prestu.authident.serverside.clustering.statistic.Statistical;

import java.util.Collection;
import java.util.List;

public interface ClusteringAnalysis {

    List<Cluster> clustering(Collection<? extends Statistical> elements);
}
