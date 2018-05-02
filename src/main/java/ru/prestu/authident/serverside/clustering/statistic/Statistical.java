package ru.prestu.authident.serverside.clustering.statistic;

import java.util.List;

public interface Statistical {

    List<? extends Number> getStatisticVector();
}
